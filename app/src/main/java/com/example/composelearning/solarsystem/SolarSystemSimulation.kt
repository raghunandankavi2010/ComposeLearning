/*
 * Copyright 2026 Raghunandan Kavi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composelearning.solarsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.isActive

/**
 * Real-world astronomical properties of a planet.
 *
 * @param semiMajorAxisAu  semi-major axis r in astronomical units (Earth = 1.0)
 * @param orbitalPeriodYears  sidereal period T in Earth years (Kepler: T ≈ r^1.5)
 * @param relativeSize  equatorial diameter relative to Earth (Earth = 1.0)
 */
@Immutable
data class PlanetConfig(
    val name: String,
    val semiMajorAxisAu: Float,
    val orbitalPeriodYears: Float,
    val relativeSize: Float,
    val color: Color,
    val hasRings: Boolean = false
)

private val Planets = listOf(
    PlanetConfig("Mercury", 0.39f, 0.24f, 0.38f, Color(0xFFB1ADAD)),
    PlanetConfig("Venus", 0.72f, 0.62f, 0.95f, Color(0xFFE8CDA2)),
    PlanetConfig("Earth", 1.00f, 1.00f, 1.00f, Color(0xFF4FC3F7)),
    PlanetConfig("Mars", 1.52f, 1.88f, 0.53f, Color(0xFFE27B58)),
    PlanetConfig("Jupiter", 5.20f, 11.86f, 11.21f, Color(0xFFD8A47F)),
    PlanetConfig("Saturn", 9.58f, 29.45f, 9.45f, Color(0xFFF5D76E), hasRings = true),
    PlanetConfig("Uranus", 19.22f, 84.02f, 4.01f, Color(0xFF7DE2D1)),
    PlanetConfig("Neptune", 30.05f, 164.79f, 3.88f, Color(0xFF5C7CFA))
)

private const val OUTERMOST_ORBIT_AU = 30.05f
private const val LARGEST_RELATIVE_SIZE = 11.21f
private const val MIN_PLANET_RADIUS_DP = 3f
private const val MAX_PLANET_RADIUS_DP = 10f

/**
 * Softening constant (AU) for the logarithmic distance scale,
 * R_i ∝ ln(1 + r_i / [this]). True linear scale is unreadable on a phone — at fit-all
 * zoom Mercury through Mars sit within 5% of Neptune's orbit, buried under the sun —
 * so distances are log-compressed: real ordering and the inner/outer contrast are
 * kept, but every orbit stays distinct. Smaller values spread the inner planets more.
 */
private const val LOG_SCALE_SOFTENING_AU = 0.4f

/**
 * Wall-clock milliseconds for one Earth year at 1× speed. Every other planet's
 * angular velocity comes from its real sidereal period relative to this.
 */
private const val SIM_YEAR_MILLIS = 12_000f

@Stable
class SolarSystemSimulationState {
    var isRunning by mutableStateOf(true)
    var speedMultiplier by mutableFloatStateOf(1f)

    /** Elapsed simulation time t in ms. Only read this inside a draw scope. */
    var elapsedMillis by mutableFloatStateOf(0f)
        internal set

    fun togglePlayPause() {
        isRunning = !isRunning
    }
}

/** Per-planet values that never change during the simulation. */
@Immutable
private data class PlanetRenderData(
    val config: PlanetConfig,
    /** ω = 2π / (t_simYear × T) in rad/ms, with T the real sidereal period in years. */
    val angularVelocityRadPerMs: Double,
    /** Log-compressed orbit radius as a fraction of the outermost orbit. */
    val orbitFraction: Float,
    /** Drawn planet radius in dp, √-normalized into [MIN, MAX] so Jupiter stays sane. */
    val radiusDp: Float
)

private val PlanetRenderList = Planets.map { planet ->
    PlanetRenderData(
        config = planet,
        // Real sidereal periods (Mercury 87.97 d … Neptune 164.79 yr), so every
        // planet runs at its true rate relative to Earth's SIM_YEAR_MILLIS year.
        angularVelocityRadPerMs =
            2.0 * Math.PI / (SIM_YEAR_MILLIS * planet.orbitalPeriodYears.toDouble()),
        orbitFraction = ln(1f + planet.semiMajorAxisAu / LOG_SCALE_SOFTENING_AU) /
            ln(1f + OUTERMOST_ORBIT_AU / LOG_SCALE_SOFTENING_AU),
        radiusDp = MIN_PLANET_RADIUS_DP +
            (MAX_PLANET_RADIUS_DP - MIN_PLANET_RADIUS_DP) *
            (sqrt(planet.relativeSize) / sqrt(LARGEST_RELATIVE_SIZE))
    )
}

