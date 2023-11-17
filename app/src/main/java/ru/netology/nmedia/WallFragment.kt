package ru.netology.nmedia

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
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.autorArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.publishedArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.typeArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.urlArg
import ru.netology.nmedia.PostFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.WallViewModel
import ru.netology.nmedia.viewmodel.empty
import javax.inject.Inject


@AndroidEntryPoint
class WallFragment : Fragment() {
    val viewModel: WallViewModel by activityViewModels()
    val postViewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        //requireActivity().setTitle(getString(R.string.postsTitle))

        /*
        val ownerId = requireArguments().idArg
        val type = requireArguments().type
        */
        var ownerId = 0L
        var type = ""



        /*
        viewModel.setData(PostsSource(ownerId,
            when (type) {
                "WALL" -> SourceType.WALL
                "MYWALL" -> SourceType.MYWALL
                else -> SourceType.MYWALL
            })
        )

         */

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

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                postViewModel.edit(post)
                findNavController().navigate(
                    R.id.action_profileFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

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

            override fun onViewAttachment(post: Post) {
                findNavController().navigate(
                    R.id.action_profileFragment_to_postAttachmentFragment,
                    Bundle().apply {
                        //textArg = "${BuildConfig.BASE_URL}media/${post.attachment!!.url}"
                        urlArg = "${post.attachment!!.url}"
                        typeArg = when (post.attachment?.type) {
                            AttachmentType.IMAGE -> "image"
                            AttachmentType.AUDIO -> "audio"
                            AttachmentType.VIDEO -> "video"
                            else -> ""
                        }
                        autorArg = "${post.author}"
                        publishedArg = "${post.published}"
                    })
            }

            override fun onViewPost(post: Post) {
                postViewModel.viewById(post)
                findNavController().navigate(
                    R.id.action_profileFragment_to_postFragment,
                    Bundle().apply {
                        idArg = post.id
                    }
                )
            }

            override fun onViewLikeOwners(post: Post) {

                if (post.likeOwnerIds.size > 0) {
                    findNavController().navigate(R.id.action_profileFragment_to_likeOwnersFragment,
                        Bundle().apply {
                            idArg = post.id
                        })
                }
            }

            override fun onViewMentions(post: Post) {

                if (post.mentionIds.size > 0) {
                    findNavController().navigate(R.id.action_profileFragment_to_mentionsFragment,
                        Bundle().apply {
                            idArg = post.id
                        })
                }


            }
        //}, observer)
        })
        //RecyclerView.Adapter.StateRestorationPolicy.PREVENT
        //adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.list.adapter = adapter

        viewModel.wallItem.observe(viewLifecycleOwner) {
            ownerId = it.ownerId
            type = it.type!!
            viewModel.setData(PostsSource(ownerId,
                when (type) {
                    "WALL" -> SourceType.WALL
                    "MYWALL" -> SourceType.MYWALL
                    else -> SourceType.MYWALL
                })
            )
            adapter.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
                //viewModel.data.collect(adapter::submitData)
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

        //viewModel.loadPosts()

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


    /*
    override fun onStop() {
        super.onStop()
        //println("From ProfileFragment.onStop.clearJobs()")
        viewModel.clearPosts()
    }
     */


    companion object {
        //var Bundle.idArg: Long? by LongArg
        //var Bundle.type: String? by StringArg

        @JvmStatic
        fun newInstance() = WallFragment()
    }
    

}