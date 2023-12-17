package ru.netology.nmedia.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.autorArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.publishedArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.typeArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.urlArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import ru.netology.nmedia.viewmodel.empty
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        requireActivity().title = getString(R.string.postsTitle)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (authViewModel.isAuthorized) {
                    viewModel.likeById(post)
                } else {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.authorization_required),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.login) { findNavController().navigate(R.id.authFragment) }
                        .show()
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                if (post.attachment != null) {
                    viewModel.setMedia(
                        MediaModel(
                            Uri.parse(post.attachment!!.url),
                            null,
                            post.attachment?.type
                        )
                    )
                }
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment
                )
            }

            override fun onViewAttachment(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postAttachmentFragment,
                    Bundle().apply {
                        urlArg = post.attachment!!.url
                        typeArg = when (post.attachment?.type) {
                            AttachmentType.IMAGE -> "image"
                            AttachmentType.AUDIO -> "audio"
                            AttachmentType.VIDEO -> "video"
                            else -> ""
                        }
                        autorArg = post.author
                        publishedArg = post.published
                    })
            }

            override fun onViewLikeOwners(post: Post) {
                if (post.likeOwnerIds.isNotEmpty()) {
                    userViewModel.getLikeOwners(post.id)
                    userViewModel.setForSelection(
                        getString(R.string.like_title),
                        false,
                        "LikeOwners"
                    )
                    findNavController().navigate(
                        R.id.usersFragment
                    )
                }
            }

            override fun onViewMentions(post: Post) {
                if (post.mentionIds.isNotEmpty()) {
                    userViewModel.getMentions(post.id)
                    userViewModel.setForSelection(getString(R.string.mentors), false, "Mentions")
                    findNavController().navigate(
                        R.id.usersFragment
                    )
                }

            }
        }, authViewModel.isAuthorized)

        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swiperefresh.isRefreshing =
                        state.refresh is LoadState.Loading ||
                                state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            if (it.refreshing || it.loading) {
                binding.swiperefresh.visibility = View.VISIBLE
                binding.swiperefresh.isRefreshing = true
            } else {
                binding.swiperefresh.isRefreshing = false
            }
        }

        viewModel.refreshList.observe(viewLifecycleOwner) {
            viewModel.refresh()
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        binding.add.setOnClickListener {
            if (authViewModel.isAuthorized) {
                viewModel.edit(empty)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.authorization_required),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.login) { findNavController().navigate(R.id.authFragment) }
                    .show()
            }
        }

        return binding.root
    }

}
