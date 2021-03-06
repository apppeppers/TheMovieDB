package me.skrilltrax.themoviedb.network.api.tv

import me.skrilltrax.themoviedb.BuildConfig
import me.skrilltrax.themoviedb.model.list.tv.TVListResponse
import me.skrilltrax.themoviedb.network.BaseRepository

class TVListRepository(private val client: TVApiInterface) : BaseRepository() {

    suspend fun getPopularShowsList(): TVListResponse? {

        return safeApiCall(
            call = { client.getPopularShows(BuildConfig.API_KEY) },
            errorMessage = "Error fetching popular shows"
        ).apply {
            this?.results?.forEach {
                it.type = "popular"
            }
        }
    }

    suspend fun getAiringShowsList(): TVListResponse? {

        return safeApiCall(
            call = { client.getAiringShows(BuildConfig.API_KEY) },
            errorMessage = "Error fetching upcoming movies"
        ).apply {
            this?.results?.forEach {
                it.type = "upcoming"
            }
        }
    }

    suspend fun getOnAirShowsList(): TVListResponse? {

        return safeApiCall(
            call = { client.getOnAirShows(BuildConfig.API_KEY) },
            errorMessage = "Error fetching now playing movies"
        ).apply {
            this?.results?.forEach {
                it.type = "playing"
            }
        }
    }

    suspend fun getTopRatedShowsList(): TVListResponse? {

        return safeApiCall(
            call = { client.getTopRatedShows(BuildConfig.API_KEY) },
            errorMessage = "Error fetching top rated movies"
        ).apply {
            this?.results?.forEach {
                it.type = "top_rated"
            }
        }
    }
}
