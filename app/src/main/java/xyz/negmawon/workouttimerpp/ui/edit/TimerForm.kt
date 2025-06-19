package xyz.negmawon.workouttimerpp.ui.edit

import xyz.negmawon.workouttimerpp.ui.common.Defaults

data class TimerForm(
    val id: String = "",
    var name: String = "",
    var warmUp: Int = Defaults.DEFAULT_WARMUP,
    var setDuration: Int = Defaults.SET_DURATION,
    var restReps: Int = Defaults.REST_BETWEEN_REPS,
    var restSets: Int = Defaults.REST_BETWEEN_SETS,
    var sets: Int = 6,
    var reps: Int = 3,
    var exerciseNames: String = "",
    var coolDown: Int = Defaults.DEFAULT_COOLDOWN
)
