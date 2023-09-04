package ru.netology.nmedia

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import okhttp3.internal.wait
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthSignViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {
    var _binding: FragmentAuthBinding? = null
    val binding: FragmentAuthBinding
        get() = _binding!!

    private val viewModel: AuthSignViewModel by viewModels()

    private val postViewModel: PostViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.loading.isVisible = state.loading
            binding.authorize.isVisible = !state.loading
            binding.incorrect.isVisible = state.error
            if (state.success) {
                postViewModel.refresh()
                viewModel.clean()
                findNavController().navigateUp()
            }
        }

        binding.authorize.setOnClickListener {
            binding.incorrect.isVisible = false
            binding.emptyField.isVisible = false

            AndroidUtils.hideKeyboard(binding.root)

            val login = binding.loginField.text.toString().trim()
            val password = binding.passwordField.text.toString()

            if (login.isBlank() || password.isBlank()) {
                binding.emptyField.isVisible = true
            } else {
                viewModel.authorize(login, password)
            }
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.clean()
                    findNavController().navigateUp()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
