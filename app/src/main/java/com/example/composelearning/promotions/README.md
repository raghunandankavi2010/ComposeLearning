# Robust Real-Time Promotional Deal Timer

This module provides a production-grade implementation of a countdown timer for promotional deals,
specifically engineered to handle common mobile edge cases — and to be straightforward to unit test.

## Edge Case Handling

### 1. Immunity to Manual Clock Changes (Edge Case 1)
- **Problem**: Users might change their device time to exploit deals.
- **Solution**: `DealTimerViewModel` computes the remaining time **once** using the wall clock
  (`currentTimeMillis`) against the server's `targetEndTimestamp`, then anchors that duration to
  **monotonic device uptime** (`elapsedRealtime`) and ticks against uptime from then on.
- **Benefit**: Because `elapsedRealtime` cannot be modified by the user, the countdown stays
  accurate even if the system clock is changed.

### 2. Resilience to Process Death (Edge Case 2)
- **Problem**: The OS may kill the app process while backgrounded, destroying the in-memory timer.
- **Solution**: The target timestamp is persisted via `DealDataStore` (Jetpack DataStore). On
  start, the ViewModel reads any persisted value and only falls back to the server-supplied initial
  timestamp when nothing is stored yet.
- **Benefit**: After restoration the countdown resumes against the original deadline.

### 3. Immediate UI Reaction on Expiry (Edge Case 3)
- **Problem**: The UI shouldn't just sit at `00:00` or remain interactive when a deal ends.
- **Solution**: `DealTimerUiState` carries an `isExpired` flag.
- **Benefit**: `DealPromoSection` disables the "Buy Now" button (showing "Deal Ended") and
  `DealTimerText` switches to "EXPIRED" the moment the timer reaches zero.

### 4. Configuration Changes (rotation / theme / locale)
- **Problem**: Rotation and other config changes recreate the Activity and the composition; a
  naively-held timer would restart, flicker back to its start value, or lose its deadline.
- **Solution**:
  - The countdown lives in the `DealTimerViewModel`, which **survives configuration changes** by
    design, so `timerState` and its running upstream are retained.
  - `SharingStarted.WhileSubscribed(5000)` bridges the brief gap while the composition tears down
    and re-subscribes, so the shared flow is **not** cancelled/restarted across a rotation — the
    countdown continues without recomputing or flickering.
  - Values that genuinely live in the UI layer use `rememberSaveable` (not `remember`) so they
    survive the config change too.
- **Benefit**: Rotating the device keeps the exact same running countdown.

### State ownership: keeping the notification in sync
The deal's deadline has **one** source of truth: it's persisted in `DealDataStore` and resolved by
the ViewModel, then surfaced on `DealTimerUiState.targetEndTimestamp`. The "Notify Me" action reads
the deadline from that UI state rather than from a Composable-local `remember`. This guarantees the
background notification's countdown matches the on-screen timer across **both** rotation and process
death (e.g. after a swipe-kill the saved-instance bundle is gone but DataStore still holds the
original deadline — the ViewModel restores it and the notification uses the same value).

## Starting the timer: cold flow + `WhileSubscribed`

The countdown is a **cold flow** (`timerFlow()`) shared as a `StateFlow` via
`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DealTimerUiState())`:

- The upstream work **starts when the UI begins collecting** (`collectAsStateWithLifecycle`) and is
  torn down ~5s after the last collector disappears.
- There is **no `init`-block side effect** (construction would otherwise become a side effect and
  race with tests) and **no UI-driven `start()`**.

> Why not `LaunchedEffect(viewModel) { viewModel.start() }`?
> A ViewModel instance is stable for the whole composition, so `viewModel` as a key never changes —
> it's effectively `LaunchedEffect(Unit)`, which falsely implies "restart if the VM changes". Worse,
> the block re-runs every time the Composable re-enters composition, so depending on backstack and
> config-change behaviour you can re-trigger the work. Letting the shared flow start on collection
> sidesteps all of this and ties the work's lifetime to actual UI subscription.

## Designed for Testability

The timer logic can be verified without a device clock or disk:

- **`timerFlow()` is collectable directly.** Tests collect the cold flow on virtual time (e.g.
  `first()`, `toList()`, or a launched collector) and assert each emission — no `viewModelScope` or
  active UI subscriber required.
- **`TimeProvider` abstraction.** `currentTimeMillis()` / `elapsedRealtime()` are injected behind
  the `TimeProvider` interface (`SystemTimeProvider` in production). Tests supply a fake whose
  uptime is wired to the coroutine test scheduler's virtual clock, so `delay()` and uptime advance
  together and an expiring flow terminates naturally.
- **`DealStore` abstraction.** The ViewModel depends on the `DealStore` interface, not the concrete
  `DealDataStore`, so an in-memory fake replaces DataStore (and `Context`) in tests.
- **Injectable tick interval.** `tickIntervalMillis` is a constructor parameter.

## Components

### `DealTimerViewModel`
- Logic core using a shared cold flow (`stateIn` + `WhileSubscribed`) and monotonic uptime.
- No `init` side effect and no `start()`: the countdown runs while the UI collects `timerState`.
- Constructor: `DealTimerViewModel(dealStore, initialTargetEndTimestamp, timeProvider = SystemTimeProvider(), tickIntervalMillis = 1000L)`.

### `TimeProvider` / `SystemTimeProvider`
- Wraps the wall clock and monotonic uptime so time is controllable in tests.

### `DealStore` / `DealDataStore`
- Persistence boundary for the target timestamp; `DealDataStore` is the DataStore-backed impl.

### `DealTimerText` & `DealPromoSection`
- Micro-components optimized for per-second recomposition.
- Demonstrate state-driven UI (disabling buttons / changing copy on expiry).

### `NotificationHelper`
- Offloads background ticking to the Android System via `setUsesChronometer(true)` +
  `setChronometerCountDown(true)`, anchored to `elapsedRealtime` to match the in-app timer.

## Usage Example

```kotlin
// In your Composable Screen
val targetTime = remember { serverDeal.endTime } // e.g. System.currentTimeMillis() + 3600000
val dealDataStore = remember { DealDataStore(context) }

val viewModel: DealTimerViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DealTimerViewModel(
                dealStore = dealDataStore,
                initialTargetEndTimestamp = targetTime
            ) as T
    }
)

// No start() call: DealPromoSection collects timerState (collectAsStateWithLifecycle), which
// starts the WhileSubscribed flow automatically and stops it when the UI goes away.
DealPromoSection(
    viewModel = viewModel,
    onBuyNowClick = { /* Handle purchase */ }
)
```

## Tests

- **Unit tests** — `app/src/test/java/com/example/composelearning/promotions/`
  - `DealTimerViewModelTest` covers: the flow being cold (no work until collected), persisting the
    initial target, restoring a persisted target (process death), counting down, expiry,
    already-expired targets, clock-change immunity, and `timerState` sharing one upstream across
    collectors via `WhileSubscribed`.
  - `FakeDealStore` / `FakeTimeProvider` are the test doubles.
- **Compose UI tests** — `app/src/androidTest/java/com/example/composelearning/promotions/`
  - `DealPromoSectionTest` verifies the active state enables "Buy Now" and the expired state
    disables it and shows "Deal Ended" / "EXPIRED".
