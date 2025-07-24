package com.example.athlos.api

data class YouTubeSearchResponse(
    val items: List<VideoItem>
)

data class VideoItem(
    val id: VideoId
)

data class VideoId(
    val videoId: String
)