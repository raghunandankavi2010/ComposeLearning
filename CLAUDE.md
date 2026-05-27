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
Single `app` module. Features organized as packages:
```
app/src/main/java/com/example/composelearning/
├── calendar/           # Date range picker calendar
├── googlecalendar/     # Google Calendar clone (NEW)
├── anim/               # Animation samples
├── tabs/               # Tab navigation samples
├── dropdown/           # Dropdown menu samples
├── modifiers/          # Modifier order demos
└── ...                 # Other standalone samples
```

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
