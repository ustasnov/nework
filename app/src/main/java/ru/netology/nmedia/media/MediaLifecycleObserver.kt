package ru.netology.nmedia.media

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class MediaLifecycleObserver : LifecycleEventObserver {
    var mediaPlayer: MediaPlayer? = MediaPlayer()
    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false
    val duration: Int
        get() = mediaPlayer?.duration ?: 0
    val currentPosition
        get() = mediaPlayer?.currentPosition ?: -1

    fun playPrepared() {
        mediaPlayer?.setOnPreparedListener {
            it.start()
        }
        mediaPlayer?.prepareAsync()
    }

    fun play() {
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> if (mediaPlayer?.isPlaying ?: false) {
                mediaPlayer?.pause()
            }

            Lifecycle.Event.ON_STOP -> {
                mediaPlayer?.release()
                mediaPlayer = null
            }

            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> Unit
        }
    }
}
