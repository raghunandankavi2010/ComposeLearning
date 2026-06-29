# Promotional Deal Timer — Architecture & Flow

Visual companion to [README.md](README.md). Diagrams use [Mermaid](https://mermaid.js.org/)
(rendered natively by GitHub and Android Studio's Markdown preview).

---

## 1. Component / Layer Architecture

How the pieces fit together and which way the dependencies point. The ViewModel depends on the
`TimeProvider` and `DealStore` **interfaces** (not the concrete classes) — that's what makes it
unit-testable on the JVM.

```mermaid
flowchart TB
    subgraph UI["UI Layer (Compose)"]
        Route["PromotionalDealRoute\n• seeds fallback end time\n• hosts permission launcher + Snackbar"]
        Section["DealPromoSection\ncollects timerState → enables/disables Buy Now"]
        TimerText["DealTimerText\nmicro-component, recomposes per tick"]
    end

    subgraph VM["ViewModel Layer (survives config changes)"]
        ViewModel["DealTimerViewModel"]
        ColdFlow["timerFlow() — cold flow"]
        State["timerState : StateFlow&lt;DealTimerUiState&gt;\nstateIn(WhileSubscribed 5s)"]
        ViewModel --- ColdFlow
        ColdFlow -- "shared as" --> State
    end

    subgraph ABS["Abstractions (injected interfaces)"]
        TimeProvider["TimeProvider\ncurrentTimeMillis / elapsedRealtime"]
        DealStore["DealStore\ntargetEndTime / saveTargetEndTime"]
    end

    subgraph IMPL["Platform Implementations"]
        SysTime["SystemTimeProvider\nSystem clock + SystemClock.elapsedRealtime"]
        DataStore["DealDataStore\nJetpack DataStore (Preferences)"]
        NotifHelper["NotificationHelper\nOS chronometer notification"]
    end

    Route --> Section
    Section --> TimerText
    Route --> ViewModel
    Section -- "collectAsStateWithLifecycle" --> State
    TimerText -- "collectAsStateWithLifecycle" --> State

    ColdFlow --> TimeProvider
    ColdFlow --> DealStore
    TimeProvider -.implemented by.-> SysTime
    DealStore -.implemented by.-> DataStore

    Route -- "Notify Me\n(reads targetEndTimestamp)" --> NotifHelper

    classDef ui fill:#e3f2fd,stroke:#1976d2,color:#0d47a1;
    classDef vm fill:#f3e5f5,stroke:#7b1fa2,color:#4a148c;
    classDef abs fill:#fff3e0,stroke:#ef6c00,color:#e65100;
    classDef impl fill:#e8f5e9,stroke:#388e3c,color:#1b5e20;
    class Route,Section,TimerText ui;
    class ViewModel,ColdFlow,State vm;
    class TimeProvider,DealStore abs;
    class SysTime,DataStore,NotifHelper impl;
```

---

## 2. Startup & Tick Flow (cold flow + `WhileSubscribed`)

The countdown has **no `init` side effect and no `start()` call**. Collection by the UI starts the
upstream; the last collector leaving (after 5s grace) stops it.

```mermaid
sequenceDiagram
    autonumber
    participant UI as DealPromoSection / DealTimerText
    participant SF as timerState (StateFlow)
    participant Flow as timerFlow() (cold)
    participant Store as DealStore
    participant Clock as TimeProvider

    UI->>SF: collectAsStateWithLifecycle()
    Note over SF: First collector → WhileSubscribed starts upstream
    SF->>Flow: subscribe

    Flow->>Store: targetEndTime.first()
    alt nothing persisted yet
        Flow->>Store: saveTargetEndTime(initialTargetEndTimestamp)
        Note right of Flow: use server/seed value
    else value already persisted
        Note right of Flow: restore saved deadline
    end

    Flow->>Clock: currentTimeMillis()  (once)
    Flow->>Clock: elapsedRealtime()    (anchor)
    Note over Flow: endElapsed = elapsedRealtime + remainingAtStart

    loop every tickIntervalMillis until remaining <= 0
        Flow->>Clock: elapsedRealtime()
        Flow-->>SF: emit DealTimerUiState(remaining, isExpired, target)
        SF-->>UI: recompose (timer text / button state)
    end

    Note over Flow: remaining == 0 → isExpired=true → loop breaks
    UI-->>SF: UI leaves (rotation / background)
    Note over SF: after 5s with no collectors → upstream torn down
```

---

## 3. Edge Cases — Why the Design Holds

```mermaid
flowchart LR
    subgraph EC1["①  Clock-change immunity"]
        direction TB
        A1["Read wall clock ONCE\ncurrentTimeMillis()"] --> A2["Anchor remaining to\nmonotonic elapsedRealtime()"]
        A2 --> A3["Tick against uptime\n→ user can't extend deal"]
    end

    subgraph EC2["②  Process-death resilience"]
        direction TB
        B1["Deadline persisted in\nDealDataStore"] --> B2["On restart: read saved value first"]
        B2 --> B3["Resume against original deadline"]
    end

    subgraph EC3["③  Immediate expiry reaction"]
        direction TB
        C1["remaining <= 0\n→ isExpired = true"] --> C2["Buy Now disabled → 'Deal Ended'\nTimer text → 'EXPIRED'"]
    end

    subgraph EC4["④  Config changes (rotation)"]
        direction TB
        D1["ViewModel survives recreation"] --> D2["WhileSubscribed(5s) bridges\nteardown/re-subscribe gap"]
        D2 --> D3["Same running countdown,\nno flicker / restart"]
    end

    classDef ec1 fill:#fde0dc,stroke:#c62828,color:#b71c1c;
    classDef ec2 fill:#e1f5fe,stroke:#0277bd,color:#01579b;
    classDef ec3 fill:#fff8e1,stroke:#f9a825,color:#f57f17;
    classDef ec4 fill:#e8f5e9,stroke:#2e7d32,color:#1b5e20;
    class A1,A2,A3 ec1;
    class B1,B2,B3 ec2;
    class C1,C2 ec3;
    class D1,D2,D3 ec4;
```

---

## 4. Single Source of Truth for the Deadline

The notification must show the *same* countdown as the screen — across rotation **and** process
death. Both read the deadline that the ViewModel resolved from DataStore, never a Composable-local
value.

```mermaid
flowchart TD
    Server["Server timestamp / seed\n(System.currentTimeMillis + 1h)"] --> DS[("DealDataStore\ntarget_end_time")]
    DS -- "resolved by timerFlow()" --> State["DealTimerUiState.targetEndTimestamp\n(single source of truth)"]

    State --> Screen["On-screen countdown\nDealTimerText"]
    State --> Notify["Notify Me action\nuiState.targetEndTimestamp"]
    Notify --> OS["NotificationHelper\nsetUsesChronometer + countDown\nanchored to elapsedRealtime"]

    Screen -. "same deadline" .- OS

    classDef src fill:#ede7f6,stroke:#5e35b1,color:#311b92;
    classDef sink fill:#e0f2f1,stroke:#00897b,color:#004d40;
    class Server,DS,State src;
    class Screen,Notify,OS sink;
```

---

### Legend
- **Solid arrow** → runtime data/control flow.
- **Dashed arrow** → "implemented by" / "stays consistent with" relationship.
- **`( )` cylinder** → persisted storage (DataStore).
