package com.example.composelearning.shortsfeed.data

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/** A single playable item in the shorts feed. */
data class VideoItem(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val videoUrl: String
)

/**
 * Fake "network" data source backing the endless feed.
 *
 * Pages are generated deterministically by cycling a fixed set of public sample MP4s
 * (test-videos.co.uk, the ExoPlayer test-media buckets and archive.org — all free for
 * testing), so page N is always available and always identical — the feed is
 * effectively infinite while staying reproducible.
 *
 * In a real app this is where Retrofit/Paging would live; the ViewModel only depends
 * on the `loadPage` contract, so swapping the implementation is a one-class change.
 */
class VideoFeedRepository {

    suspend fun loadPage(page: Int, pageSize: Int = PAGE_SIZE): List<VideoItem> {
        delay(250.milliseconds) // Simulated network latency.
        return List(pageSize) { offset ->
            val index = page * pageSize + offset
            val source = sampleSources[index % sampleSources.size]
            VideoItem(
                id = "shorts_$index",
                title = source.title,
                author = source.author,
                description = source.description,
                videoUrl = source.url
            )
        }
    }

    private data class SampleSource(
        val title: String,
        val author: String,
        val description: String,
        val url: String
    )

    // Every URL below was probe-verified (HTTP 206 on a ranged GET). The usual
    // gtv-videos-bucket samples are NOT used here — they are blocked on some
    // corporate networks (403).
    private val sampleSources = listOf(
        SampleSource(
            title = "Big Buck Bunny (1080p)",
            author = "@blenderfoundation",
            description = "A giant rabbit takes on three bullying rodents. #animation #blender",
            url = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_5MB.mp4"
        ),
        SampleSource(
            title = "Sintel (1080p)",
            author = "@blenderfoundation",
            description = "A girl searches for her lost dragon companion. #sintel #openmovie",
            url = "https://test-videos.co.uk/vids/sintel/mp4/h264/1080/Sintel_1080_10s_5MB.mp4"
        ),
        SampleSource(
            title = "Jellyfish (1080p)",
            author = "@testvideos",
            description = "Jellyfish drifting in deep blue water. #ocean #relax",
            url = "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_5MB.mp4"
        ),
        SampleSource(
            title = "Android Screens",
            author = "@exoplayer",
            description = "Android UI screens montage from the ExoPlayer test suite. #android",
            url = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4"
        ),
        SampleSource(
            title = "Big Buck Bunny (720p)",
            author = "@blenderfoundation",
            description = "Bunny vs. rodents, round two. #animation",
            url = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_5MB.mp4"
        ),
        SampleSource(
            title = "Sintel (720p)",
            author = "@blenderfoundation",
            description = "Through the snow in search of Scales. #sintel",
            url = "https://test-videos.co.uk/vids/sintel/mp4/h264/720/Sintel_720_10s_5MB.mp4"
        ),
        SampleSource(
            title = "Jellyfish (720p)",
            author = "@testvideos",
            description = "More jellyfish, more calm. #ocean",
            url = "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_5MB.mp4"
        ),
        SampleSource(
            title = "Big Buck Bunny (classic)",
            author = "@exoplayer",
            description = "The 320x180 ExoPlayer demo classic. #retro",
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        ),
        SampleSource(
            title = "Bunny & Butterfly",
            author = "@w3schools",
            description = "Big Buck Bunny meets a butterfly. #animation",
            url = "https://www.w3schools.com/html/mov_bbb.mp4"
        ),
        SampleSource(
            title = "Big Buck Bunny (full movie)",
            author = "@archiveorg",
            description = "The whole short film, 720p surround. #openmovie #fullmovie",
            url = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4"
        )
    )

    companion object {
        const val PAGE_SIZE = 10
    }
}
