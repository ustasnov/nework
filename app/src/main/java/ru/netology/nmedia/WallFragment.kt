package ru.netology.nmedia

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.autorArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.publishedArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.typeArg
import ru.netology.nmedia.PostAttachmentFragment.Companion.urlArg
import ru.netology.nmedia.PostFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.WallPostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ProfileViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import ru.netology.nmedia.viewmodel.WallViewModel
import ru.netology.nmedia.viewmodel.empty
import javax.inject.Inject


@AndroidEntryPoint
class WallFragment : Fragment() {
    val viewModel: WallViewModel by activityViewModels()
    val postViewModel: PostViewModel by activityViewModels()
    val userViewModel: UserViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = WallPostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (authViewModel.isAuthorized) {
                    viewModel.likeById(post, userViewModel.currentUser.value!!)
                    postViewModel.likeById(post)
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
                postViewModel.edit(post)
                if (post.attachment != null) {
                    //if (post.attachment?.type === AttachmentType.IMAGE) {
                    postViewModel.setMedia(MediaModel(Uri.parse(post.attachment!!.url), null, post.attachment?.type))
                    //}
                }
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
        })

        binding.list.adapter = adapter

        viewModel.postSource.observe(viewLifecycleOwner) {
            viewModel.clearPosts()
            if (it.authorId != null && it.authorId != 0L) {
                //viewModel.clearPosts()
                if (it.sourceType === SourceType.MYWALL) {
                    viewModel.loadMyWallPosts()
                } else {
                    viewModel.loadWallPosts(it.authorId)
                }
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swiperefresh.isRefreshing = state.refreshing
            when (state.error) {
                ErrorType.LOADING ->
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) {
                            if (viewModel.postSource.value!!.sourceType == SourceType.MYWALL) {
                                viewModel.loadMyWallPosts()
                            } else {
                                viewModel.loadWallPosts(viewModel.postSource.value!!.authorId!!)
                            }
                        }
                        .show()
                else -> Unit
            }
        }

        val swipeRefresh = binding.swiperefresh
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            if (viewModel.postSource.value!!.sourceType == SourceType.MYWALL) {
                viewModel.refreshMyWall()
            } else {
                viewModel.refreshWall(viewModel.postSource.value!!.authorId!!)
            }
            swipeRefresh.isRefreshing = false
        }


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

    override fun onStop() {
        super.onStop()
        //requireActivity().supportFragmentManager.beginTransaction().remove(this@WallFragment).commit()
    }

    companion object {
        //var Bundle.idArg: Long? by LongArg
        //var Bundle.type: String? by StringArg

        @JvmStatic
        fun newInstance() = WallFragment()
    }
    

}
