package com.example.composelearning.charts

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * One day of fake fitness data.
 *
 * Modeled as a date-offset from today (offset 0 = today, 1 = yesterday, …). Using an offset rather
 * than a real LocalDate keeps the demo deterministic across days and trivially serializable.
 */
@Immutable
data class FitnessDay(
    val dayOffset: Int,
    val steps: Int,
    val activeMinutes: Int,
    val restingHr: Int,
)

object FitnessRepository {

    /**
     * Deterministic generator. Same [dayOffset] always returns the same day, so paging through
     * older data looks consistent across recompositions and config changes.
     */
    fun day(dayOffset: Int): FitnessDay {
        val rng = Random(31L * dayOffset.toLong() + 1)
        val baseline = 6000 + 1200 * kotlin.math.sin(dayOffset / 7f * 2f * Math.PI).toFloat()
        val noise = rng.nextInt(-1500, 1800)
        val steps = (baseline + noise).coerceIn(1500f, 15000f).roundToInt()
        val activeMinutes = (steps / 110).coerceIn(15, 120)
        val restingHr = 58 + rng.nextInt(-3, 5)
        return FitnessDay(dayOffset, steps, activeMinutes, restingHr)
    }

    /** Load a page of [count] consecutive days starting at [startOffset]. Simulates network latency. */
    suspend fun loadDays(startOffset: Int, count: Int, simulatedDelayMs: Long = 250): List<FitnessDay> {
        if (simulatedDelayMs > 0) delay(simulatedDelayMs)
        return List(count) { i -> day(startOffset + i) }
    }
}