package xyz.negmawon.workouttimerpp.data.repo

import android.content.Context
import kotlinx.coroutines.flow.Flow
import xyz.negmawon.workouttimerpp.data.WorkoutTimerDb
import xyz.negmawon.workouttimerpp.data.entity.WorkoutTimer

class TimerRepo private constructor(ctx: Context) {
    private val dao = WorkoutTimerDb.get(ctx).dao()
    val timers: Flow<List<WorkoutTimer>> = dao.all()
    suspend fun save(t: WorkoutTimer) =
        if (dao.all().equals(t)) dao.update(t) else dao.insert(t)
    suspend fun delete(id: String) = dao.delete(id)
    suspend fun deleteAll() = dao.deleteAll()
    companion object {
        @Volatile private var INSTANCE: TimerRepo? = null
        fun get(ctx: Context) =
            INSTANCE ?: synchronized(this) { INSTANCE ?: TimerRepo(ctx).also { INSTANCE = it } }
    }
}
