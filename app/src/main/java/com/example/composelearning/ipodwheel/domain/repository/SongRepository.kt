package com.example.composelearning.ipodwheel.domain.repository

import com.example.composelearning.ipodwheel.domain.model.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>
}
