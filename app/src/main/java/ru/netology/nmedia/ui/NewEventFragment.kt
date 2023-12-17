package ru.netology.nmedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UsersAdapter
import ru.netology.nmedia.databinding.FragmentNewEventBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.EventCash
import ru.netology.nmedia.dto.EventType
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.fileFromContentUri
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.EventViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import java.util.Calendar

@AndroidEntryPoint
class NewEventFragment : Fragment() {
    private val viewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val mediaPickerContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = if (viewModel.currentMediaType.value === AttachmentType.IMAGE)
                    uri.toFile() else fileFromContentUri(requireContext(), uri)
                viewModel.setMedia(MediaModel(uri, file, viewModel.currentMediaType.value))
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.media_pick_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private lateinit var autoCompleteTextView: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewEventBinding.inflate(inflater, container, false)

        autoCompleteTextView = binding.autoCompleteTextView

        val cal = Calendar.getInstance()

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(INPUT_MODE_CLOCK)
                .setHour(12)
                .setMinute(0)
                .setTitleText(getString(R.string.select_event_time))
                .build()

        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.hide()

        binding.topAppBar.setNavigationOnClickListener {
            viewModel.newEventCash = null
            findNavController().navigateUp()
        }

        val adapter = UsersAdapter(object : OnUsersInteractionListener {})

