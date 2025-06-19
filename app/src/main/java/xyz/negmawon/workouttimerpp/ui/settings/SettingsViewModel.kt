package xyz.negmawon.workouttimerpp.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo

class SettingsViewModel(ctx: Context) : ViewModel() {
    private val repo = SettingsRepo.get(ctx)
    val prefsFlow = repo.flow
    fun toggle(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>) =
        viewModelScope.launch { repo.toggle(key) }

    companion object {
        fun factory(ctx: Context) : ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java))
                        return SettingsViewModel(ctx.applicationContext) as T
                    throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }

}
