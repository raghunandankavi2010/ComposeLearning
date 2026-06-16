# Adaptive Layouts

A hands-on module for learning to build UIs that adapt to phones, foldables,
tablets, Chromebooks and resizable desktop windows — with **one codebase**.

Open it from the home screen: **Lists, Layouts & Pagers → "Adaptive Layouts
(multi-pane)"**.

## The one rule

> **React to the size you are given, never to the device type.**

A phone in landscape, a small free-form window, and a folded foldable can all be
*compact*. An unfolded foldable, a tablet, and a desktop window can all be
*expanded*. You never check "is this a tablet?" — you check the **WindowSizeClass**.

The standard width breakpoints (Material 3):

| Class    | Width        | Typical                              |
|----------|--------------|--------------------------------------|
| Compact  | `< 600dp`    | Phone portrait, small window         |
| Medium   | `600–839dp`  | Phone landscape, foldable cover, tablet portrait |
| Expanded | `≥ 840dp`    | Tablet landscape, unfolded foldable, desktop |

## The six demos

| # | Demo | API | When to use |
|---|------|-----|-------------|
| 1 | List–Detail | `NavigableListDetailPaneScaffold` + `rememberListDetailPaneScaffoldNavigator` | Master/detail: inbox, settings, contacts. **Reach for this first.** |
| 2 | Supporting Pane | `NavigableSupportingPaneScaffold` + `rememberSupportingPaneScaffoldNavigator` | Primary content + *auxiliary* pane: filters, related items, ToC. |
| 3 | Adaptive Grid | `LazyVerticalGrid` with `GridCells.Adaptive` vs `GridCells.Fixed` | Galleries/dashboards. Adaptive = continuous reflow; Fixed = exact per-breakpoint count. |
| 4 | Reflowing Detail | `currentWindowAdaptiveInfo().windowSizeClass` | A single page with no pane scaffold; you hand-arrange content per size class. |
| 5 | Navigation Suite | `NavigationSuiteScaffold` | Top-level navigation. One list of destinations → bottom **bar** (compact) / **rail** (medium) / **drawer** (expanded), automatically. **Reach for this for primary nav.** |
| 6 | Adaptive Drawer | `ModalNavigationDrawer` ↔ `PermanentNavigationDrawer` | When you want a drawer specifically: modal+hamburger on phones, permanently docked on wide screens. The manual version of what the suite does. |

Demos 1–4 are ordered highest-level helper → lowest primitive; the scaffolds are
built on top of the size class (demo 4). Demos 5–6 are the two ways to do
**primary navigation** adaptively — the suite (automatic) vs hand-rolled (drawer).

## Key concepts the code demonstrates

- **`currentWindowAdaptiveInfo()`** — the source of truth. Exposes `windowSizeClass`
  (width/height buckets) and `windowPosture` (fold info). See `WindowSizeClassDemo.kt`
  / `rememberAdaptiveWidthClass()`.
- **The pane navigator holds a *content key*, not the object.** The list-detail
  navigator stores the selected email **id** (`Int`); we look the `Email` up from it.
  This keeps the saved state small and survives configuration changes.
- **`AnimatedPane { }`** — wrap each pane so it animates as the scaffold shows/hides it.
- **`navigator.canNavigateBack()`** — true only when a pane is stacked (compact). Used
  to decide whether to draw a back arrow in the detail pane.
- **`navigator.scaffoldValue[role]`** — query whether a given pane is currently
  `Expanded`/`Hidden`, e.g. to hide a now-redundant "show pane" button.
- **`Modifier.widthIn(max = …)`** — cap the text measure on wide panes so lines stay
  readable instead of stretching edge to edge.
- **Back navigation is automatic** with the `Navigable*` scaffolds (including predictive
  back) — you do **not** add your own `BackHandler` for the panes.

## How to actually see it adapt

A normal phone emulator is always *compact*, so the panes never split. Use one of:

- **Resizable (Experimental)** AVD → switch between Phone / Unfolded / Tablet, or drag
  the window edge.
- A **foldable** AVD → toggle the fold (the virtual sensor) in extended controls.
- **Multi-window / free-form** mode → resize the app window across 600dp / 840dp.
- A **Chromebook** or desktop window you can drag-resize.

## Libraries (already in `gradle/libs.versions.toml`)

```
androidx.compose.material3.adaptive:adaptive
androidx.compose.material3.adaptive:adaptive-layout
androidx.compose.material3.adaptive:adaptive-navigation
androidx.compose.material3:material3-adaptive-navigation-suite   # NavigationSuiteScaffold (demo 5)
```
`androidx.window.core.layout.WindowSizeClass` (the breakpoint constants) comes
transitively via these.
