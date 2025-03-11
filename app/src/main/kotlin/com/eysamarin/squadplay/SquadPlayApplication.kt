package com.eysamarin.squadplay

import android.app.Application
import com.eysamarin.squadplay.domain.CalendarUIProvider
import com.eysamarin.squadplay.domain.CalendarUIProviderImpl
import com.eysamarin.squadplay.domain.GameEventUIProvider
import com.eysamarin.squadplay.domain.GameEventUIProviderImpl
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.main.MainScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class SquadPlayApplication : Application() {
    val appModule = module {
        single<CalendarUIProvider> { CalendarUIProviderImpl() }
        single<GameEventUIProvider> { GameEventUIProviderImpl() }
        viewModelOf(::MainScreenViewModel)
        viewModelOf(::AuthScreenViewModel)
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SquadPlayApplication)
            modules(appModule)
        }
    }
}