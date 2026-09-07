package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.GameRepository
import com.eysamarin.squadplay.data.datasource.RawgDataSource

class GameRepositoryImpl(
    private val rawgDataSource: RawgDataSource
): GameRepository {
    override suspend fun getGameThumbnailUrl(gameTitle: String): String? {
        return rawgDataSource.getGameThumbnail(gameTitle)
    }
}
