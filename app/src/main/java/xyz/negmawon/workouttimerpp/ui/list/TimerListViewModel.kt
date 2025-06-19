package xyz.negmawon.workouttimerpp.ui.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import xyz.negmawon.workouttimerpp.data.repo.TimerRepo

class TimerListViewModel(ctx: Context) : ViewModel() {
    private val repo = TimerRepo.get(ctx)
    val timers = repo.timers                    // Flow<List<WorkoutTimer>>
    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
    fun deleteAll() = viewModelScope.launch { repo.deleteAll() }

    companion object {
        /** Returns a classic ViewModelProvider.Factory that works everywhere. */
        fun factory(ctx: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TimerListViewModel::class.java))
                        return TimerListViewModel(ctx.applicationContext) as T
                    throw IllegalArgumentException("Unknown ViewModel class $modelClass")
                }
            }
    }
}
