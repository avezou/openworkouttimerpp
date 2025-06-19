package xyz.negmawon.workouttimerpp.engine

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.os.PowerManager
import android.content.Context
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.negmawon.workouttimerpp.audio.SfxPlayer
import xyz.negmawon.workouttimerpp.audio.TtsSpeaker
import xyz.negmawon.workouttimerpp.data.entity.WorkoutTimer
import xyz.negmawon.workouttimerpp.data.repo.Settings
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo
import xyz.negmawon.workouttimerpp.ui.common.Defaults

class TimerEngine(app: Application) : AndroidViewModel(app) {

    private var prefs: Settings? = null

    private val WAKE_TAG = "WorkoutTimer:TimerWake"

    @SuppressLint("StaticFieldLeak")
    private val context = app.applicationContext

    init {
        SettingsRepo.get(app).flow
            .onEach { preferences ->
                prefs = preferences
            }
            .launchIn(viewModelScope)
    }

    private var wakeLock: PowerManager.WakeLock? = null

    private fun acquireWakeLock() {
        if ((wakeLock == null || wakeLock?.isHeld == false)) {
            val pm = getApplication<Application>().getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PARTIAL_WAKE_LOCK, WAKE_TAG).apply {
                acquire(2*60*60*1000L /* 2 hours max*/)
            }
        }
    }
    private fun releaseWakeLock() {
        wakeLock?.takeIf {
            it.isHeld
        }?.release()
    }

    /* ---------- public UI state ---------- */
    data class UiState(
        val timer: WorkoutTimer? = null,
        val section: Section = Section.DONE,
        val setIndex: Int = 0,     // 1‑based
        val repIndex: Int = 0,     // 1‑based
        val remaining: Int = 0,
        val summary: String? = null,
        val paused: Boolean = false
    )
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    /* ---------- internals ---------- */
    private val tts = TtsSpeaker.get(app)
    private val audio = SfxPlayer(app)
    private var job: Job? = null

    override fun onCleared() { audio.shutDown(); tts.shutdown(); super.onCleared() }

    /* ---------- external commands ---------- */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun start(t: WorkoutTimer) {
        acquireWakeLock()
        viewModelScope.launch {
            SettingsRepo.get(context).saveLastTimer(t.id)
        }
        if (prefs?.vibrateTransitions == true) audio.vibrate(getApplication())
        job?.cancel()
        announce("Warm‑up")
        _state.value = UiState(
            timer     = t,
            section   = Section.WARM_UP,
            setIndex  = 0,
            repIndex  = 0,
            remaining = t.warmUp,
            summary   = null,
            paused    = false)
        tick()
    }

    fun pause()  {
        job?.cancel()
        _state.update { it.copy(paused = true) }
    }
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun resume() {
        _state.update { it.copy(paused = false) }
        tick()
    }
    fun stop()   {
        job?.cancel()
        releaseWakeLock()
        _state.value = _state.value.copy(section = Section.DONE)
        viewModelScope.launch {
            SettingsRepo.get(context).clearLastTimer()
        }
    }
    fun clear() {
        job?.cancel()
        _state.value = UiState()
    }

    /* ---------- tick loop ---------- */
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun tick() {
        job?.cancel()
        job = viewModelScope.launch {
            while (true) {
                delay(1_000L)

                val s = _state.value
                if (s.paused) continue

                val newRemaining = s.remaining - 1

                // Play near-end beep if we're exactly 5 seconds from end
                if (newRemaining == 5) {
                    audio.nearEnd()
                }

                // Update state
                if (newRemaining > 0) {
                    _state.value = s.copy(remaining = newRemaining)
                } else {
                    if (prefs == null || prefs?.beepEndSet == true) {
                        audio.setEnd()
                    }
                    advance()
                }
            }
        }
    }


    /* ---------- section / rep / set transitions ---------- */
    @RequiresPermission(Manifest.permission.VIBRATE)
    private suspend fun advance() {
        val s = _state.value;
        val t = s.timer ?: return
        var sec = s.section
        var set = s.setIndex
        var rep = s.repIndex
        var rem = s.remaining

        when (sec) {
            Section.WARM_UP -> {
                if (prefs == null || prefs?.beepEndSet == true) audio.setEnd()
                sec = Section.WORKOUT; set = Defaults.MIN_SETS; rep = Defaults.MIN_REPS; rem = t.setDuration
                if (prefs == null || prefs?.announceName == true) announce("Starting workout. Set 1 ${name(t,0)}. Rep 1")
                if (prefs == null || prefs?.vibrateTransitions == true) audio.vibrate(getApplication())
            }
            Section.WORKOUT -> {
                if (rep < t.reps) {
                    // rest between reps
                    sec = Section.REST_REP; rem = t.restBetweenReps
                    if (prefs == null || prefs?.announceRest == true) announce("Rest for ${t.restBetweenReps} seconds")
                } else {
                    // finished last rep of set
                    sec = Section.REST_SET; rem = t.restBetweenSets
                    if (prefs == null || prefs?.announceRest == true) announce("Next set in ${t.restBetweenSets} seconds")
                }
            }
            Section.REST_REP -> {                         // back to next rep
                rep++
                sec = Section.WORKOUT; rem = t.setDuration
                announce(if (rep == t.reps) "Last rep" else "Rep $rep")
            }
            Section.REST_SET -> {
                if (set < t.sets) {
                    set++
                    rep = 1
                    sec = Section.WORKOUT; rem = t.setDuration
                    val title = name(t, set - 1)
                    announce("Set $set $title. Rep 1")
                } else {
                    sec = Section.COOL_DOWN; rem = t.coolDown
                    announce("Cool‑down for ${t.coolDown / 60} min ${t.coolDown % 60} sec")
                }
            }
            Section.COOL_DOWN -> {
                // workout finished
                if (prefs == null || prefs?.cheerEndWorkout == true) audio.workoutEnd()
                announce("Workout completed")
                val summary = buildSummary(t)
                _state.value = UiState(
                    timer = t,
                    section = Section.DONE,
                    setIndex = set,
                    repIndex = rep,
                    remaining = 0,
                    summary = summary,
                    paused = false
                )
                job?.cancel(); return
            }
            Section.DONE -> return
        }
        _state.value = UiState(
            timer = t,
            section = sec,
            setIndex = set,
            repIndex = rep,
            remaining = rem,
            summary = null,
            paused = false
        )
        tick()
    }

    /* ---------- small helpers ---------- */
    private fun announce(text: String) = tts.speak(text)
    private fun name(t: WorkoutTimer, i: Int) =
        t.exerciseNames.getOrElse(i) { "Exercise ${i+1}" }

    private fun buildSummary(t: WorkoutTimer): String = buildString {
        // compute total workout seconds
        var total = t.warmUp
        for (i in Defaults.MIN_SETS..t.sets) {
            total += t.setDuration * t.reps
            total += t.restBetweenReps * (t.reps - 1)
            if (i < t.sets) total += t.restBetweenSets
        }
        total += t.coolDown
        val mm = total / 60
        val ss = total % 60
        return buildString {
            appendLine("Workout summary:")
            appendLine("Total time: %02d:%02d".format(mm, ss))
            appendLine("Warm‑up: ${t.warmUp}s")
            t.exerciseNames.forEachIndexed { idx, name ->
                appendLine("Set ${idx+1} $name → ${t.reps} reps")
            }
            appendLine("Cool‑down: ${t.coolDown}s")
        }
    }
}
