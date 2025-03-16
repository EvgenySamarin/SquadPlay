package com.eysamarin.squadplay

import android.app.Application
import androidx.credentials.CredentialManager
import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.PollingRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.data.FirebaseAuthManager
import com.eysamarin.squadplay.data.FirebaseAuthManagerImpl
import com.eysamarin.squadplay.data.contract.AuthRepositoryImpl
import com.eysamarin.squadplay.data.contract.PollingRepositoryImpl
import com.eysamarin.squadplay.data.contract.ProfileRepositoryImpl
import com.eysamarin.squadplay.data.datasource.FirebaseDatabaseDataSource
import com.eysamarin.squadplay.data.datasource.FirebaseDatabaseDataSourceImpl
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSourceImpl
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.auth.AuthProviderImpl
import com.eysamarin.squadplay.domain.calendar.CalendarUIProvider
import com.eysamarin.squadplay.domain.calendar.CalendarUIProviderImpl
import com.eysamarin.squadplay.domain.event.GameEventUIProvider
import com.eysamarin.squadplay.domain.event.GameEventUIProviderImpl
import com.eysamarin.squadplay.domain.polling.PollingProvider
import com.eysamarin.squadplay.domain.polling.PollingProviderImpl
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.profile.ProfileProviderImpl
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.main.MainScreenViewModel
import com.eysamarin.squadplay.screens.profile.ProfileScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class SquadPlayApplication : Application() {
    val appModule = module {
        //region data
        single<CredentialManager> { CredentialManager.create(baseContext) }
        single<FirebaseAuthManager> {
            FirebaseAuthManagerImpl(
                firebaseAuth = FirebaseAuth.getInstance(),
                webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
                credentialManager = get(),
                appContext = applicationContext,
            )
        }
        single<FirebaseDatabaseDataSource> {
            FirebaseDatabaseDataSourceImpl(
                firebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
            )
        }
        single {
            FirebaseFirestoreDataSourceImpl(
                firebaseFirestore = FirebaseFirestore.getInstance(),
            )
        }
        //endregion

        //region contracts
        single<AuthRepository> {
            AuthRepositoryImpl(
                firebaseAuthManager = get(),
            )
        }
        single<PollingRepository> { PollingRepositoryImpl(realtimeDatabaseDataSource = get()) }
        single<ProfileRepository> { ProfileRepositoryImpl(firestoreDataSource = get()) }
        //endregion

        //region domain
        single<AuthProvider> { AuthProviderImpl(authRepository = get()) }
        single<CalendarUIProvider> { CalendarUIProviderImpl() }
        single<GameEventUIProvider> { GameEventUIProviderImpl() }
        single<PollingProvider> { PollingProviderImpl(pollingRepository = get()) }
        single<ProfileProvider> {
            ProfileProviderImpl(
                profileRepository = get(),
                authRepository = get(),
            )
        }
        //endregion

        //region presentation
        viewModelOf(::MainScreenViewModel)
        viewModelOf(::AuthScreenViewModel)
        viewModelOf(::ProfileScreenViewModel)
        //endregion
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