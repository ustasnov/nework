package ru.netology.nmedia

import android.content.Intent
import android.media.MediaPlayer
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
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.PostFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.media.MediaLifecycleObserver
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.empty
import java.util.Locale

fun formatValue(value: Double): String {
    if (value >= 1000000000.0) {
        return "\u221e"
    }
    val suffix: String
    val res = when {
        value >= 1000000.0 -> {
            suffix = "M"
            String.format(Locale.ROOT, "%f", value / 1000000.0)
        }

        value >= 1000.0 -> {
            suffix = "K"
            String.format(Locale.ROOT, "%f", value / 1000.0)
        }

        else -> {
            suffix = ""
            String.format(Locale.ROOT, "%f", value)
        }
    }

    val dotPosition = res.indexOf(".")

    return when {
        (value >= 10000.0 && value < 1000000.0) || value < 1000 || res[dotPosition + 1] == '0' ->
            res.substring(0, dotPosition) + suffix

        else -> res.substring(0, dotPosition + 2) + suffix
    }
}

@AndroidEntryPoint
class FeedFragment : Fragment() {
    val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val observer = MediaLifecycleObserver()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        lifecycle.addObserver(observer)

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

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
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

            */

            override fun onPlayVideo(post: Post) {
                viewModel.toggleNewPost(false)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                if (intent.resolveActivity(context!!.packageManager) != null) {
                    startActivity(intent)
                }
            }

            override fun onViewAttachment(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postPhotoFragment,
                    Bundle().apply {
                        //textArg = "${BuildConfig.BASE_URL}media/${post.attachment!!.url}"
                        textArg = "${post.attachment!!.url}"
                    })
            }

            override fun onViewPost(post: Post) {
                viewModel.viewById(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply {
                        idArg = post.id
                    }
                )
            }
        }, observer)

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
