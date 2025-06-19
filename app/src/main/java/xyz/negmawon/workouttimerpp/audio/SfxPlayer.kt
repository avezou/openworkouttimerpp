package xyz.negmawon.workouttimerpp.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.negmawon.workouttimerpp.R

/**
 * SfxPlayer class manages the playback of sound effects within the application.
 * It uses SoundPool for efficient handling of multiple sound effects and provides
 * methods to play specific sounds related to workout events (near end, set end, workout end).
 *
 * @param ctx The application context, required for loading sound resources.
 */
class SfxPlayer(ctx: Context) {
    /**
     * The SoundPool instance used to load and play sound effects.
     * It's configured to handle up to 3 simultaneous streams and includes a listener
     * to report errors during sound file loading.
     */
    private val pool = SoundPool.Builder().setMaxStreams(3).build().also {
        it.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) {
                println("Error loading sound effect with ID $sampleId. Error code: $status")
            }
        }
    }

    /**
     * The sound ID for the 'near end' sound effect.
     * If loading fails, it defaults to 0, preventing crashes.
     */
    private val idNearEnd = pool.load(ctx, R.raw.near_end, 1).takeIf { it > 0 } ?: run { println("Could not load R.raw.near_end"); 0 }

    /**
     * The sound ID for the 'set end' sound effect.
     * If loading fails, it defaults to 0, preventing crashes.
     */
    private val idSetEnd = pool.load(ctx, R.raw.set_end, 1).takeIf { it > 0 } ?: run { println("Could not load R.raw.set_end"); 0 }

    /**
     * The sound ID for the 'workout end' sound effect.
     * If loading fails, it defaults to 0, preventing crashes.
     */
    private val idWorkoutEnd = pool.load(ctx, R.raw.workout_end, 1).takeIf { it > 0 } ?: run { println("Could not load R.raw.workout_end"); 0 }

    /**
     * Plays the 'near end' sound effect.
     * This function is a suspending function and should be called within a coroutine.
     */
    suspend fun nearEnd() = play(idNearEnd)

    /**
     * Plays the 'set end' sound effect.
     * This function is a suspending function and should be called within a coroutine.
     */
    suspend fun setEnd() = play(idSetEnd)

    /**
     * Plays the 'workout end' sound effect.
     * This function is a suspending function and should be called within a coroutine.
     */
    suspend fun workoutEnd() = play(idWorkoutEnd)

    /**
     * Plays a sound effect given its resource ID.
     * If the sound ID is valid, it plays the sound with specific volume settings.
     * If the sound ID is invalid (0), it does nothing.
     *
     * @param id The resource ID of the sound effect to play.
     */
    @SuppressLint("ResourceType")
    private suspend fun play(@RawRes id: Int) = withContext(Dispatchers.Default) {
        if (id > 0) {
            pool.play(id, /*L*/0.35f, /*R*/0.5f, /*pri*/1, /*loop*/0, /*rate*/1f)
        }
    }

    
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(ctx: Context, millis: Long = 100) {
        val vib = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vib?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    }


    /**
     * Releases the resources held by the SoundPool when the SfxPlayer is no longer needed.
     * This should be called to prevent resource leaks.
     */
    fun shutDown() = pool.release()
}