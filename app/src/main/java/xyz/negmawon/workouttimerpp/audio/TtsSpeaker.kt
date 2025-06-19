package xyz.negmawon.workouttimerpp.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simple singleton wrapper around Android TTS.
 *  * Initialises once per process.
 *  * Non‑blocking .speak() – it ignores calls until ready().
 */
class TtsSpeaker private constructor(ctx: Context) :
    TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(ctx.applicationContext, this)
    private val ready = AtomicBoolean(false)
    private val pending = ArrayDeque<String>()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            ready.set(true)
            while (pending.isNotEmpty()) {
                tts.speak(
                    pending.removeFirst(),
                    TextToSpeech.QUEUE_ADD,
                    null,
                    UUID.randomUUID().toString()
                )
            }
        }
    }

    fun speak(text: String) {
        if (ready.get()) {
            val b = Bundle()
            b.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Workout timer")
        } else {
            pending.addLast(text)
        }
    }

    fun shutdown() = tts.shutdown()

    companion object {
        @Volatile private var INSTANCE: TtsSpeaker? = null
        fun get(ctx: Context): TtsSpeaker =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TtsSpeaker(ctx).also { INSTANCE = it }
            }
    }
}
