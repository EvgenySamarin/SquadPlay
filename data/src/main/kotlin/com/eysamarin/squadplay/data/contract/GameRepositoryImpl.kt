package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.GameRepository
import com.eysamarin.squadplay.data.datasource.RawgDataSource
import com.eysamarin.squadplay.data.datasource.SteamGridDbDataSource

class GameRepositoryImpl(
    private val steamGridDbDataSource: SteamGridDbDataSource,
    private val rawgDataSource: RawgDataSource? = null,
): GameRepository {
    override suspend fun getGameThumbnailUrl(gameTitle: String): String? {
        val cover = steamGridDbDataSource.getGameCoverThumbnail(gameTitle)
        if (cover != null) return cover
        return rawgDataSource?.getGameThumbnail(gameTitle)
    }
}
