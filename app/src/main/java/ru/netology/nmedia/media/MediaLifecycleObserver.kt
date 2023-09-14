package ru.netology.nmedia.media

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class MediaLifecycleObserver: LifecycleEventObserver {

    var mediaPlayer: MediaPlayer? = MediaPlayer()

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

    fun stop() {
        mediaPlayer?.stop()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> mediaPlayer?.pause()
            Lifecycle.Event.ON_STOP -> {
                mediaPlayer?.release()
                mediaPlayer = null
            }
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> Unit
        }
    }


}