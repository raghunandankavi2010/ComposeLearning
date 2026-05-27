# Google Calendar Clone — Intent Specification

## Goal
Build a pixel-accurate Google Calendar clone within the ComposeLearning app, featuring:
- Collapsible month toolbar that syncs with event list scroll
- Three view modes: Schedule (list), Day, Week
- Real events from Android CalendarProvider
- Proper overlapping event layout

## User Flows

### Flow 1: Schedule View (Default)
1. User opens calendar → sees collapsible month grid (expanded) + event list below
2. Scrolling event list DOWN → month toolbar collapses to single week row
3. Scrolling event list UP → month toolbar expands back to full month
4. Tapping a day in the month grid → event list jumps to that day
5. As event list scrolls past day boundaries → selected day in toolbar updates
6. Month title in toolbar updates as user scrolls through months

### Flow 2: Day View
1. User switches to Day view via menu/button
2. Shows 24-hour vertical timeline for selected date
3. Events rendered as colored blocks at their time positions
4. Overlapping events share horizontal space (side-by-side columns)
5. Horizontal swipe → navigate to prev/next day
6. Toolbar shows date, swipe updates it

### Flow 3: Week View
1. User switches to Week view
2. Shows 7-column grid with 24-hour vertical timeline
3. Events rendered as colored blocks in their day columns
4. Overlapping events within same day share that day's column width
5. Horizontal swipe → navigate to prev/next week

## Architecture

```
googlecalendar/
├── data/
│   ├── CalendarRepository.kt           # Interface
│   ├── CalendarRepositoryImpl.kt       # CalendarProvider queries
│   └── model/
│       └── CalendarEventEntity.kt      # Raw provider data
├── domain/
│   ├── model/
│   │   ├── CalendarEvent.kt            # Domain model
│   │   └── LayoutEvent.kt             # Event with layout position
│   └── usecase/
│       └── GetEventsUseCase.kt         # Query + map events
├── ui/
│   ├── GoogleCalendarActivity.kt       # Entry point, permissions
│   ├── GoogleCalendarScreen.kt         # Main scaffold + view switching
│   ├── viewmodel/
│   │   └── GoogleCalendarViewModel.kt  # State management
│   ├── state/
│   │   ├── CalendarUiState.kt          # UI state sealed interface
│   │   └── ViewMode.kt                 # SCHEDULE, DAY, WEEK enum
│   ├── schedule/
│   │   ├── ScheduleView.kt            # Collapsible toolbar + event list
│   │   ├── MonthToolbar.kt            # Expandable/collapsible month grid
│   │   ├── WeekRow.kt                 # Single week row in toolbar
│   │   ├── DayCell.kt                 # Day cell with event dots
│   │   └── EventListItem.kt           # Event card in schedule list
│   ├── day/
│   │   ├── DayView.kt                 # 24-hour day timeline
│   │   ├── TimeColumn.kt              # Hour labels (12am-11pm)
│   │   └── EventBlock.kt              # Positioned event chip
│   ├── week/
│   │   ├── WeekView.kt                # 7-day timeline grid
│   │   └── WeekDayHeader.kt           # Mon-Sun header
│   └── common/
│       └── EventColor.kt              # Calendar color mapping
└── util/
    ├── OverlapCalculator.kt            # Event overlap layout algorithm
    └── DateUtils.kt                    # Date formatting helpers
```

## Overlap Algorithm
1. Sort events by startTime ASC, then by duration DESC
2. Group into overlap clusters (events whose time ranges intersect)
3. For each cluster, greedily assign columns:
   - Event goes in first column where it doesn't overlap with existing
   - Track max columns used in cluster
4. Each event gets: column index, total columns in its cluster
5. Layout: x = column/totalColumns * availableWidth, width = availableWidth/totalColumns

## CalendarProvider Integration
- Permission: `READ_CALENDAR` (runtime request via accompanist-permissions)
- Query `CalendarContract.Instances` for date range (handles recurring events)
- Fields: title, start, end, allDay, calendarColor, location, description
- Query strategy: load current month ± 1 month, expand as user scrolls

## Collapsible Toolbar Behavior
- Use `nestedScroll` with custom `NestedScrollConnection`
- Toolbar height interpolates between expanded (full month ~280dp) and collapsed (single week ~48dp)
- Collapse ratio drives: month grid alpha, week row visibility, header text size
- Fling should carry momentum through collapse/expand
- When collapsed, show only the week containing the selected day

## Key Technical Decisions
- No Hilt: ViewModel uses manual factory with Application context for ContentResolver
- No Room: Events come directly from CalendarProvider (read-only)
- LazyColumn for schedule view event list (infinite scroll)
- HorizontalPager for day/week swipe navigation
- Canvas-based event rendering in day/week views for performance
