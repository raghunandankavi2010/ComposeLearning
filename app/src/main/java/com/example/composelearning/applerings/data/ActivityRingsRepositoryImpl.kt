package com.example.composelearning.applerings.data

import com.example.composelearning.applerings.domain.model.RingSpec
import com.example.composelearning.applerings.domain.repository.ActivityRingsRepository

/** The three rings from the original demo (Move / Exercise / Stand colors and targets). */
class ActivityRingsRepositoryImpl : ActivityRingsRepository {
    override suspend fun getRings(): List<RingSpec> = listOf(
        // Outer (Move) — red/pink, 170%.
        RingSpec(0xFFF9124E, 0xFFF93885, 0xFF32010E, targetTurns = 1.7f, insetSteps = 0),
        // Middle (Exercise) — green, 60%.
        RingSpec(0xFF99FF00, 0xFFD8FF01, 0xFF2F4E00, targetTurns = 0.6f, insetSteps = 1),
        // Inner (Stand) — cyan, 230%.
        RingSpec(0xFF00D9FD, 0xFF00FFA9, 0xFF00484D, targetTurns = 2.3f, insetSteps = 2)
    )
}
