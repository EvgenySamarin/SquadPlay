package com.eysamarin.squadplay.data.datasource

import com.eysamarin.squadplay.contracts.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Serializable
data class SteamGridDbGame(
    val id: Int,
    val name: String
)

@Serializable
data class SteamGridDbSearchResponse(
    val success: Boolean = false,
    val data: List<SteamGridDbGame> = emptyList()
)

@Serializable
data class SteamGridDbGrid(
    val id: Int,
    val url: String? = null,
    val thumb: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class SteamGridDbGridsResponse(
    val success: Boolean = false,
    val data: List<SteamGridDbGrid> = emptyList()
)

class SteamGridDbDataSource(
    private val apiKey: String,
    private val logger: AppLogger,
) {
    private val json = Json { ignoreUnknownKeys = true }

    internal var httpGet: (String, Map<String, String>) -> String? = { urlString, headers ->
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            logger.w(tag = "SteamGridDbDataSource") { "HTTP error ${connection.responseCode} for $urlString" }
            null
        }
    }

    suspend fun getGameCoverThumbnail(gameName: String): String? = withContext(Dispatchers.IO) {
        if (gameName.isBlank() || apiKey.isBlank()) return@withContext null
        try {
            val query = URLEncoder.encode(gameName.trim(), "UTF-8")
            val searchUrl = "https://www.steamgriddb.com/api/v2/search/autocomplete/$query"
            val searchResponse = executeGet<SteamGridDbSearchResponse>(searchUrl)
            val gameId = searchResponse?.data?.firstOrNull()?.id ?: return@withContext null

            // Prioritize vertical covers (600x900, 342x482, 660x930)
            val verticalGridsUrl =
                "https://www.steamgriddb.com/api/v2/grids/game/$gameId?dimensions=600x900,342x482,660x930&nsfw=false"
            val verticalGridsResponse = executeGet<SteamGridDbGridsResponse>(verticalGridsUrl)
            val verticalCover = verticalGridsResponse?.data?.firstOrNull()?.let { it.thumb ?: it.url }
            if (verticalCover != null) return@withContext verticalCover

            // Fallback to any non-NSFW grid
            val anyGridsUrl = "https://www.steamgriddb.com/api/v2/grids/game/$gameId?nsfw=false"
            val anyGridsResponse = executeGet<SteamGridDbGridsResponse>(anyGridsUrl)
            return@withContext anyGridsResponse?.data?.firstOrNull()?.let { it.thumb ?: it.url }
        } catch (e: Exception) {
            logger.e(tag = "SteamGridDbDataSource", throwable = e) { "Failed to get game cover thumbnail" }
        }
        return@withContext null
    }

    private inline fun <reified T> executeGet(urlString: String): T? {
        val headers = mapOf(
            "Authorization" to "Bearer $apiKey",
            "User-Agent" to "SquadPlay/1.0"
        )
        val body = httpGet(urlString, headers) ?: return null
        return json.decodeFromString<T>(body)
    }
}
