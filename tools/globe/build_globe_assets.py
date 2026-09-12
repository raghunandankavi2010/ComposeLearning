#!/usr/bin/env python3
"""
Bakes the Globe feature's runtime assets from Natural Earth vector data.

    python3 tools/globe/build_globe_assets.py [--width 4096]

Outputs (see app/src/main/java/com/example/composelearning/globe/GLOBE.md §14):
    app/src/main/assets/globe/world_index.png   8-bit equirectangular country-id raster
    app/src/main/assets/globe/countries.json    id -> name, label anchor, area, colour class

Source data: Natural Earth 1:50m admin-0 (public domain), delivered as TopoJSON by
world-atlas (ISC). Downloaded once and cached under tools/globe/.cache/.
"""

import argparse
import json
import math
import os
import subprocess
import sys
import urllib.request

import numpy as np
from PIL import Image, ImageDraw

SOURCE_URL = "https://cdn.jsdelivr.net/npm/world-atlas@2/countries-50m.json"
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
CACHE = os.path.join(HERE, ".cache", "countries-50m.json")
OUT_DIR = os.path.join(REPO, "app", "src", "main", "assets", "globe")

EARTH_RADIUS_KM = 6371.0088

# Natural Earth ships display-oriented abbreviations; expand the ones that matter (§14).
NAME_FIXES = {
    "Dem. Rep. Congo": "DR Congo",
    "Central African Rep.": "Central African Republic",
    "Eq. Guinea": "Equatorial Guinea",
    "S. Sudan": "South Sudan",
    "W. Sahara": "Western Sahara",
    "Bosnia and Herz.": "Bosnia & Herzegovina",
    "Dominican Rep.": "Dominican Republic",
    "Czechia": "Czech Republic",
    "Côte d'Ivoire": "Côte d'Ivoire",
    "N. Cyprus": "Northern Cyprus",
    "N. Mariana Is.": "Northern Mariana Islands",
    "Solomon Is.": "Solomon Islands",
    "Marshall Is.": "Marshall Islands",
    "Falkland Is.": "Falkland Islands",
    "Cayman Is.": "Cayman Islands",
    "Turks and Caicos Is.": "Turks & Caicos",
    "U.S. Virgin Is.": "US Virgin Islands",
    "British Virgin Is.": "British Virgin Islands",
    "Wallis and Futuna Is.": "Wallis & Futuna",
    "Fr. Polynesia": "French Polynesia",
    "Fr. S. Antarctic Lands": "French Southern Territories",
    "Heard I. and McDonald Is.": "Heard & McDonald Islands",
    "S. Geo. and the Is.": "South Georgia",
    "St. Kitts and Nevis": "Saint Kitts & Nevis",
    "St. Vin. and Gren.": "Saint Vincent & Grenadines",
    "St. Pierre and Miquelon": "Saint Pierre & Miquelon",
    "Antigua and Barb.": "Antigua & Barbuda",
    "Trinidad and Tobago": "Trinidad & Tobago",
    "São Tomé and Principe": "São Tomé & Príncipe",
    "Br. Indian Ocean Ter.": "British Indian Ocean Territory",
    "Cook Is.": "Cook Islands",
    "Faeroe Is.": "Faroe Islands",
    "Åland": "Åland Islands",
    "Pitcairn Is.": "Pitcairn Islands",
    "Sint Maarten": "Sint Maarten",
    "Vatican": "Vatican City",
    "eSwatini": "Eswatini",
    "Timor-Leste": "Timor-Leste",
    "Bahamas": "The Bahamas",
    "Gambia": "The Gambia",
}


def log(*a):
    print(*a, file=sys.stderr, flush=True)


# ── 1. source data ────────────────────────────────────────────────────────────


def download(url, dest):
    """urllib first; fall back to curl, whose trust store is configured more often."""
    try:
        with urllib.request.urlopen(url, timeout=120) as r:
            data = r.read()
        with open(dest, "wb") as f:
            f.write(data)
        return
    except Exception as e:
        log(f"urllib failed ({e.__class__.__name__}), falling back to curl")
    subprocess.run(["curl", "-sSL", "--fail", "-o", dest, url], check=True)


