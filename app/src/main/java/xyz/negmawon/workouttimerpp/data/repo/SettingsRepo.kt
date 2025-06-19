package xyz.negmawon.workouttimerpp.data.repo

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo.Keys.LAST_TIMER_ID
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo.Keys.LAST_TIMER_TIMESTAMP

private val Context.dataStore by preferencesDataStore("prefs")

class SettingsRepo private constructor(private val ctx: Context) {

    /* preference keys */
    object Keys {
        val ANNOUNCE_NAME        = booleanPreferencesKey("announce_name")
        val ANNOUNCE_REST        = booleanPreferencesKey("announce_rest")
        val BEEP_END_SET         = booleanPreferencesKey("beep_end_set")
        val CHEER_END_WO         = booleanPreferencesKey("cheer_end_wo")
        val DARK_THEME           = booleanPreferencesKey("dark_theme")
        val VIBRATE              = booleanPreferencesKey("vibrate_transitions")
        val WAKE_LOCK            = booleanPreferencesKey("wake_lock")
        val LAST_TIMER_ID        = stringPreferencesKey("last_timer_id")
        val LAST_TIMER_TIMESTAMP = longPreferencesKey("last_timer_started_at")
        val CRASH_REPORTING      = booleanPreferencesKey("crash_reporting")
    }

    /* expose a single flow of Settings data class */
    val flow = ctx.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { p ->
        Settings(
            announceName        = p[Keys.ANNOUNCE_NAME] != false,
            announceRest        = p[Keys.ANNOUNCE_REST] != false,
            beepEndSet          = p[Keys.BEEP_END_SET] != false,
            cheerEndWorkout     = p[Keys.CHEER_END_WO] != false,
            darkTheme           = p[Keys.DARK_THEME] == true,
            vibrateTransitions  = p[Keys.VIBRATE] == true,
            wakeLock            = p[Keys.WAKE_LOCK] == true,
            crashReporting      = p[Keys.CRASH_REPORTING] == true
        )
    }

    suspend fun toggle(key: Preferences.Key<Boolean>) =
        ctx.dataStore.edit { it[key] = !(it[key] ?: false) }

    suspend fun saveLastTimer(id: String) {
        ctx.dataStore.edit {
            it[LAST_TIMER_ID] = id
            it[LAST_TIMER_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun clearLastTimer() {
        ctx.dataStore.edit {
            it.remove(LAST_TIMER_ID)
            it.remove(LAST_TIMER_TIMESTAMP)
        }
    }

    val lastTimerFlow = ctx.dataStore.data.map {
        it[LAST_TIMER_ID] to it[LAST_TIMER_TIMESTAMP]
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: SettingsRepo? = null
        fun get(c: Context) =
            INSTANCE ?: synchronized(this) { INSTANCE ?: SettingsRepo(c).also { INSTANCE = it } }
    }
}

data class Settings(
    val announceName: Boolean = true,
    val announceRest: Boolean = true,
    val beepEndSet: Boolean = true,
    val cheerEndWorkout: Boolean = true,
    val darkTheme: Boolean = false,
    val vibrateTransitions: Boolean = false,
    val wakeLock: Boolean = false,
    val crashReporting: Boolean = true
)
