package me.skrilltrax.themoviedb.network.api.tv

import me.skrilltrax.themoviedb.BuildConfig
import me.skrilltrax.themoviedb.model.credits.CreditsResponse
import me.skrilltrax.themoviedb.model.detail.tv.TVDetailResponse
import me.skrilltrax.themoviedb.model.list.tv.TVListResponse
import me.skrilltrax.themoviedb.model.videos.VideoResponse
import me.skrilltrax.themoviedb.network.BaseRepository

class TVDetailRepository(private val client: TVApiInterface) : BaseRepository() {

    suspend fun getShowDetails(showId: String): TVDetailResponse? = safeApiCall(
        call = { client.getShowDetails(showId, BuildConfig.API_KEY) },
        errorMessage = "Error fetching show details"
    )

    suspend fun getCastCrew(showId: String): CreditsResponse? = safeApiCall(
        call = { client.getShowCredits(showId, BuildConfig.API_KEY) },
        errorMessage = "Error fetching show credits"
    )

    suspend fun getVideos(showId: String): VideoResponse? = safeApiCall(
        call = { client.getShowVideos(showId, BuildConfig.API_KEY) },
        errorMessage = "Error fetching show videos"
    )

    suspend fun getRecommendations(showId: String): TVListResponse? = safeApiCall(
        call = { client.getShowRecommendations(showId, BuildConfig.API_KEY) },
        errorMessage = "Error fetching recommendations"
    )
}