def load_topology():
    if not os.path.exists(CACHE):
        os.makedirs(os.path.dirname(CACHE), exist_ok=True)
        log(f"downloading {SOURCE_URL}")
        download(SOURCE_URL, CACHE)
    with open(CACHE) as f:
        return json.load(f)


def decode_arcs(topo):
    """TopoJSON quantised delta decoding: GLOBE.md §14a."""
    sx, sy = topo["transform"]["scale"]
    tx, ty = topo["transform"]["translate"]
    arcs = []
    for arc in topo["arcs"]:
        x = y = 0
        pts = np.empty((len(arc), 2), dtype=np.float64)
        for i, (dx, dy) in enumerate(arc):
            x += dx
            y += dy
            pts[i, 0] = x * sx + tx
            pts[i, 1] = y * sy + ty
        arcs.append(pts)
    return arcs


def ring_points(arc_refs, arcs):
    """Stitch a ring from signed arc references (~a == -a-1 means 'reversed')."""
    out = []
    for ref in arc_refs:
        pts = arcs[ref] if ref >= 0 else arcs[~ref][::-1]
        out.append(pts if not out else pts[1:])  # drop the duplicated join point
    return np.concatenate(out) if out else np.zeros((0, 2))


def build_countries(topo, arcs):
    countries = []
    for geom in topo["objects"]["countries"]["geometries"]:
        polys_refs = geom["arcs"] if geom["type"] == "MultiPolygon" else [geom["arcs"]]
        polygons = [[ring_points(r, arcs) for r in poly] for poly in polys_refs]
        used = set()
        for poly in polys_refs:
            for ring in poly:
                used.update(a if a >= 0 else ~a for a in ring)
        raw = geom["properties"]["name"]
        countries.append({
            "m49": int(geom.get("id") or 0),
            "raw_name": raw,
            "name": NAME_FIXES.get(raw, raw),
            "polygons": polygons,
            "arc_ids": used,
        })
    return countries


# ── 2. spherical area (GLOBE.md §14d) ─────────────────────────────────────────


def ring_area_steradians(ring):
    """Signed area of a spherical polygon, Green's theorem on the sphere."""
    if len(ring) < 3:
        return 0.0
    lon = np.radians(ring[:, 0])
    lat = np.radians(ring[:, 1])
    lon2 = np.roll(lon, -1)
    lat2 = np.roll(lat, -1)
    dlon = lon2 - lon
    dlon = (dlon + math.pi) % (2 * math.pi) - math.pi  # shortest signed difference
    return float(np.sum(dlon * (2.0 + np.sin(lat) + np.sin(lat2))) / 2.0)


def country_area(country):
    total = 0.0
    for poly in country["polygons"]:
        for i, ring in enumerate(poly):
            a = ring_area_steradians(ring)
            total += abs(a) if i == 0 else -abs(a)  # outer +, holes −
    return abs(total)


# ── 3. adjacency + DSATUR colouring (GLOBE.md §14b) ───────────────────────────


def colour_map(countries):
    n = len(countries)
    by_arc = {}
    for i, c in enumerate(countries):
        for a in c["arc_ids"]:
            by_arc.setdefault(a, []).append(i)
    adj = [set() for _ in range(n)]
    for owners in by_arc.values():
        for i in owners:
            for j in owners:
                if i != j:
                    adj[i].add(j)

    colours = [-1] * n
    for _ in range(n):
        # DSATUR: most saturated first, ties broken by degree
        best, best_key = -1, None
        for i in range(n):
            if colours[i] != -1:
                continue
            sat = len({colours[j] for j in adj[i] if colours[j] != -1})
            key = (sat, len(adj[i]))
            if best_key is None or key > best_key:
                best, best_key = i, key
        taken = {colours[j] for j in adj[best] if colours[j] != -1}
        c = 0
        while c in taken:
            c += 1
        colours[best] = c
    return colours, adj


