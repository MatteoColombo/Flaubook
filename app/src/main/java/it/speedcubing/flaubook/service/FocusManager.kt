package it.speedcubing.flaubook.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

class FocusManager(context: Context, player: FlaubookPlayer) {

    private val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val focusLock = Object()
    private var playOnGain = false
    private var hasFocus = false
    private lateinit var currentRequest: AudioFocusRequest
    private val callback = player as FocusCallback
    
    fun requestFocusAndPlay(): Boolean {
        synchronized(focusLock) {
            if (hasFocus) {
                callback.focusPlay()
                return true
            }
            currentRequest = createFocusRequest()
            val res = am.requestAudioFocus(currentRequest)
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                hasFocus = true
                callback.focusPlay()
                return true
            } else
                return false
        }
    }

    fun abandonFocus() {
        synchronized(focusLock) {
            am.abandonAudioFocusRequest(currentRequest)
            hasFocus = false
            playOnGain = false
            callback.focusPause()
        }
    }

    private fun createFocusRequest(): AudioFocusRequest {
        return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setOnAudioFocusChangeListener(FocusChangeListener())
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                build()
            })
            setWillPauseWhenDucked(true)
            setAcceptsDelayedFocusGain(false)
            build()
        }
    }

    private inner class FocusChangeListener : AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> synchronized(focusLock) {
                    if (playOnGain) {
                        callback.focusPlay()
                        playOnGain = false
                        hasFocus = true
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> synchronized(focusLock) {
                    am.abandonAudioFocusRequest(currentRequest)
                    playOnGain = false
                    hasFocus = false
                    callback.focusPause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    callback.focusPause()
                    playOnGain = true
                    hasFocus = false
                }
            }
        }
    }

    interface FocusCallback {
        fun focusPlay()

        fun focusPause()
    }
}