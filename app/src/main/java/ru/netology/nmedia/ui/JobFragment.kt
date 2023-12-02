package ru.netology.nmedia.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentJobBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.AndroidUtils.formatDateForDB
import ru.netology.nmedia.utils.AndroidUtils.getDatePickerDialogListener
import ru.netology.nmedia.utils.AndroidUtils.showCalendar
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class JobFragment : Fragment() {
    private val viewModel: JobViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentJobBinding.inflate(inflater, container, false)

        val job = viewModel.edited.value!!
        val isNewJob = job.id == 0L
        requireActivity().title = if (isNewJob) getString(R.string.new_job) else getString(R.string.job)

        binding.apply {

            if (!isNewJob) {
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

            var cal = Calendar.getInstance()

            startDate.setOnFocusChangeListener { _, hasFocus ->
                showCalendar(requireContext(), cal, startDate,
                    hasFocus, getDatePickerDialogListener(startDate, cal))
            }

            startDate.setOnClickListener() {
                showCalendar(requireContext(), cal, it,
                    it.hasFocus(), getDatePickerDialogListener(it, cal))
            }

            finishDate.setOnFocusChangeListener { _, hasFocus ->
                showCalendar(requireContext(), cal, finishDate,
                    hasFocus, getDatePickerDialogListener(finishDate, cal))
            }

            finishDate.setOnClickListener() {
                showCalendar(requireContext(), cal, it,
                    it.hasFocus(), getDatePickerDialogListener(it, cal))
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (authViewModel.isAuthorized) {
                            val companyText = binding.company.text.toString()
                            val positionText = binding.position.text.toString()
                            var startDateText = binding.startDate.text.toString()
                            var finishDateText = binding.finishDate.text.toString()
                            val linkText = binding.link.text.toString()

                            if (companyText.isNotBlank() &&
                                positionText.isNotBlank() &&
                                startDateText.isNotBlank()) {

                                startDateText = formatDateForDB(startDateText)
                                if (finishDateText.isNotBlank()) {
                                    finishDateText = formatDateForDB(finishDateText)
                                } else {
                                    finishDateText = ""
                                }

                                val newPost = viewModel.edited.value!!.copy(
                                    name = companyText,
                                    position = positionText,
                                    start = startDateText,
                                    finish = finishDateText.ifBlank { null },
                                    link = linkText.ifBlank { null }
                                )
                                viewModel.saveMyJob(newPost)
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
        }, viewLifecycleOwner)

        viewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
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