# ── 4. rasterisation (GLOBE.md §14e) ──────────────────────────────────────────


def to_texel(ring, w, h):
    x = (ring[:, 0] + 180.0) / 360.0 * w
    y = (90.0 - ring[:, 1]) / 180.0 * h
    return list(zip(x.tolist(), y.tolist()))


def rasterise(countries, order, w, h):
    """Paint country ids into a w×h equirectangular uint8 raster. 0 = ocean."""
    canvas = np.zeros((h, w), dtype=np.uint8)
    for idx_in_order, ci in enumerate(order):
        country = countries[ci]
        cid = country["id"]
        for poly in country["polygons"]:
            outer = poly[0]
            if len(outer) < 3:
                continue
            xs = (outer[:, 0] + 180.0) / 360.0 * w
            ys = (90.0 - outer[:, 1]) / 180.0 * h
            x0 = max(0, int(math.floor(xs.min())) - 1)
            x1 = min(w, int(math.ceil(xs.max())) + 2)
            y0 = max(0, int(math.floor(ys.min())) - 1)
            y1 = min(h, int(math.ceil(ys.max())) + 2)
            bw, bh = x1 - x0, y1 - y0
            if bw <= 0 or bh <= 0:
                continue
            mask = Image.new("1", (bw, bh), 0)
            d = ImageDraw.Draw(mask)
            d.polygon([(x - x0, y - y0) for x, y in to_texel(outer, w, h)], fill=1)
            for hole in poly[1:]:  # even-odd: holes punched out
                if len(hole) >= 3:
                    d.polygon([(x - x0, y - y0) for x, y in to_texel(hole, w, h)], fill=0)
            m = np.array(mask, dtype=bool)
            if m.any():
                sub = canvas[y0:y1, x0:x1]
                sub[m] = cid
    return canvas


def stamp_missing(canvas, countries, w, h, radius=2):
    """Sub-texel countries (§14e): stamp a small disc so they exist and are tappable."""
    stamped = []
    present = np.unique(canvas)
    for c in countries:
        if c["id"] in present:
            continue
        pts = np.concatenate([p[0] for p in c["polygons"] if len(p[0])])
        lon = float(np.mean(pts[:, 0]))
        lat = float(np.mean(pts[:, 1]))
        cx = int((lon + 180.0) / 360.0 * w)
        cy = int((90.0 - lat) / 180.0 * h)
        for dy in range(-radius, radius + 1):
            for dx in range(-radius, radius + 1):
                if dx * dx + dy * dy > radius * radius:
                    continue
                x, y = (cx + dx) % w, min(max(cy + dy, 0), h - 1)
                canvas[y, x] = c["id"]
        c["stamped"] = (lon, lat)
        stamped.append(c["name"])
    return stamped


# ── 5. label anchors: largest inscribed disc on the raster (GLOBE.md §14c) ─────


def erode(mask):
    """4-neighbour binary erosion."""
    e = mask.copy()
    e[1:, :] &= mask[:-1, :]
    e[:-1, :] &= mask[1:, :]
    e[:, 1:] &= mask[:, :-1]
    e[:, :-1] &= mask[:, 1:]
    e[0, :] = False
    e[-1, :] = False
    return e


