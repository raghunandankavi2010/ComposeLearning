package com.example.composelearning.riveo.domain.model

/**
 * A "project" card shown in the Riveo page-curl screen.
 *
 * @param accentColor ARGB color of the bottom label strip (e.g. 0xFFBDA098).
 */
data class Project(
    val id: String,
    val title: String,
    val size: String,
    val duration: String,
    val imageUrl: String,
    val accentColor: Long,
)
