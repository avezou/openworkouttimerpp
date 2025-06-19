package xyz.negmawon.workouttimerpp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.negmawon.workouttimerpp.data.converters.StringListConverter
import xyz.negmawon.workouttimerpp.data.dao.TimerDao
import xyz.negmawon.workouttimerpp.data.entity.WorkoutTimer

@Database(entities = [WorkoutTimer::class], version = 1)
@TypeConverters(StringListConverter::class)
abstract class WorkoutTimerDb : RoomDatabase() {
    abstract fun dao(): TimerDao
    companion object {
        @Volatile private var INSTANCE: WorkoutTimerDb? = null
        fun get(context: Context): WorkoutTimerDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutTimerDb::class.java, "workout_timer.db"
                ).build().also { INSTANCE = it }
            }
    }
}
