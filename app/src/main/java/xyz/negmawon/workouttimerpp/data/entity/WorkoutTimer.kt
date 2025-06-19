package xyz.negmawon.workouttimerpp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.negmawon.workouttimerpp.ui.edit.TimerForm
import java.util.*

@Entity(tableName = "timers")
data class WorkoutTimer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var name: String,
    var warmUp: Int,
    var setDuration: Int,
    var restBetweenReps: Int,
    var restBetweenSets: Int,
    var sets: Int,
    var reps: Int = 3,
    var exerciseNames: List<String> = emptyList(),
    var coolDown: Int
)
