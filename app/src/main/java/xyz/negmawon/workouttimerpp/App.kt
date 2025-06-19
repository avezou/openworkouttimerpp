package xyz.negmawon.workouttimerpp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        // Read DataStore crash toggle
        CoroutineScope(Dispatchers.IO).launch {
            val settingsRepo = SettingsRepo.get(this@App)
            val enabled = settingsRepo.flow.first().crashReporting
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
        }
    }
}

