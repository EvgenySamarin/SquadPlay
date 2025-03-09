package com.eysamarin.squadplay.domain

import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.GameEventUI
import com.eysamarin.squadplay.models.PREVIEW_GAME_EVENTS

fun interface GameEventUIProvider {
    fun provideGameEventUIBy(date: CalendarUI.Date): List<GameEventUI>
}

class GameEventUIProviderImpl: GameEventUIProvider {

    override fun provideGameEventUIBy(date: CalendarUI.Date): List<GameEventUI> {
        return PREVIEW_GAME_EVENTS
    }
}
