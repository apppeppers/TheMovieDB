package me.skrilltrax.themoviedb.model.list.movie

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class MovieListResultItem(

    @PrimaryKey
    @field:SerializedName("id")
    val id: Int? = null,

    @ColumnInfo(name = "overview")
    @field:SerializedName("overview")
    val overview: String? = null,

    @ColumnInfo(name = "original_language")
    @field:SerializedName("original_language")
    val originalLanguage: String? = null,

    @ColumnInfo(name = "original_title")
    @field:SerializedName("original_title")
    val originalTitle: String? = null,

    @ColumnInfo(name = "video")
    @field:SerializedName("video")
    val video: Boolean? = null,

    @ColumnInfo(name = "title")
    @field:SerializedName("title")
    val title: String? = null,

    @ColumnInfo(name = "genre_ids")
    @field:SerializedName("genre_ids")
    val genreIds: List<Int?>? = null,

    @ColumnInfo(name = "poster_path")
    @field:SerializedName("poster_path")
    val posterPath: String? = null,

    @ColumnInfo(name = "backdrop_path")
    @field:SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @ColumnInfo(name = "release_date")
    @field:SerializedName("release_date")
    val releaseDate: String? = null,

    @ColumnInfo(name = "vote_average")
    @field:SerializedName("vote_average")
    val voteAverage: Double? = null,

    @ColumnInfo(name = "popularity")
    @field:SerializedName("popularity")
    val popularity: Double? = null,

    @ColumnInfo(name = "adult")
    @field:SerializedName("adult")
    val adult: Boolean? = null,

    @ColumnInfo(name = "vote_count")
    @field:SerializedName("vote_count")
    val voteCount: Int? = null,

    @ColumnInfo(name = "first_air_date")
    @field:SerializedName("first_air_date")
    val firstAirDate: String? = null,

    @ColumnInfo(name = "origin_country")
    @field:SerializedName("origin_country")
    val originCountry: List<String?>? = null,

    @ColumnInfo(name = "original_name")
    @field:SerializedName("original_name")
    val originalName: String? = null,

    @ColumnInfo(name = "name")
    @field:SerializedName("name")
    val name: String? = null,

    @ColumnInfo(name = "type")
    var type: String? = null
)
