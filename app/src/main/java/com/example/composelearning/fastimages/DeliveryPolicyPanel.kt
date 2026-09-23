package com.example.composelearning.fastimages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Makes the delivery decision visible and forceable.
 *
 * Every number shown here is an *output* of [DeliveryPolicy], derived from
 * measured signals. The chips force the inputs so the low-end path can be
 * exercised on high-end hardware — which is the only practical way to test it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeliveryPolicyPanel(
    profile: DeliveryProfile,
    signals: DeviceSignals,
    liveKbps: Int,
    overrides: PolicyOverrides,
    onOverridesChange: (PolicyOverrides) -> Unit,
    onSimulateMemoryPressure: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Delivery policy · ${profile.label}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (liveKbps > 0) "$liveKbps kbps" else "measuring…",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "w≤${profile.bucketCapPx}px · q=${profile.quality} · " +
                    "webp${if (profile.avifPreferred) " (avif eligible)" else ""} · " +
                    "prefetch ${profile.eagerPrefetch}+${profile.diskPrefetch} · " +
                    "timeout ${profile.firstAttemptTimeoutMs}ms→${profile.retryTimeoutMs}ms · " +
                    "bitmaps ${if (profile.useRgb565) "RGB_565" else "hardware"}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = profile.rationale,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "device tier (detected: ${signals.tier.name.lowercase()})",
                style = MaterialTheme.typography.labelSmall
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TierChip("auto", overrides.deviceTier == null) {
                    onOverridesChange(overrides.copy(deviceTier = null))
                }
                DeviceTier.entries.forEach { tier ->
                    TierChip(tier.name.lowercase(), overrides.deviceTier == tier) {
                        onOverridesChange(overrides.copy(deviceTier = tier))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "network tier",
                style = MaterialTheme.typography.labelSmall
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TierChip("auto", overrides.networkTier == null) {
                    onOverridesChange(overrides.copy(networkTier = null))
                }
                NetworkTier.entries.forEach { tier ->
                    TierChip(tier.name.lowercase(), overrides.networkTier == tier) {
                        onOverridesChange(overrides.copy(networkTier = tier))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TierChip("Data Saver", overrides.forceSaveData) {
                    onOverridesChange(overrides.copy(forceSaveData = !overrides.forceSaveData))
                }
                TierChip("Tight timeout", overrides.tightTimeouts) {
                    onOverridesChange(overrides.copy(tightTimeouts = !overrides.tightTimeouts))
                }
                TextButton(onClick = onSimulateMemoryPressure) { Text("Trim memory") }
            }
        }
    }
}

@Composable
private fun TierChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}
