package com.example.composelearning.ipodwheel.presentation

import com.example.composelearning.ipodwheel.domain.model.Song

data class IpodState(
    val isLoading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val nowPlaying: Int? = null
)

sealed interface IpodIntent {
    data object Load : IpodIntent
    data class Select(val index: Int) : IpodIntent
}
