package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.autorArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.publishedArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.typeArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.urlArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class PostFragment : Fragment() {
    val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(inflater, container, false)

        val viewHolder = PostViewHolder(binding.postFr, object : OnInteractionListener {
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

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_postFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigate(R.id.action_postFragment_to_feedFragment)
            }

            /*
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
                    R.id.action_postFragment_to_postAttachmentFragment,
                    Bundle().apply {
                        urlArg = post.attachment!!.url
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

            override fun onViewLikeOwners(post: Post) {
                /*
                if (post.likeOwnerIds.size > 0) {
                    findNavController().navigate(R.id.action_postFragment_to_usersFragment,
                        Bundle().apply {
                            idArg = post.id
                            listType = "likeOwners"
                        })
                }

                 */
            }

            override fun onViewMentions(post: Post) {
                /*
                if (post.mentionIds.size > 0) {
                    findNavController().navigate(R.id.action_postFragment_to_usersFragment,
                        Bundle().apply {
                            idArg = post.id
                            listType = "mentions"
                        })
                }

                 */
            }
        })

        //val postId = requireArguments().idArg
        viewModel.currentPost.observe(viewLifecycleOwner) {
            viewHolder.bind(it)
        }
        //viewHolder.bind(viewModel.currentPost.value!!)

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

    companion object {
        var Bundle.idArg: Long? by LongArg
    }
}