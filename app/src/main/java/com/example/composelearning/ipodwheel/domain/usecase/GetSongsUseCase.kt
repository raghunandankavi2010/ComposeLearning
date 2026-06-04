package com.example.composelearning.ipodwheel.domain.usecase

import com.example.composelearning.ipodwheel.domain.model.Song
import com.example.composelearning.ipodwheel.domain.repository.SongRepository

class GetSongsUseCase(private val repository: SongRepository) {
    suspend operator fun invoke(): List<Song> = repository.getSongs()
}
