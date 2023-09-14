package ru.netology.nmedia

import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentPostAttachmentBinding
import ru.netology.nmedia.media.MediaLifecycleObserver
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import java.util.Timer
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class PostAttachmentFragment : Fragment() {
    val viewModel: PostViewModel by activityViewModels()
    private val observer = MediaLifecycleObserver()
    var prepared = false
    var timer: Timer? = null
    var playMode = false; // проигрывание на паузе или завершено активна кнопка play

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
                binding.playAudio.setImageResource(R.drawable.ic_pause_audio_24)
                if (prepared)
                    observer.play()
                else {
                    observer.playPrepared()
                    prepared = true
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

    private fun setProgress(binding: FragmentPostAttachmentBinding, mediaObserver: MediaLifecycleObserver) {
        timer = Timer()
        timer?.scheduleAtFixedRate(
            timerTask() {
                Handler(Looper.getMainLooper()).post {
                    var isPlaying = mediaObserver.mediaPlayer?.isPlaying() ?: false
                    if (isPlaying) {
                        playMode = true
                        val duration = mediaObserver.mediaPlayer?.duration ?: 100
                        val currentPosition = mediaObserver.mediaPlayer?.currentPosition ?: 0
                        binding.audioSlider.valueTo = duration.toFloat();
                        binding.audioSlider.value = currentPosition.toFloat()
                    } else if (playMode) {
                        playMode = false
                        binding.audioSlider.value = 0f
                        binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)
                        timer?.cancel()
                        timer = null
                    }
                }
            }, 1000, 1000
        )
    }

    fun listenToAudio(binding: FragmentPostAttachmentBinding, url: String) {
        if (binding != null) {
            binding.photo.visibility = View.GONE
            binding.audioGroup.visibility = View.VISIBLE
            observer.mediaPlayer?.setDataSource(requireArguments().urlArg)
        }
    }

    fun watchVideo(binding: FragmentPostAttachmentBinding, url: String) {
        if (binding != null) {
            binding.photo.visibility = View.GONE
            binding.audioGroup.visibility = View.GONE
        }
    }

    fun seePicture(binding: FragmentPostAttachmentBinding, url: String) {
        if (binding != null) {
            binding.photo.visibility = View.VISIBLE
            binding.audioGroup.visibility = View.GONE

            Glide.with(binding.photo)
                .load(url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.photo)
        }
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
    }
}