@Composable
fun SolarSystemSimulation(
    modifier: Modifier = Modifier,
    state: SolarSystemSimulationState = remember { SolarSystemSimulationState() }
) {
    // Ticker: accumulate scaled frame deltas into the simulation clock. Folding the
    // speed multiplier into dt (instead of into ω) keeps every θ continuous when the
    // user drags the slider — no positional jumps. Restarting on pause/resume resets
    // lastFrame so the paused wall time is never counted.
    LaunchedEffect(state.isRunning) {
        if (!state.isRunning) return@LaunchedEffect
        var lastFrameMillis = -1L
        while (isActive) {
            withFrameMillis { frameMillis ->
                if (lastFrameMillis >= 0) {
                    state.elapsedMillis += (frameMillis - lastFrameMillis) * state.speedMultiplier
                }
                lastFrameMillis = frameMillis
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06070F))
            .systemBarsPadding()
    ) {
        SolarSystemCanvas(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        SimulationControls(state = state)
    }
}

@Composable
private fun SolarSystemCanvas(
    state: SolarSystemSimulationState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Reading elapsedMillis here (draw phase) means each frame only re-runs
        // this lambda — composition and layout are never invalidated by the ticker.
        val t = state.elapsedMillis
        val center = size.center
        val maxPlanetPx = MAX_PLANET_RADIUS_DP.dp.toPx()
        val maxOrbitRadius = min(size.width, size.height) / 2f - maxPlanetPx - 4.dp.toPx()

        val sunRadius = 11.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFC107), Color(0x00FF8F00)),
                center = center,
                radius = sunRadius * 1.8f
            ),
            radius = sunRadius * 1.8f,
            center = center
        )
        drawCircle(color = Color(0xFFFFD54F), radius = sunRadius, center = center)

        PlanetRenderList.forEach { planet ->
            // R_i = MaxRadius × ln(1 + r_i/0.4) / ln(1 + 30.05/0.4)
            val orbitRadius = maxOrbitRadius * planet.orbitFraction
            drawCircle(
                color = Color(0xFF8A93B8),
                radius = orbitRadius,
                center = center,
                alpha = 0.35f,
                style = Stroke(width = 1.dp.toPx())
            )

            // θ_i = ω_i × t, then polar → Cartesian about the canvas center.
            val theta = planet.angularVelocityRadPerMs * t
            val position = Offset(
                x = center.x + orbitRadius * cos(theta).toFloat(),
                y = center.y + orbitRadius * sin(theta).toFloat()
            )
            val planetRadius = planet.radiusDp.dp.toPx()

            if (planet.config.hasRings) {
                val ringOuter = planetRadius * 2.2f
                rotate(degrees = -18f, pivot = position) {
                    drawOval(
                        color = planet.config.color,
                        topLeft = Offset(position.x - ringOuter, position.y - ringOuter * 0.38f),
                        size = Size(ringOuter * 2f, ringOuter * 0.76f),
                        alpha = 0.55f,
                        style = Stroke(width = planetRadius * 0.35f)
                    )
                }
            }

            drawCircle(color = planet.config.color, radius = planetRadius, center = position)
            // Day-side highlight facing the sun.
            val toSun = (center - position).let { it / it.getDistance() }
            drawCircle(
                color = Color.White,
                radius = planetRadius * 0.45f,
                center = position + toSun * (planetRadius * 0.35f),
                alpha = 0.30f
            )
        }
    }
}

@Composable
private fun SimulationControls(
    state: SolarSystemSimulationState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledIconButton(
            onClick = state::togglePlayPause,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF1D2440),
                contentColor = Color(0xFFFFD54F)
            )
        ) {
            Icon(
                imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (state.isRunning) "Pause" else "Resume"
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Time warp %.2f× — 1 Earth year ≈ %.1fs".format(
                    state.speedMultiplier,
                    SIM_YEAR_MILLIS / (1000f * state.speedMultiplier)
                ),
                color = Color(0xFF9FA8C7),
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = state.speedMultiplier,
                onValueChange = { state.speedMultiplier = it },
                valueRange = 0.25f..16f
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF06070F)
@Composable
private fun SolarSystemSimulationPreview() {
    SolarSystemSimulation()
}
