package ru.netology.nmedia.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.RegisterViewModel

@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    private val viewModel: RegisterViewModel by activityViewModels()

    private val postViewModel: PostViewModel by activityViewModels()

    private val photoPickerContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    requireContext(),
                    "Photo pick error",
                    Toast.LENGTH_SHORT
                ).show()

                Activity.RESULT_OK -> {
                    val uri = it.data?.data ?: return@registerForActivityResult
                    viewModel.setPhoto(MediaModel(uri, uri.toFile()))
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        requireActivity().title = getString(R.string.register)

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.loading.isVisible = state.loading
            binding.register.isVisible = !state.loading
            binding.incorrect.isVisible = state.error
            if (state.success) {
                viewModel.clean()
                postViewModel.refresh()
                findNavController().navigateUp()
            }
        }

        binding.register.setOnClickListener {
            binding.incorrect.isVisible = false
            binding.emptyField.isVisible = false

            AndroidUtils.hideKeyboard(binding.root)

            val login = binding.loginField.editText?.text.toString().trim()
            val password = binding.passwordField.editText?.text.toString()
            val name = binding.nameField.editText?.text.toString()

            if (login.isBlank() || password.isBlank() || name.isBlank()) {
                binding.emptyField.isVisible = true
            } else {
                viewModel.register(login, password, name)
            }
        }

        binding.avatar.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(photoPickerContract::launch)
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it?.uri == null) {
                return@observe
            }
            binding.avatar.setImageURI(it.uri)
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

        return binding.root
    }
}
