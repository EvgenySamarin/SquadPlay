package com.eysamarin.squadplay.domain.game

import com.eysamarin.squadplay.contracts.GameRepository

interface GameProvider {
    suspend fun getGameThumbnailUrl(gameTitle: String): String?
}

class GameProviderImpl(
    private val gameRepository: GameRepository
) : GameProvider {
    override suspend fun getGameThumbnailUrl(gameTitle: String): String? {
        return gameRepository.getGameThumbnailUrl(gameTitle)
    }
}
