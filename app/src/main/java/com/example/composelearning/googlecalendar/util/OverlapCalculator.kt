package com.example.composelearning.googlecalendar.util

import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import com.example.composelearning.googlecalendar.domain.model.LayoutEvent

/**
 * Calculates non-overlapping column positions for events in a day/week view.
 *
 * Algorithm:
 * 1. Sort events by start time, then by duration (longest first)
 * 2. Cluster overlapping events into groups
 * 3. Greedily assign each event to the first available column
 * 4. Return LayoutEvent with (column, totalColumns) for positioning
 */
object OverlapCalculator {

    fun calculateLayout(events: List<CalendarEvent>): List<LayoutEvent> {
        if (events.isEmpty()) return emptyList()

        val sorted = events.sortedWith(
            compareBy<CalendarEvent> { it.startTime }
                .thenByDescending { it.durationMinutes }
        )

        val clusters = buildClusters(sorted)

        return clusters.flatMap { cluster ->
            assignColumns(cluster)
        }
    }

    /**
     * Groups overlapping events into clusters.
     * Events in the same cluster have transitive overlap.
     */
    private fun buildClusters(sortedEvents: List<CalendarEvent>): List<List<CalendarEvent>> {
        val clusters = mutableListOf<MutableList<CalendarEvent>>()
        var currentCluster = mutableListOf<CalendarEvent>()
        var clusterEnd = sortedEvents.first().endTime

        for (event in sortedEvents) {
            if (event.startTime >= clusterEnd) {
                // No overlap with current cluster — start a new one
                if (currentCluster.isNotEmpty()) {
                    clusters.add(currentCluster)
                }
                currentCluster = mutableListOf(event)
                clusterEnd = event.endTime
            } else {
                // Overlaps — add to current cluster
                currentCluster.add(event)
                if (event.endTime > clusterEnd) {
                    clusterEnd = event.endTime
                }
            }
        }
        if (currentCluster.isNotEmpty()) {
            clusters.add(currentCluster)
        }

        return clusters
    }

    /**
     * Assigns column indices within a cluster using a greedy algorithm.
     * Each event goes in the first column where it doesn't overlap with already-placed events.
     */
    private fun assignColumns(cluster: List<CalendarEvent>): List<LayoutEvent> {
        // columns[i] = list of events in column i
        val columns = mutableListOf<MutableList<CalendarEvent>>()
        val eventColumns = mutableMapOf<Long, Int>()

        for (event in cluster) {
            var placed = false
            for (colIndex in columns.indices) {
                val lastInCol = columns[colIndex].last()
                if (event.startTime >= lastInCol.endTime) {
                    // Fits in this column
                    columns[colIndex].add(event)
                    eventColumns[event.id] = colIndex
                    placed = true
                    break
                }
            }
            if (!placed) {
                // Need a new column
                columns.add(mutableListOf(event))
                eventColumns[event.id] = columns.size - 1
            }
        }

        val totalColumns = columns.size

        return cluster.map { event ->
            LayoutEvent(
                event = event,
                column = eventColumns[event.id] ?: 0,
                totalColumns = totalColumns
            )
        }
    }
}
