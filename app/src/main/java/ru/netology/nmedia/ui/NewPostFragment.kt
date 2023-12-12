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
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UsersAdapter
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.fileFromContentUri
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import ru.netology.nmedia.viewmodel.WallViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by activityViewModels()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        //binding.topAppBar.title = getString(R.string.mentors)

        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.hide()

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = UsersAdapter(object : OnUsersInteractionListener {
            override fun onViewUser(user: User) {
                /*
                if (enableSelection) {
                    viewModel.setChecked(user.id, !user.checked)
                } else {
                    profileViewModel.setPostSource(PostsSource(user.id, SourceType.WALL))
                    findNavController().navigate(
                        R.id.action_usersFragment_to_profileFragment
                    )
                }
                */
            }
        })

        viewModel.edited.observe(viewLifecycleOwner) {
            val isNewPost = it.id == 0L

            //requireActivity().title = getString(R.string.post_title)
            binding.topAppBar.title = getString(
                if (isNewPost) R.string.new_post else R.string.edit_post
            )

            if (isNewPost) {
                viewModel.clearMedia()
                //viewModel.toggleNewPost(true)
                wallViewModel.clearMedia()
                //wallViewModel.toggleNewPost(true)
                val text = viewModel.getNewPostCont().value ?: ""
                binding.content.editText?.setText(text)
                binding.mentionsMaterialCardView.visibility = View.GONE
            } else {
                binding.content.editText?.setText(it.content)
                binding.link.editText?.setText(it.link ?: "")
                if (userViewModel.checkList.value == null) {
                    val mentionsList: MutableList<User> = mutableListOf()
                    it.mentionIds.forEach() { id ->
                        val mention = it.users[id.toString()]
                        mentionsList.add(
                            User(
                                id = id,
                                name = mention!!.name,
                                login = "",
                                avatar = mention.avatar,
                                checked = false
                            )
                        )
                    }
                    adapter.submitList(mentionsList.toList())
                    binding.mentionsMaterialCardView.visibility =
                        if (mentionsList.isEmpty()) View.GONE else View.VISIBLE
                }
            }

            val callback: OnBackPressedCallback =
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (isNewPost) {
                            viewModel.saveNewPostContent(binding.content.editText?.text.toString())
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

        /*
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
         */
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (authViewModel.isAuthorized) {
                            val text = binding.content.editText?.text.toString().trim()
                            val linkText = binding.link.editText?.text.toString()

                            val mentionsIds: MutableList<Long> = mutableListOf()
                            userViewModel.checkList.value?.forEach {
                                mentionsIds.add(it.id)
                            }

                            if (text.isNotBlank()) {
                                viewModel.edit(
                                    viewModel.edited.value!!.copy(
                                        content = text,
                                        link = if (linkText.isNullOrBlank()) null else linkText,
                                        mentionIds = mentionsIds.toList()
                                    )
                                )
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
        }
        //, viewLifecycleOwner)

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        userViewModel.checkList.observe(viewLifecycleOwner) {
            if (userViewModel.checkList.value !== null) {
                adapter.submitList(userViewModel.checkList.value!!.map { user ->
                    user.checked = false
                    return@map user})
                binding.mentionsMaterialCardView.visibility =
                    if (userViewModel.checkList.value!!.isEmpty()) View.GONE else View.VISIBLE
            }
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

        binding.pickMentions.setOnClickListener {
            userViewModel.setForSelection(getString(R.string.select_users_to_mention), true, "Users")
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

    override fun onStop() {
        super.onStop()
        userViewModel.setCheckList(null)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.show()
    }

}