        viewModel.edited.observe(viewLifecycleOwner) {
            val isNewEvent = it.id == 0L

            binding.topAppBar.title = getString(
                if (isNewEvent) R.string.new_event else R.string.edit_event
            )

            if (isNewEvent) {
                if (viewModel.newEventCash !== null) {
                    restoreFromCash(binding, viewModel.newEventCash!!)
                } else {
                    viewModel.clearMedia()
                    val text = viewModel.getNewEventCont().value ?: ""
                    binding.content.editText?.setText(text)
                    binding.speakersMaterialCardView.visibility = View.GONE
                }
            } else {
                binding.eventType.editText?.setText(
                    if (it.type === EventType.ONLINE) getString(R.string.online)
                    else getString(R.string.offline)
                )
                binding.content.editText?.setText(it.content)
                binding.link.editText?.setText(it.link ?: "")
                val eventDateTime = AndroidUtils.formatDate(it.datetime, "dd.MM.yyyy HH:mm")
                binding.eventDate.editText?.setText(eventDateTime.subSequence(0, 10))
                binding.eventTime.editText?.setText(eventDateTime.subSequence(11, 16))

                if (userViewModel.checkList.value == null) {
                    val speakersList: MutableList<User> = mutableListOf()
                    it.speakerIds.forEach { id ->
                        val speaker = it.users[id.toString()]
                        speakersList.add(
                            User(
                                id = id,
                                name = speaker!!.name,
                                login = "",
                                avatar = speaker.avatar,
                                checked = false
                            )
                        )
                    }
                    userViewModel.setCheckList(speakersList.toList())
                    binding.speakersMaterialCardView.visibility =
                        if (speakersList.isEmpty()) View.GONE else View.VISIBLE
                }
            }

            val callback: OnBackPressedCallback =
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (isNewEvent) {
                            viewModel.newEventCash = null
                            viewModel.saveNewEventContent(binding.content.editText?.text.toString())
                        }
                        findNavController().navigateUp()
                    }
                }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                callback
            )
        }

        binding.content.requestFocus()
        binding.mentionsList.adapter = adapter
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    if (authViewModel.isAuthorized) {
                        val text = binding.content.editText?.text.toString().trim()
                        val linkText = binding.link.editText?.text.toString()
                        val eventDate = binding.eventDate.editText?.text.toString()
                        val eventTime = binding.eventTime.editText?.text.toString()
                        val eventTypeText = binding.eventType.editText?.text.toString()

                        val eventType: EventType =
                            if (eventTypeText == getString(R.string.online))
                                EventType.ONLINE
                            else
                                EventType.OFFLINE

                        val speakersIds: MutableList<Long> = mutableListOf()
                        userViewModel.checkList.value?.forEach {
                            speakersIds.add(it.id)
                        }

                        if (text.isNotBlank() && eventDate.isNotBlank()) {
                            val eventDbDate = AndroidUtils.formatDateForDB(
                                eventDate, "T$eventTime"
                            )

                            viewModel.edit(
                                viewModel.edited.value!!.copy(
                                    type = eventType,
                                    content = text,
                                    datetime = eventDbDate,
                                    link = if (linkText.isNullOrBlank()) null else linkText,
                                    speakerIds = speakersIds.toList()
                                )
                            )
                            viewModel.save()
                            viewModel.saveNewEventContent("")
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.required_fields_cannot_be_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.authorization_required),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(R.string.login) { findNavController().navigate(R.id.authFragment) }
                            .show()
                    }
                    AndroidUtils.hideKeyboard(requireView())
                    true
                }

                else -> false
            }
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            viewModel.newEventCash = null
            viewModel.loadEvents()
            findNavController().navigateUp()
        }

        userViewModel.checkList.observe(viewLifecycleOwner) {
            if (userViewModel.checkList.value !== null) {
                adapter.submitList(userViewModel.checkList.value!!.map { user ->
                    user.checked = false
                    return@map user
                })
                binding.speakersMaterialCardView.visibility =
                    if (userViewModel.checkList.value!!.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        binding.eventDate.editText?.setOnFocusChangeListener { _, hasFocus ->
            AndroidUtils.showCalendar(
                requireContext(),
                cal,
                binding.eventDate.editText!!,
                hasFocus,
                AndroidUtils.getDatePickerDialogListener(binding.eventDate.editText!!, cal)
            )
        }

        binding.eventDate.editText?.setOnClickListener {
            AndroidUtils.showCalendar(
                requireContext(), cal, it,
                true, AndroidUtils.getDatePickerDialogListener(it, cal)
            )
        }

        binding.eventTime.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                timePicker.show(childFragmentManager, "tag")
            }
        }

        binding.eventTime.editText?.setOnClickListener {
            timePicker.show(childFragmentManager, "tag")
        }

        timePicker.addOnPositiveButtonClickListener {
            var hourStr = timePicker.hour.toString()
            var minuteStr = timePicker.minute.toString()
            if (timePicker.hour < 10) {
                hourStr = "0$hourStr"
            }
            if (timePicker.minute < 10) {
                minuteStr = "0$minuteStr"
            }
            val hm = "$hourStr:$minuteStr"
            binding.eventTime.editText?.setText(hm)
        }

        binding.clear.setOnClickListener {
            viewModel.clearMedia()
        }

        binding.pickPhoto.setOnClickListener {
            saveToCash(binding)
            viewModel.setMediaType(AttachmentType.IMAGE)
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(mediaPickerContract::launch)
        }

        binding.takePhoto.setOnClickListener {
            saveToCash(binding)
            viewModel.setMediaType(AttachmentType.IMAGE)
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent(mediaPickerContract::launch)
        }

        binding.pickAudio.setOnClickListener {
            saveToCash(binding)
            viewModel.setMediaType(AttachmentType.AUDIO)
            val intent = Intent()
                .setType("audio/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            mediaPickerContract.launch(intent)
        }

        binding.pickVideo.setOnClickListener {
            saveToCash(binding)
            viewModel.setMediaType(AttachmentType.VIDEO)
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            mediaPickerContract.launch(intent)
        }

        binding.pickSpeakers.setOnClickListener {
            saveToCash(binding)
            userViewModel.setForSelection(
                getString(R.string.select_users_to_speakers),
                true,
                "Users"
            )
            findNavController().navigate(R.id.usersFragment)
        }

        viewModel.media.observe(viewLifecycleOwner) { mediaModel ->
            if (mediaModel == null) {
                binding.previewContainer.isGone = true
                return@observe
            }
            binding.previewContainer.isVisible = true
            if (mediaModel.attachmentType != AttachmentType.AUDIO) {
                Glide.with(binding.preview).load("${mediaModel.uri}")
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.preview)
            } else {
                binding.preview.setImageResource(R.drawable.audio)
            }
        }

        return binding.root
    }

    private fun restoreFromCash(
        binding: FragmentNewEventBinding,
        eventCash: EventCash
    ) {
        binding.content.editText?.setText(eventCash.content)
        binding.link.editText?.setText(eventCash.link)
        binding.eventDate.editText?.setText(eventCash.eventDate)
        binding.eventTime.editText?.setText(eventCash.eventTime)
        binding.eventType.editText?.setText(eventCash.eventType)
    }

    private fun saveToCash(binding: FragmentNewEventBinding) {
        val text = binding.content.editText?.text.toString().trim()
        val linkText = binding.link.editText?.text.toString()
        val eventDate = binding.eventDate.editText?.text.toString()
        val eventTime = binding.eventTime.editText?.text.toString()
        val eventTypeText = binding.eventType.editText?.text.toString()

        viewModel.newEventCash = EventCash(
            eventType = eventTypeText,
            content = text,
            eventDate = eventDate,
            eventTime = eventTime,
            link = linkText
        )
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.hide()
        val eventTypeItems = listOf(getString(R.string.offline), getString(R.string.online))
        val eventTypeAdapter =
            ArrayAdapter(requireContext(), R.layout.event_type_item, eventTypeItems)
        autoCompleteTextView.setAdapter(eventTypeAdapter)
    }

    override fun onStop() {
        super.onStop()
        userViewModel.setCheckList(null)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.show()
    }

}
