# ComposeLearning Project

## Overview
A Jetpack Compose learning project containing various UI experiments and samples. Each feature lives in its own package under `com.example.composelearning`.

## Build
- **Language**: Kotlin 2.3.21
- **AGP**: 9.1.0
- **Compose BOM**: 2026.05.00
- **minSdk**: 33, **compileSdk**: 37
- **DI**: None (no Hilt) — use manual constructor injection or ViewModel factories
- **Build system**: Gradle Groovy DSL (not KTS)
- **Version catalog**: `gradle/libs.versions.toml`

## Project Structure
Modules: `:app` (Android), plus `:proto-models` (java-library holding the shared `.proto` schema + protoc-generated classes) and `:server` (Kotlin JVM desktop server) that power the Protobuf demo. See [PROTOBUF.md](app/src/main/java/com/example/composelearning/protobufdemo/PROTOBUF.md).

Features in `:app` are organized as packages under `app/src/main/java/com/example/composelearning/`:
```
├── anim/               # New Year's Eve fireworks, basic animations
├── animcompose/        # HomeScreen.kt and hub screens (AppNavigation.kt / NavigationState.kt now live in the root package)
├── applerings/         # Apple Activity Rings clone (Clean Architecture: domain/presentation)
├── breathing/          # Headspace-style breathing animation
├── calendar/           # Date range picker calendar
├── charts/             # Line, Bar, Pie, Donut, Candle, Speedometer charts
├── cleartodo/          # Clear To-Do pinch-to-create interaction
├── clocks/             # Time Range Knob / 24h dial
├── customlayout/       # Custom Pager and Arc List layouts
├── customshapes/       # Ticket shapes and other custom geometry
├── dropdown/           # Dropdown menu samples
├── flight/             # Flight seat selection UI
├── foldcard/           # 3D folding card animation
├── googlecalendar/     # Google Calendar clone with schedule/week views
├── gradients/          # Mesh gradient samples
├── graphics/           # Shaders, Path progress, Blur effects, Draw scale
├── heartfill/          # Gradient heart with diagonal fill reveal (see its README.md)
├── images/             # Overlapping avatars, Image processing (AGSL)
├── ipodwheel/          # iPod Click Wheel interaction
├── layouts/            # Percentage-based custom layout
├── lists/              # Comprehensive list demos (Swipe, Reorder, Staggered)
├── modifiers/          # Modifier order and custom modifier demos
├── pager/              # Arc carousel, Fan carousel, Instagram-style pagers
├── pathmorph/          # SVG Path morphing (Phone silhouettes)
├── peritemvm/          # Scoping ViewModels to individual list items
├── permissions/        # Passkeys and Accompanist permissions demos
├── progress/           # Progress bar / circular progress samples
├── riveo/              # Riveo-style page curl (AGSL)
├── shaders/            # AGSL Shimmer, Liquid, Spiral, Fluid spring shaders
├── shadows/            # Shadow playground (Inner, Drop, Colored)
├── sliders/            # Squiggly material slider
├── speedometer/        # Speedometer gauge components
├── spinningwheel/      # Spinning wheel / Fortune wheel
├── story/              # Story-style swipe-to-dismiss experiments
├── tabs/               # Fluid tabs and navigation samples
├── textfields/         # Marquee text and text styling
├── tutorial/           # Spotlight/Coach-mark walkthrough overlay
├── util/               # Shared utilities (LogCompositions, LocalAnimationsEnabled, convertPxToDp)
└── wallet/             # Apple Wallet collapsing card stack
```

### Key Files
- `MainActivity.kt`: Entry point using Navigation 3
- `AppNavigation.kt`: Central navigation graph and route definitions (root package)
- `animcompose/HomeScreen.kt`: Main feature list and category definitions
- `gradle/libs.versions.toml`: Version catalog for all dependencies
- `build.gradle` (root) & `app/build.gradle`: Gradle configuration (Groovy)


## Architecture Conventions
- **MVVM**: ViewModel + Compose screens
- **State**: `StateFlow` / `MutableStateFlow` in ViewModels
- **No Hilt**: ViewModels use `viewModel()` factory or manual creation
- **Navigation**: Mix of Navigation Compose and Navigation 3
- **Permissions**: Use `accompanist-permissions` library (already in deps)

## Key Dependencies Available
- Material3, Material Icons Extended
- Navigation Compose + Navigation 3
- Lifecycle (ViewModel, runtime-compose)
- Coil for images
- Accompanist Permissions
- Paging 3
- Adaptive layouts
- Kotlinx Serialization
- ConstraintLayout Compose

## Code Style
- Kotlin, Jetpack Compose throughout
- Composable functions: PascalCase
- State hoisting pattern: Route -> Screen -> Content
- Preview annotations on @Composable functions where possible
