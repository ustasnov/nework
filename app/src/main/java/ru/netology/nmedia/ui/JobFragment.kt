package ru.netology.nmedia.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentJobBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.AndroidUtils.showCalendar
import ru.netology.nmedia.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class JobFragment : Fragment() {
    private val viewModel: JobViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentJobBinding.inflate(inflater, container, false)

        val job = viewModel.edited.value!!
        val isNewJob = job.id == 0L
        requireActivity().title = if (isNewJob) getString(R.string.new_job) else getString(R.string.job)

        if (!isNewJob) {
            binding.apply {
                company.setText(job.name)
                position.setText(job.position)
                startDate.setText(AndroidUtils.formatDate(job.start, "dd MMM yyyy"))
                if (!job.finish.isNullOrBlank()) {
                    finishDate.setText(AndroidUtils.formatDate(job.finish, "dd MMM yyyy"))
                }
                if (!job.link.isNullOrBlank()) {
                    link.setText(job.link)
                }
            }
        }

        var cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            binding.startDate.setText(sdf.format(cal.time))
        }

        binding.startDate.setOnFocusChangeListener { _, hasFocus ->
            showCalendar(requireContext(), cal, binding.startDate, hasFocus, dateSetListener)
        }

        binding.startDate.setOnClickListener() {
            showCalendar(requireContext(), cal, it, it.hasFocus(), dateSetListener)
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )

        return binding.root
    }



}