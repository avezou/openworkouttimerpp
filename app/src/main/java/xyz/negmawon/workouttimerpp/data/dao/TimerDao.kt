package xyz.negmawon.workouttimerpp.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.negmawon.workouttimerpp.data.entity.WorkoutTimer

@Dao interface TimerDao {
    @Query("SELECT * FROM timers") fun all(): Flow<List<WorkoutTimer>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(t: WorkoutTimer)
    @Update suspend fun update(t: WorkoutTimer)
    @Query("DELETE FROM timers WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM timers") suspend fun deleteAll()
}