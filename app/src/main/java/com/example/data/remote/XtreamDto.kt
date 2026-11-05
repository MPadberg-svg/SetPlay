package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class XtreamAuthResponse(
    @param:Json(name = "user_info") val userInfo: XtreamUserInfo? = null,
    @param:Json(name = "server_info") val serverInfo: XtreamServerInfo? = null
)

@JsonClass(generateAdapter = true)
data class XtreamUserInfo(
    val username: String? = null,
    val status: String? = "Active",
    @param:Json(name = "exp_date") val expDate: String? = null,
    @param:Json(name = "is_trial") val isTrial: String? = "0",
    @param:Json(name = "active_cons") val activeCons: String? = "0",
    @param:Json(name = "max_connections") val maxConnections: String? = "1",
    val auth: Int? = 1,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamServerInfo(
    val url: String? = null,
    val port: String? = null,
    @param:Json(name = "https_port") val httpsPort: String? = null,
    @param:Json(name = "server_protocol") val serverProtocol: String? = "http",
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamCategoryDto(
    @param:Json(name = "category_id") val categoryId: String? = null,
    @param:Json(name = "category_name") val categoryName: String? = null,
    @param:Json(name = "parent_id") val parentId: Int? = 0
)

@JsonClass(generateAdapter = true)
data class XtreamShortEpgResponse(
    @param:Json(name = "epg_listings") val epgListings: List<XtreamEpgListingDto>? = null
)

@JsonClass(generateAdapter = true)
data class XtreamEpgListingDto(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val start: String? = null,
    val end: String? = null,
    @param:Json(name = "start_timestamp") val startTimestamp: String? = null,
    @param:Json(name = "stop_timestamp") val stopTimestamp: String? = null
)

data class SeriesEpisode(
    val episodeId: Long,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val containerExtension: String = "mp4",
    val duration: String = "",
    val plot: String = "",
    val thumbnail: String = "",
    val rating: String = ""
)

data class SeriesSeason(
    val seasonNumber: Int,
    val name: String,
    val episodes: List<SeriesEpisode>
)

data class SeriesDetails(
    val seriesId: Long,
    val title: String,
    val coverUrl: String,
    val plot: String,
    val genre: String,
    val cast: String,
    val releaseDate: String,
    val rating: String,
    val seasons: List<SeriesSeason>,
    val tmdbOrImdbId: String = ""
)
