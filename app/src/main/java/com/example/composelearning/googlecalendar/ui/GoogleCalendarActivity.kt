package com.example.composelearning.googlecalendar.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.composelearning.googlecalendar.data.CalendarRepositoryImpl
import com.example.composelearning.googlecalendar.domain.usecase.GetEventsUseCase
import com.example.composelearning.googlecalendar.ui.addevent.AddEventScreen
import com.example.composelearning.googlecalendar.ui.settings.SettingsScreen
import com.example.composelearning.googlecalendar.ui.viewmodel.GoogleCalendarViewModel
import com.example.composelearning.ui.theme.GoogleCalendarTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class GoogleCalendarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val repository = CalendarRepositoryImpl(contentResolver)
        val getEventsUseCase = GetEventsUseCase(repository)
        val factory = GoogleCalendarViewModel.Factory(getEventsUseCase)
        val viewModel = ViewModelProvider(this, factory)[GoogleCalendarViewModel::class.java]

        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
            var showSettings by rememberSaveable { mutableStateOf(false) }
            var showAddEvent by rememberSaveable { mutableStateOf(false) }

            GoogleCalendarTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        showSettings -> SettingsScreen(
                            darkTheme = darkTheme,
                            onDarkThemeToggle = { darkTheme = it },
                            onBack = { showSettings = false }
                        )

                        showAddEvent -> AddEventScreen(
                            initialDate = viewModel.uiState.value.selectedDate,
                            onSave = { newEvent ->
                                viewModel.addEvent(newEvent)
                                showAddEvent = false
                            },
                            onCancel = { showAddEvent = false }
                        )

                        else -> CalendarWithPermission(
                            viewModel = viewModel,
                            onOpenSettings = { showSettings = true },
                            onAddEvent = { showAddEvent = true }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CalendarWithPermission(
    viewModel: GoogleCalendarViewModel,
    onOpenSettings: () -> Unit,
    onAddEvent: () -> Unit
) {
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
    )

    if (permissions.allPermissionsGranted) {
        LaunchedEffect(Unit) {
            viewModel.loadEvents()
        }
        GoogleCalendarScreen(
            viewModel = viewModel,
            onOpenSettings = onOpenSettings,
            onAddEvent = onAddEvent
        )
    } else {
        PermissionRequest(
            showRationale = permissions.shouldShowRationale,
            onRequestPermission = { permissions.launchMultiplePermissionRequest() }
        )
    }
}

@Composable
private fun PermissionRequest(
    showRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (showRationale) {
                "Calendar permission is needed to display your events. Please grant it to continue."
            } else {
                "This app needs access to your calendar to show events."
            },
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRequestPermission) {
            Text("Grant Calendar Permission")
        }
    }
}
