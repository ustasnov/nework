package ru.netology.nmedia

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentPostAttachmentBinding
import ru.netology.nmedia.media.MediaLifecycleObserver
import ru.netology.nmedia.utils.StringArg
import java.io.IOException
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class PostAttachmentFragment : Fragment() {
    private val observer = MediaLifecycleObserver()
    private var prepared = false
    private var timer: Timer? = null
    private var playMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostAttachmentBinding.inflate(inflater, container, false)

        lifecycle.addObserver(observer)

        binding.photo.visibility = View.GONE
        binding.audioGroup.visibility = View.GONE

        when (requireArguments().typeArg) {
            "image" -> seePicture(binding, requireNotNull(requireArguments().urlArg))
            "audio" -> listenToAudio(binding, requireNotNull(requireArguments().urlArg))
            "video" -> watchVideo(binding, requireNotNull(requireArguments().urlArg))
        }

        binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.playAudio.setOnClickListener {
            if (!playMode) {
                try {
                    binding.playAudio.setImageResource(R.drawable.ic_pause_audio_24)
                    if (prepared)
                        observer.play()
                    else {
                        observer.playPrepared()
                        prepared = true
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                setProgress(binding, observer)
            } else {
                playMode = false
                binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)
                observer.pause()
            }
        }

        return binding.root
    }

    private fun setProgress(
        binding: FragmentPostAttachmentBinding,
        mediaObserver: MediaLifecycleObserver
    ) {
        timer = Timer()
        timer?.scheduleAtFixedRate(
            timerTask {
                Handler(Looper.getMainLooper()).post {
                    val isPlaying = mediaObserver.isPlaying
                    if (isPlaying) {
                        playMode = true
                        binding.audioSlider.valueTo = mediaObserver.duration.toFloat()
                        binding.audioSlider.value = mediaObserver.currentPosition.toFloat()
                        binding.curTime.text = convertToMMSS(mediaObserver.currentPosition)
                        binding.duration.text = convertToMMSS(mediaObserver.duration)
                    } else if (playMode) {
                        playMode = false
                        binding.audioSlider.value = 0f
                        binding.curTime.text = "00:00"
                        binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)
                        timer?.cancel()
                        timer = null
                    }
                }
            }, 1000, 1000
        )
    }

    private fun listenToAudio(binding: FragmentPostAttachmentBinding, url: String) {
        binding.photo.visibility = View.GONE
        binding.audioGroup.visibility = View.VISIBLE
        observer.mediaPlayer?.setDataSource(url)
    }

    private fun watchVideo(binding: FragmentPostAttachmentBinding, url: String) {
        binding.photo.visibility = View.GONE
        binding.audioGroup.visibility = View.GONE
    }

    private fun seePicture(binding: FragmentPostAttachmentBinding, url: String) {
        binding.photo.visibility = View.VISIBLE
        binding.audioGroup.visibility = View.GONE

        Glide.with(binding.photo)
            .load(url)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .into(binding.photo)
    }

    override fun onStop() {
        super.onStop()
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    companion object {
        var Bundle.urlArg: String? by StringArg
        var Bundle.typeArg: String? by StringArg

        fun convertToMMSS(duration: Int): String {
            return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration.toLong()) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % TimeUnit.MINUTES.toSeconds(1)
            )
        }
    }
}
