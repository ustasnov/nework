package ru.netology.nmedia

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentPostAttachmentBinding
import ru.netology.nmedia.media.MediaLifecycleObserver
import ru.netology.nmedia.utils.AndroidUtils.formatDate
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
    //private var mp: MediaPlayer? = null
    //private var mediaController: MediaController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostAttachmentBinding.inflate(inflater, container, false)

        lifecycle.addObserver(observer)

        val dateTime = formatDate(requireNotNull(requireArguments().publishedArg))
        binding.mediaPublishedBy.text = String.format(
            getString(R.string.published_label),
            requireNotNull(requireArguments().autorArg)
        )
        binding.mediaPublishedWhen.text = dateTime
        binding.photo.visibility = View.GONE
        binding.audioGroup.visibility = View.GONE
        binding.video.visibility = View.GONE

        val url = requireNotNull(requireArguments().urlArg)
        val attachmentType = requireNotNull(requireArguments().typeArg)
        when (attachmentType) {
            "image" -> seePicture(binding, url)
            "audio" -> listenToAudio(binding, url)
            "video" -> watchVideo(binding, url)
        }

        binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.playAudio.setOnClickListener {
            when (attachmentType) {
                "audio" -> if (!playMode) {
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
                    setAudioProgress(binding, observer)
                } else {
                    playMode = false
                    binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)
                    observer.pause()
                }

                "video" -> {
                    if (playMode) {
                        //mp?.pause()
                        binding.video.pause()
                        playMode = false
                        binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)
                    } else {
                        binding.photo.visibility = View.GONE
                        binding.video.visibility = View.VISIBLE
                        //mp?.start()
                        binding.video.start()
                        setVideoProgress(binding, binding.video)
                        binding.playAudio.setImageResource(R.drawable.ic_pause_audio_24)
                    }
                }

                else -> Unit
            }
        }

        return binding.root
    }

    private fun setAudioProgress(
        binding: FragmentPostAttachmentBinding,
        mediaObserver: MediaLifecycleObserver
    ) {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }

        timer = Timer()
        timer?.scheduleAtFixedRate(
            timerTask {
                Handler(Looper.getMainLooper()).post {
                    val isPlaying = mediaObserver.isPlaying
                    if (isPlaying) {
                        playMode = true
                        binding.audioSlider.valueTo = mediaObserver.duration.toFloat()
                        binding.audioSlider.value = mediaObserver.currentPosition.toFloat()
                        println("Current position: ${mediaObserver.currentPosition}")
                        binding.curTime.text = convertToMMSS(mediaObserver.currentPosition)
                        binding.duration.text = convertToMMSS(mediaObserver.duration)
                    } else if (playMode) {
                        playMode = false
                        binding.audioSlider.value = 0f
                        binding.curTime.text = getString(R.string.start_time)
                        binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)
                        timer?.cancel()
                        timer = null
                    }
                }
            }, 1000, 1000
        )
    }

    private fun setVideoProgress(
        binding: FragmentPostAttachmentBinding,
        videoView: VideoView
    ) {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
        timer = Timer()
        timer?.scheduleAtFixedRate(
            timerTask {
                Handler(Looper.getMainLooper()).post {
                    val isPlaying = videoView.isPlaying
                    if (isPlaying) {
                        playMode = true
                        binding.audioSlider.valueTo = videoView.duration.toFloat()
                        binding.audioSlider.value = videoView.currentPosition.toFloat()
                        binding.curTime.text = convertToMMSS(videoView.currentPosition)
                        binding.duration.text = convertToMMSS(videoView.duration)
                    } else if (playMode) {
                        playMode = false
                        binding.audioSlider.value = 0f
                        binding.curTime.text = getString(R.string.start_time)
                        binding.playAudio.setImageResource(R.drawable.ic_play_audio_24)
                        timer?.cancel()
                        timer = null
                    }
                }
            }, 1000, 1000
        )
    }

    private fun listenToAudio(binding: FragmentPostAttachmentBinding, url: String) {
        requireActivity().title = getString(R.string.audio)
        binding.photo.visibility = View.VISIBLE
        binding.video.visibility = View.GONE
        binding.audioGroup.visibility = View.VISIBLE
        binding.photo.setImageResource(R.drawable.audio)
        observer.mediaPlayer?.setDataSource(url)
    }

    private fun watchVideo(binding: FragmentPostAttachmentBinding, url: String) {
        requireActivity().title = getString(R.string.video)
        binding.video.apply {
            visibility = View.VISIBLE
            val mediaController = MediaController(this.context)

            mediaController!!.setAnchorView(binding.video)
            setMediaController(mediaController)

            setVideoURI(Uri.parse(url))
            requestFocus()

            setOnPreparedListener {
                start()
            }
        }

    }

    private fun seePicture(binding: FragmentPostAttachmentBinding, url: String) {
        requireActivity().title = getString(R.string.picture)
        binding.photo.visibility = View.VISIBLE
        binding.audioGroup.visibility = View.GONE
        binding.video.visibility = View.GONE

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
        var Bundle.autorArg: String? by StringArg
        var Bundle.publishedArg: String? by StringArg

        fun convertToMMSS(duration: Int): String {
            return String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration.toLong()) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % TimeUnit.MINUTES.toSeconds(1)
            )
        }
    }
}
