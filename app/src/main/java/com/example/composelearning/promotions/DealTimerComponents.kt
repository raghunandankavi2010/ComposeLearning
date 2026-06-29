package com.example.composelearning.promotions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composelearning.R
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * A micro-component that only recomposes when the timer state changes.
 */
@Composable
fun DealTimerText(
    viewModel: DealTimerViewModel,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    val uiState by viewModel.timerState.collectAsStateWithLifecycle()

    val formattedTime = remember(uiState.remainingMillis) {
        formatMillis(uiState.remainingMillis)
    }

    Text(
        text = if (uiState.isExpired) "EXPIRED" else formattedTime,
        modifier = modifier,
        style = style.copy(
            fontWeight = if (uiState.isExpired) FontWeight.Bold else style.fontWeight
        ),
        color = if (uiState.isExpired) MaterialTheme.colorScheme.error else color
    )
}

/**
 * The main route/screen for the Promotional Deal Timer Demo.
 */
@Composable
fun PromotionalDealRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Seed end time for a brand-new deal (one hour from now, demo purposes). rememberSaveable so a
    // first-ever-launch seed survives a configuration change before the ViewModel has persisted it.
    // Note: this is only the *fallback* seed — once the deal exists, DealDataStore (read by the
    // ViewModel) is the authoritative deadline across config changes and process death.
    val seedTargetTime = rememberSaveable { System.currentTimeMillis() + 3600000 }

    val dealDataStore = remember { DealDataStore(context) }
    val viewModel: DealTimerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DealTimerViewModel(
                    dealStore = dealDataStore,
                    initialTargetEndTimestamp = seedTargetTime
                ) as T
            }
        }
    )
    // No explicit start needed: timerState is a WhileSubscribed flow that begins ticking as soon as
    // the UI collects it (collectAsStateWithLifecycle below) and stops when it goes away.
    val uiState by viewModel.timerState.collectAsStateWithLifecycle()

    fun tryShowNotification() {
        // Use the deadline resolved by the ViewModel (from DataStore) so the background notification
        // always matches the on-screen timer — even after a rotation or process death. Fall back to
        // the seed only if the timer hasn't emitted its first tick yet.
        val targetTime = uiState.targetEndTimestamp.takeIf { it > 0L } ?: seedTargetTime
        if (NotificationHelper.areNotificationsEnabled(context)) {
            NotificationHelper.showDealCountdownNotification(context, "Exclusive Deal!", targetTime)
            scope.launch {
                snackbarHostState.showSnackbar("Notification synced with background timer.")
            }
        } else {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Notifications are muted or disabled.",
                    actionLabel = "Settings",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    NotificationHelper.openNotificationSettings(context)
                }
            }
        }
    }

    // Handle Notification Permission (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tryShowNotification()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permission denied. Notifications won't be shown.")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Marketplace Promotions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DealPromoSection(
                        viewModel = viewModel,
                        onBuyNowClick = { /* Handle buy action */ }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Notify Me in Background")
                    }
                }
            }
        }
    }
}

/**
 * Example of a screen section reacting to the timer expiration (Edge Case 3).
 * It disables the "Buy Now" button when the deal expires.
 */
@Composable
fun DealPromoSection(
    viewModel: DealTimerViewModel,
    onBuyNowClick: () -> Unit
) {
    val uiState by viewModel.timerState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Flash Sale!",
            style = MaterialTheme.typography.headlineSmall
        )

        DealTimerText(
            viewModel = viewModel,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = onBuyNowClick,
            enabled = !uiState.isExpired, // Disable action on expiry
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(if (uiState.isExpired) "Deal Ended" else "Buy Now")
        }
    }
}

/**
 * Formats milliseconds into HH:MM:SS or MM:SS.
 */
private fun formatMillis(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

@Preview(showBackground = true)
@Composable
fun DealPromoSectionPreview() {
    ComposeLearningTheme {
        Column {
            Text("Active Deal Example:")
            Text(text = formatMillis(3661000L), style = MaterialTheme.typography.titleLarge)

            Text("\nExpired Deal Example:", modifier = Modifier.padding(top = 16.dp))
            Text(
                text = "EXPIRED",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = {}, enabled = false) {
                Text("Deal Ended")
            }
        }
    }
}
