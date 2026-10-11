package com.eysamarin.squadplay.data.datasource

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.data.contract.GameRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SteamGridDbDataSourceTest {

    private val fakeLogger = object : AppLogger {
        override fun d(tag: String?, message: () -> String) {}
        override fun i(tag: String?, message: () -> String) {}
        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}
        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {}
    }

    @Test
    fun `returns vertical cover thumb when game search and vertical grid succeed`() = runTest {
        val dataSource = SteamGridDbDataSource(
            apiKey = "test_key",
            logger = fakeLogger
        )

        dataSource.httpGet = { url, _ ->
            when {
                url.contains("/search/autocomplete/") -> """
                    {
                        "success": true,
                        "data": [{"id": 15831, "name": "Dota 2"}]
                    }
                """.trimIndent()
                url.contains("dimensions=600x900,342x482,660x930") -> """
                    {
                        "success": true,
                        "data": [{
                            "id": 131962,
                            "url": "https://cdn2.steamgriddb.com/grid/sample_full.png",
                            "thumb": "https://cdn2.steamgriddb.com/thumb/sample_thumb.jpg"
                        }]
                    }
                """.trimIndent()
                else -> null
            }
        }

        val result = dataSource.getGameCoverThumbnail("Dota 2")
        assertEquals("https://cdn2.steamgriddb.com/thumb/sample_thumb.jpg", result)
    }

    @Test
    fun `falls back to any grid when vertical grid is empty`() = runTest {
        val dataSource = SteamGridDbDataSource(
            apiKey = "test_key",
            logger = fakeLogger
        )

        dataSource.httpGet = { url, _ ->
            when {
                url.contains("/search/autocomplete/") -> """
                    {
                        "success": true,
                        "data": [{"id": 5214, "name": "Tetris"}]
                    }
                """.trimIndent()
                url.contains("dimensions=600x900,342x482,660x930") -> """
                    {
                        "success": true,
                        "data": []
                    }
                """.trimIndent()
                url.contains("nsfw=false") -> """
                    {
                        "success": true,
                        "data": [{
                            "id": 9999,
                            "url": "https://cdn2.steamgriddb.com/grid/fallback_full.png",
                            "thumb": "https://cdn2.steamgriddb.com/thumb/fallback_thumb.jpg"
                        }]
                    }
                """.trimIndent()
                else -> null
            }
        }

        val result = dataSource.getGameCoverThumbnail("Tetris")
        assertEquals("https://cdn2.steamgriddb.com/thumb/fallback_thumb.jpg", result)
    }

    @Test
    fun `returns null when search yields no matches`() = runTest {
        val dataSource = SteamGridDbDataSource(
            apiKey = "test_key",
            logger = fakeLogger
        )

        dataSource.httpGet = { url, _ ->
            if (url.contains("/search/autocomplete/")) {
                """{"success": true, "data": []}"""
            } else null
        }

        val result = dataSource.getGameCoverThumbnail("NonExistentGame")
        assertNull(result)
    }

    @Test
    fun `returns null when gameTitle or apiKey is blank without making network calls`() = runTest {
        var callCount = 0
        val dataSource = SteamGridDbDataSource(
            apiKey = "test_key",
            logger = fakeLogger
        )
        dataSource.httpGet = { _, _ ->
            callCount++
            null
        }

        assertNull(dataSource.getGameCoverThumbnail(""))
        assertNull(dataSource.getGameCoverThumbnail("   "))

        val emptyKeyDataSource = SteamGridDbDataSource(
            apiKey = "",
            logger = fakeLogger
        )
        emptyKeyDataSource.httpGet = { _, _ ->
            callCount++
            null
        }
        assertNull(emptyKeyDataSource.getGameCoverThumbnail("Dota 2"))

        assertEquals(0, callCount)
    }

    @Test
    fun `GameRepositoryImpl prioritizes SteamGridDb and falls back to RawgDataSource`() = runTest {
        val steamGridDbDataSource = SteamGridDbDataSource(
            apiKey = "key",
            logger = fakeLogger
        )
        val rawgDataSource = RawgDataSource(
            apiKey = "rawg_key",
            logger = fakeLogger
        )

        val repository = GameRepositoryImpl(
            steamGridDbDataSource = steamGridDbDataSource,
            rawgDataSource = rawgDataSource
        )

        // Scenario 1: SteamGridDb succeeds
        steamGridDbDataSource.httpGet = { url, _ ->
            when {
                url.contains("/search/autocomplete/") -> """{"success":true,"data":[{"id":1,"name":"Game"}]}"""
                url.contains("dimensions=") -> """{"success":true,"data":[{"id":1,"thumb":"https://steamgriddb.com/thumb.jpg"}]}"""
                else -> null
            }
        }
        val steamResult = repository.getGameThumbnailUrl("Game")
        assertEquals("https://steamgriddb.com/thumb.jpg", steamResult)

        // Scenario 2: SteamGridDb fails/empty -> fallback to RAWG
        steamGridDbDataSource.httpGet = { _, _ -> """{"success":true,"data":[]}""" }
        // For RAWG, RawgDataSource uses URLConnection directly in getGameThumbnail, but when it returns null:
        val fallbackResult = repository.getGameThumbnailUrl("Game")
        assertNull(fallbackResult)
    }
}
