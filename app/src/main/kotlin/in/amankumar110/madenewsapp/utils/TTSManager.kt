package `in`.amankumar110.madenewsapp.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private val pendingQueue = mutableListOf<Pair<String, String>>()
    private var lastListener: UtteranceProgressListener? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.language = Locale.US
            lastListener?.let { tts?.setOnUtteranceProgressListener(it) }

            pendingQueue.forEach { (text, id) -> speak(text, id) }
            pendingQueue.clear()
        }
    }

    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (tts == null) {
            // Shouldn't happen normally, but reinit just in case
            tts = TextToSpeech(context, this)
            pendingQueue.add(Pair(text, utteranceId))
            return
        }

        if (isReady) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } else {
            pendingQueue.add(Pair(text, utteranceId))
        }
    }

    fun setListener(listener: UtteranceProgressListener) {
        lastListener = listener
        if (isReady) {
            tts?.setOnUtteranceProgressListener(listener)
        }
    }

    fun stop() {
        tts?.stop()
    }

    // Optional manual cleanup (not needed unless low on memory)
    fun shutdown() {
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
