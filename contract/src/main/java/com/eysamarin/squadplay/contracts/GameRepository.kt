package com.eysamarin.squadplay.contracts

interface GameRepository {
    suspend fun getGameThumbnailUrl(gameTitle: String): String?
}
