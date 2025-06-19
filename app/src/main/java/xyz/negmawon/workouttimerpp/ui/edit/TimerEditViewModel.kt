package xyz.negmawon.workouttimerpp.ui.edit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.negmawon.workouttimerpp.data.entity.WorkoutTimer
import java.util.*
import xyz.negmawon.workouttimerpp.data.repo.TimerRepo

class TimeEditViewModel(
    ctx: Context,
    private val timerId: String          // "new" or existing UUID
) : ViewModel() {

    private val repo = TimerRepo.get(ctx)
    private val _form = MutableStateFlow(TimerForm(id = timerId))
    val form: StateFlow<TimerForm> = _form

    init {
        if (timerId != "new") {
            viewModelScope.launch {
                repo.timers.collect { list ->
                    list.find { it.id == timerId }?.let { w ->
                        _form.value = w.toForm()
                    }
                }
            }
        }
    }

    /* ---------- field setters ---------- */
    fun setName(v: String)          = _form.update { it.copy(name = v) }
    fun setWarmUp(v: Int)           = _form.update { it.copy(warmUp = v) }
    fun setSetDuration(v: Int)      = _form.update { it.copy(setDuration = v) }
    fun setRestReps(v: Int)         = _form.update { it.copy(restReps = v) }
    fun setRestSets(v: Int)         = _form.update { it.copy(restSets = v) }

    fun setReps(v: Int)             = _form.update { it.copy(reps = v) }
    fun setExerciseNames(v: String) = _form.update { it.copy(exerciseNames = v) }
    fun setCoolDown(v: Int)         = _form.update { it.copy(coolDown = v) }

    fun setSets(count: Int) {
        val c     = count.coerceIn(1, 50)
        val cur   = _form.value

        /* current list → mutable list */
        val names = cur.exerciseNames
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()

        /* expand / shrink list */
        when {
            c > names.size -> repeat(c - names.size) { names.add("") }
            c < names.size -> names.subList(c, names.size).clear()
        }

        /* build string **without** trailing empty entries */
        val nameString =
            names.filter { it.isNotBlank() }
                .joinToString(", ")
        _form.update { it.copy(sets = c, exerciseNames = nameString) }
    }

    /* ---------- actions ---------- */
    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val f = _form.value
        Log.d("Avezou", "Form value: ${_form.value.exerciseNames}")
        repo.save(
            WorkoutTimer(
                id               = if (f.id == "new") UUID.randomUUID().toString() else f.id,
                name             = f.name.trim(),
                warmUp           = f.warmUp,
                setDuration      = f.setDuration,
                restBetweenReps  = f.restReps,
                restBetweenSets  = f.restSets,
                sets             = f.sets,
                reps             = f.reps,
                exerciseNames    = f.exerciseNames.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                coolDown         = f.coolDown
            )
        )
        onDone()
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        repo.delete(_form.value.id)
        onDone()
    }

    /* ---------- factory (no‑Hilt) ---------- */
    companion object {
        fun factory(ctx: Context, timerId: String) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TimeEditViewModel(ctx.applicationContext, timerId) as T
            }
    }
}

/* Helper to map entity → form */
private fun WorkoutTimer.toForm() = TimerForm(
    id, name, warmUp, setDuration, restBetweenReps, restBetweenSets, sets, reps, exerciseNames.joinToString(","), coolDown
)
