package ru.netology.nmedia.ui

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
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.autorArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.publishedArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.typeArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.urlArg
import ru.netology.nmedia.ui.PostFragment.Companion.idArg
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionEventListener
import ru.netology.nmedia.adapter.EventsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentEventsFeedBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.EventViewModel
import ru.netology.nmedia.viewmodel.emptyEvent
import javax.inject.Inject

@AndroidEntryPoint
class EventsFeedFragment : Fragment() {
    val viewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsFeedBinding.inflate(inflater, container, false)

        requireActivity().setTitle(getString(R.string.events))

        val adapter = EventsAdapter(object : OnInteractionEventListener {
            override fun onLike(event: Event) {
                if (authViewModel.isAuthorized) {
                    viewModel.likeById(event)
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

            /*
            override fun onShare(post: Post) {
                if (authViewModel.isAuthorized) {
                    viewModel.shareById(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(intent, getString(R.string.share_post))
                    startActivity(shareIntent)
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
             */

            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            /*
            override fun onEdit(event: Event) {
                viewModel.edit(event)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = event.content
                    }
                )
            }

             */

            /*
            override fun onPlayAudio(post: Post) {
                MediaPlayer.create(this,).apply {
                    start()

                }
            }

            override fun onPlayVideo(post: Post) {
                viewModel.toggleNewPost(false)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                if (intent.resolveActivity(context!!.packageManager) != null) {
                    startActivity(intent)
                }
            }

            */

            override fun onViewAttachment(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFeedFragment_to_postAttachmentFragment,
                    Bundle().apply {
                        //textArg = "${BuildConfig.BASE_URL}media/${post.attachment!!.url}"
                        urlArg = "${event.attachment!!.url}"
                        typeArg = when (event.attachment?.type) {
                            AttachmentType.IMAGE -> "image"
                            AttachmentType.AUDIO -> "audio"
                            AttachmentType.VIDEO -> "video"
                            else -> ""
                        }
                        autorArg = "${event.author}"
                        publishedArg = "${event.published}"
                    })
            }

            /*
            override fun onViewPost(event: Event) {
                viewModel.viewById(event)
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply {
                        idArg = event.id
                    }
                )
            }

             */

            override fun onViewLikeOwners(event: Event) {

                if (event.likeOwnerIds.size > 0) {
                    findNavController().navigate(
                        R.id.action_eventsFeedFragment_to_likeOwnersFragment,
                        Bundle().apply {
                            idArg = event.id
                        })
                }
            }

            override fun onViewParticipants(event: Event) {
                if (event.participantsIds.size > 0) {
                    findNavController().navigate(
                        R.id.action_eventsFeedFragment_to_participantsFragment,
                        Bundle().apply {
                            idArg = event.id
                        })
                }
            }

            override fun onViewSpeakers(event: Event) {
                if (event.speakerIds.size > 0) {
                    findNavController().navigate(
                        R.id.action_eventsFeedFragment_to_speakersFragment,
                        Bundle().apply {
                            idArg = event.id
                        })
                }
            }
            //}, observer)
        }, this.requireContext())

        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //viewModel.data.collectLatest(adapter::submitData)
                viewModel.data.collect(adapter::submitData)
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

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        binding.add.setOnClickListener {
            if (authViewModel.isAuthorized) {
                viewModel.edit(emptyEvent)
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
