package ru.netology.nmedia.ui

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
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.JobViewModel

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