def label_anchors(canvas, countries, w, h):
    """Peel the mask until it vanishes; the last surviving texel is the deepest interior."""
    ys, xs = np.nonzero(canvas)
    ids = canvas[ys, xs]
    order = np.argsort(ids, kind="stable")
    ys, xs, ids = ys[order], xs[order], ids[order]
    bounds = np.searchsorted(ids, np.arange(0, 256))
    for c in countries:
        cid = c["id"]
        lo, hi = bounds[cid], bounds[cid + 1] if cid + 1 < 256 else len(ids)
        if hi <= lo:
            c["anchor"] = c.get("stamped", (0.0, 0.0))
            continue
        cy0, cy1 = ys[lo:hi].min(), ys[lo:hi].max() + 1
        cx0, cx1 = xs[lo:hi].min(), xs[lo:hi].max() + 1
        pad = 1
        y0, y1 = max(0, cy0 - pad), min(h, cy1 + pad)
        x0, x1 = max(0, cx0 - pad), min(w, cx1 + pad)
        mask = canvas[y0:y1, x0:x1] == cid
        last = mask
        while True:
            nxt = erode(last)
            if not nxt.any():
                break
            last = nxt
        yy, xx = np.nonzero(last)
        # The deepest layer can have several disconnected components (archipelagos, or a
        # shape with a thickness tie such as Bolivia), and their mean can fall outside the
        # country entirely — so snap to the actual texel nearest that mean.
        my, mx = yy.mean(), xx.mean()
        k = int(np.argmin((yy - my) ** 2 + (xx - mx) ** 2))
        py = float(y0 + yy[k])
        px = float(x0 + xx[k])
        c["anchor"] = (
            (px + 0.5) / w * 360.0 - 180.0,
            90.0 - (py + 0.5) / h * 180.0,
        )


# ── 6. main ───────────────────────────────────────────────────────────────────


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--width", type=int, default=4096, help="texture width (height = width/2)")
    args = ap.parse_args()
    w = args.width
    h = w // 2

    topo = load_topology()
    arcs = decode_arcs(topo)
    countries = build_countries(topo, arcs)
    log(f"{len(countries)} countries, {len(arcs)} arcs, "
        f"{sum(len(a) for a in arcs)} points")

    for c in countries:
        c["area_sr"] = country_area(c)

    # ids: 1..N in descending area, so ordering by id == ordering by size (§14e draw order)
    countries.sort(key=lambda c: -c["area_sr"])
    for i, c in enumerate(countries):
        c["id"] = i + 1
    if len(countries) > 255:
        raise SystemExit(f"{len(countries)} countries will not fit in 8 bits")

    colours, adj = colour_map(countries)
    for c, col in zip(countries, colours):
        c["colour"] = col
    log(f"map coloured with {max(colours) + 1} classes; "
        f"max degree {max(len(a) for a in adj)}")

    order = list(range(len(countries)))  # already area-descending
    canvas = rasterise(countries, order, w, h)
    stamped = stamp_missing(canvas, countries, w, h)
    log(f"raster {w}x{h}: {int((canvas > 0).sum() / canvas.size * 1000) / 10}% land, "
        f"{len(np.unique(canvas)) - 1} ids present")
    if stamped:
        log(f"stamped {len(stamped)} sub-texel countries: {', '.join(stamped)}")

    label_anchors(canvas, countries, w, h)

    os.makedirs(OUT_DIR, exist_ok=True)
    png = os.path.join(OUT_DIR, "world_index.png")
    Image.fromarray(canvas, mode="L").save(png, optimize=True, compress_level=9)

    meta = {
        "version": 1,
        "source": "Natural Earth 1:50m admin-0 via world-atlas@2 (public domain / ISC)",
        "textureWidth": w,
        "textureHeight": h,
        "colourClasses": max(colours) + 1,
        "countries": [
            {
                "id": c["id"],
                "name": c["name"],
                "lon": round(c["anchor"][0], 4),
                "lat": round(c["anchor"][1], 4),
                "areaSr": round(c["area_sr"], 9),
                "areaKm2": int(round(c["area_sr"] * EARTH_RADIUS_KM ** 2)),
                "colour": c["colour"],
            }
            for c in countries
        ],
    }
    with open(os.path.join(OUT_DIR, "countries.json"), "w") as f:
        json.dump(meta, f, ensure_ascii=False, separators=(",", ":"))

    log(f"wrote {png} ({os.path.getsize(png) / 1024:.0f} KB) and countries.json "
        f"({os.path.getsize(os.path.join(OUT_DIR, 'countries.json')) / 1024:.0f} KB)")
    biggest = countries[0]
    log(f"largest: {biggest['name']} {biggest['areaKm2'] if 'areaKm2' in biggest else int(biggest['area_sr'] * EARTH_RADIUS_KM ** 2)} km²")


if __name__ == "__main__":
    main()
