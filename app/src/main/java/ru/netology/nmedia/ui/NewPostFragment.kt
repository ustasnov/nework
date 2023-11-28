package ru.netology.nmedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.BooleanArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.utils.fileFromContentUri
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.WallViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private val mediaPickerContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode === Activity.RESULT_OK) {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        requireActivity().title = getString(R.string.post_title)

        arguments?.isNewPost.let {
            if (it == null || it) {
                viewModel.clearMedia()
                viewModel.toggleNewPost(true)

                wallViewModel.clearMedia()
                wallViewModel.toggleNewPost(true)
            }
        }

        arguments?.textArg.let {
            if (viewModel.isNewPost) {
                val text = viewModel.getNewPostCont().value
                binding.content.setText(text)
            } else {
                binding.content.setText(it)
            }
        }
        binding.link.setText(
            viewModel.edited.value?.link?.toString()
        )
        binding.content.requestFocus()

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (authViewModel.isAuthorized) {
                            val text = binding.content.text.toString()
                            val linkText = binding.link.text.toString()

                            if (text.isNotBlank()) {
                                viewModel.changeContent(text)
                                viewModel.changeLink(linkText)
                                viewModel.save()
                                viewModel.saveNewPostContent("")
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.empty_content_warning),
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

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        binding.clear.setOnClickListener {
            viewModel.clearMedia()
        }

        binding.pickPhoto.setOnClickListener {
            viewModel.setMediaType(AttachmentType.IMAGE)
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(mediaPickerContract::launch)
        }

        binding.takePhoto.setOnClickListener {
            viewModel.setMediaType(AttachmentType.IMAGE)
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent(mediaPickerContract::launch)
        }

        binding.pickAudio.setOnClickListener {
            viewModel.setMediaType(AttachmentType.AUDIO)
            val intent = Intent()
                .setType("audio/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            mediaPickerContract.launch(intent)
        }

        binding.pickVideo.setOnClickListener {
            viewModel.setMediaType(AttachmentType.VIDEO)
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            mediaPickerContract.launch(intent)
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

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.isNewPost) {
                        viewModel.saveNewPostContent(binding.content.text.toString())
                    }
                    findNavController().navigateUp()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.isNewPost: Boolean? by BooleanArg
    }
}
