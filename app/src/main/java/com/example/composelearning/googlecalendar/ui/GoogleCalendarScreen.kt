package com.example.composelearning.googlecalendar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composelearning.googlecalendar.ui.day.DayView
import com.example.composelearning.googlecalendar.ui.schedule.ScheduleView
import com.example.composelearning.googlecalendar.ui.state.ViewMode
import com.example.composelearning.googlecalendar.ui.viewmodel.GoogleCalendarViewModel
import com.example.composelearning.googlecalendar.ui.week.WeekView
import com.example.composelearning.googlecalendar.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleCalendarScreen(
    viewModel: GoogleCalendarViewModel,
    onOpenSettings: () -> Unit,
    onAddEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showViewMenu by remember { mutableStateOf(false) }

    // Toolbar expand/collapse state (owned here so the title arrow can reflect it)
    var isToolbarExpanded by remember { mutableStateOf(true) }

    // Scroll target for the Schedule view (set by the Today button)
    var scheduleScrollTarget by remember { mutableStateOf<LocalDate?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val titleText = when (uiState.viewMode) {
        ViewMode.SCHEDULE -> DateUtils.formatMonthYear(uiState.currentMonth)
        ViewMode.DAY -> DateUtils.formatDayMonth(uiState.selectedDate)
        ViewMode.WEEK -> {
            val weekStart = uiState.selectedWeekStart
            val weekEnd = weekStart.plusDays(6)
            "${DateUtils.formatDayMonth(weekStart)} - ${DateUtils.formatDayMonth(weekEnd)}"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (uiState.viewMode == ViewMode.SCHEDULE) {
                            Modifier.clickable {
                                isToolbarExpanded = !isToolbarExpanded
                            }
                        } else Modifier
                    ) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge
                        )
                        // Dropdown arrow — only in Schedule view
                        if (uiState.viewMode == ViewMode.SCHEDULE) {
                            Icon(
                                imageVector = if (isToolbarExpanded)
                                    Icons.Default.ArrowDropUp
                                else
                                    Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle month view",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.goToToday()
                        isToolbarExpanded = true
                        scheduleScrollTarget = LocalDate.now()
                    }) {
                        Icon(Icons.Default.Today, contentDescription = "Today")
                    }

                    Box {
                        IconButton(onClick = { showViewMenu = true }) {
                            val icon = when (uiState.viewMode) {
                                ViewMode.SCHEDULE -> Icons.AutoMirrored.Filled.ViewList
                                ViewMode.DAY -> Icons.Filled.CalendarViewDay
                                ViewMode.WEEK -> Icons.Filled.CalendarViewWeek
                            }
                            Icon(icon, contentDescription = "View mode")
                        }
                        DropdownMenu(
                            expanded = showViewMenu,
                            onDismissRequest = { showViewMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Schedule") },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.ViewList, null)
                                },
                                onClick = {
                                    viewModel.onViewModeChanged(ViewMode.SCHEDULE)
                                    showViewMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Day") },
                                leadingIcon = {
                                    Icon(Icons.Filled.CalendarViewDay, null)
                                },
                                onClick = {
                                    viewModel.onViewModeChanged(ViewMode.DAY)
                                    showViewMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Week") },
                                leadingIcon = {
                                    Icon(Icons.Filled.CalendarViewWeek, null)
                                },
                                onClick = {
                                    viewModel.onViewModeChanged(ViewMode.WEEK)
                                    showViewMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEvent,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add event")
            }
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.eventsByDate.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (uiState.viewMode) {
                    ViewMode.SCHEDULE -> ScheduleView(
                        selectedDate = uiState.selectedDate,
                        currentMonth = uiState.currentMonth,
                        isToolbarExpanded = isToolbarExpanded,
                        eventsByDate = uiState.eventsByDate,
                        eventDots = uiState.eventDotsByDate,
                        onDayClick = { viewModel.onDateSelected(it) },
                        onMonthChanged = { viewModel.onMonthChanged(it) },
                        onScrolledToDate = { viewModel.onScrolledToDate(it) },
                        onToolbarExpandedChanged = { isToolbarExpanded = it },
                        scrollToDate = scheduleScrollTarget,
                        onScrollConsumed = { scheduleScrollTarget = null }
                    )

                    ViewMode.DAY -> DayView(
                        selectedDate = uiState.selectedDate,
                        events = uiState.dayEvents,
                        onDateChanged = { viewModel.onDateSelected(it) }
                    )

                    ViewMode.WEEK -> WeekView(
                        selectedDate = uiState.selectedDate,
                        weekEvents = uiState.weekEvents,
                        onDateChanged = { viewModel.onDateSelected(it) }
                    )
                }
            }
        }
    }
    }
}
