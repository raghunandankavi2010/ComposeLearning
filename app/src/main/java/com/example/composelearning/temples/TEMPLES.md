# Temple Showcase

An offline guide to 38 Bengaluru temples, in English, Hindi and Kannada.

Reachable from the home screen: **App Clones & Real-world → Temple Showcase**
(`AnimScreen.TempleShowcase` in `AppNavigation.kt`).

## What it does

| Area | Behaviour |
| --- | --- |
| Discover | Search across names, areas and deities in any of the three languages; filter by deity, area, open-now, wheelchair access, near-a-metro-station and annadana; sort by name, area or distance. |
| Near me | The same list with distance sorting forced on, backed by a one-shot location fix. Falls back to a permission-required state with a deep link to app settings. |
| Detail | Photo gallery with per-image credit and licence, live open/closed status, today's windows, next ritual, how to reach (bus stop, metro line and walk time, parking), facilities, dress code, pooja-item shops, eateries, donation channels, festivals, and an about block ending in a data-confidence line. |
| Festivals | Every festival across every temple as a year-round calendar, rotated to start from the current month. Each entry can be added to the device calendar. |
| Saved | Favourites ordered nearest-first — a "temple trail" route — plus visited marks. |
| Language | Per-app language picker (`AppCompatDelegate.setApplicationLocales`) for English, Hindi and Kannada. |

Off-app hand-offs all go through a chooser so any installed app can take them
(`platform/TempleIntents.kt`): `geo:` for map and navigation, `tel:` for phone,
`ACTION_VIEW` for donation pages, `ACTION_INSERT` for calendar events, `ACTION_SEND` for share.

## Layout

```
temples/
├── data/
│   ├── TempleModels.kt        # Temple, LocalizedText, enums, Festival, NearbyPlace, …
│   ├── TempleDataCommon.kt    # commons() photo helper + everything reused across records
│   ├── TemplesCity.kt         # Pete, Avenue Road, Nagarathpet, Shivajinagar        (7)
│   ├── TemplesSouth.kt        # Basavanagudi, Gavipuram, Jayanagar, Banashankari   (12)
│   ├── TemplesNorthWest.kt    # Malleshwaram, Rajajinagar, Hebbal, Kengeri         (10)
│   ├── TemplesEast.kt         # Halasuru, Domlur, Sarjapur Rd, Whitefield, Anekal   (9)
│   ├── BengaluruTemples.kt    # the four lists concatenated
│   ├── TempleRepository.kt    # read side + festival calendar
│   ├── TempleSchedule.kt      # open/closed state from OpeningWindow + clock
│   ├── TemplePreferences.kt   # favourites / visited, DataStore
│   └── Geo.kt                 # haversine + distance formatting
├── platform/
│   ├── AppLanguage.kt         # per-app locale switching
│   ├── LocationSource.kt      # permission state + one-shot fix
│   └── TempleIntents.kt       # every hand-off to another app
└── ui/                        # TempleShowcaseApp, Discover, Detail, Festivals, Saved, Language
```

`TempleShowcaseApp` nests two adaptive components: `NavigationSuiteScaffold` turns the five
tabs into a bottom bar / rail / drawer by window width, and `NavigableListDetailPaneScaffold`
puts the list beside the detail pane on a tablet. Selection is hoisted to the top so opening a
temple from Festivals or Saved lands in the same detail pane.

## Localisation

Two mechanisms, both driven by the active app locale:

* **Fixed UI vocabulary** — section titles, facility names, deity names, day names. A small
  closed set, so ordinary string resources (`values/`, `values-hi/`, `values-kn/`), referenced
  through `@StringRes` ids on the enums. 174 strings, all three locales at parity.
* **Per-temple prose** — names, areas, descriptions, festival notes, shop notes. Hundreds of
  these, and they are data rather than UI, so they travel with the record as `LocalizedText`
  and resolve through `LocalizedText.resolve(languageTag)`, falling back to English.

`resourceConfigurations += ["en", "hi", "kn"]` in `app/build.gradle` and
`res/xml/locales_config.xml` must stay in sync — with only `en` listed, the translated folders
are stripped and the language picker has nothing to switch to.

## Data provenance

| Field | Source | Trust |
| --- | --- | --- |
| Identity, deity, history, `builtIn` | Wikipedia (Category: Hindu temples in Bengaluru) | High |
| Coordinates | Wikipedia article coordinates | High where present |
| Photos | Wikimedia Commons, via `Special:FilePath` | Licence + author on every record |
| Timings, rituals, dress code | Commonly published temple practice | Verify before travelling |
| Bus stops, metro walk times, nearby shops | Local knowledge | Indicative |

33 photos across 24 temples, every one a freely licensed Commons file (CC BY, CC BY-SA, CC0,
public domain or GFDL). The author string and licence name are stored on `TemplePhoto` and
rendered over the image, with the Commons file page one tap away — which is what the
attribution clauses require.

Two deliberate refusals to fabricate:

* **Coordinates.** 25 temples carry a verified pin. The other 13 have `location = null` rather
  than a plausible-looking guess; the maps hand-off then geocodes name + address, and distance
  sorting drops them to the bottom instead of pretending they are at distance zero.
* **BMTC route numbers.** Routes get renumbered often enough that a stale number is worse than
  none, so each record names the stop — which is stable — and points at the Namma BMTC app for
  live routes.

`DataConfidence` (HIGH / MEDIUM / LOW) is on every record and shown in the UI, and the detail
screen carries a standing "verify before you travel" disclaimer. Current split: 9 high,
23 medium, 6 low.

Only four temples carry an official website, each one checked to resolve: ISKCON Bangalore,
Ragigudda, and the two Omkar Hills temples. Phone numbers are omitted entirely rather than
guessed.

## Adding a temple

Append a `Temple` to the file for its part of the city. The `id` must be unique and stable —
it is the key the saved/visited preferences use, so renaming one silently drops a user's saved
list. Reuse the helpers in `TempleDataCommon.kt` (`bus()`, `commons()`, `hundiDonation`,
`mahaShivaratri`, `gandhiBazaar`, …) rather than re-typing the shared prose, and set
`confidence` honestly.
