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
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class JobFragment : Fragment() {
    private val viewModel: JobViewModel by activityViewModels()
        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentJobBinding.inflate(inflater, container, false)

            requireActivity().title = getString(R.string.job)



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