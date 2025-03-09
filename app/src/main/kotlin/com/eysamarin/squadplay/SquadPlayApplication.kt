package com.eysamarin.squadplay

import android.app.Application
import com.eysamarin.squadplay.screens.main_screen.MainScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class SquadPlayApplication : Application() {
    val appModule = module {
        viewModelOf(::MainScreenViewModel)
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