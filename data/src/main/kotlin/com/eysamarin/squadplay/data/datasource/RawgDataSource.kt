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
data class RawgGameResult(
    val id: Int,
    val name: String,
    val background_image: String? = null
)

@Serializable
data class RawgResponse(
    val results: List<RawgGameResult>
)

class RawgDataSource(
    private val apiKey: String,
    private val logger: AppLogger,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getGameThumbnail(gameName: String): String? = withContext(Dispatchers.IO) {
        if (gameName.isBlank() || apiKey.isBlank()) return@withContext null
        try {
            val query = URLEncoder.encode(gameName, "UTF-8")
            val urlString = "https://api.rawg.io/api/games?search=$query&key=$apiKey&page_size=1"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<RawgResponse>(responseText)
                return@withContext response.results.firstOrNull()?.background_image
            }
        } catch (e: Exception) {
            logger.e(tag = "RawgDataSource", throwable = e) { "Failed to get game thumbnail" }
        }
        return@withContext null
    }
}
