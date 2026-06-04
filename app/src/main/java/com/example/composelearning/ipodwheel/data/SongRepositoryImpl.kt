package com.example.composelearning.ipodwheel.data

import com.example.composelearning.ipodwheel.domain.model.Song
import com.example.composelearning.ipodwheel.domain.repository.SongRepository

class SongRepositoryImpl : SongRepository {
    override suspend fun getSongs(): List<Song> = listOf(
        Song("Hearts Were Gold", "Vök"),
        Song("Brother", "Matt Corby"),
        Song("Where We Were", "Siv Jakobsen"),
        Song("Ribbons", "Sons of the East"),
        Song("Do Something Beautiful", "Aquilo"),
        Song("Go Get Gone", "Lera Lynn"),
        Song("In Colour", "Jamie xx"),
        Song("Mountains", "Message to Bears"),
        Song("Pyramids", "Frank Ocean"),
        Song("Melodrama", "Lorde"),
        Song("Ibn El Leil", "Mashrou' Leila"),
        Song("Whenever, Wherever", "Shakira"),
    )
}
