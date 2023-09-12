package ru.netology.nmedia.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.formatValue
import ru.netology.nmedia.media.MediaLifecycleObserver

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPlayVideo(post: Post) {}
    fun onViewPost(post: Post) {}
    fun onViewAttachment(post: Post) {}
    //fun onPlayAudio(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val mediaLifecycleObserver: MediaLifecycleObserver
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int  =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener, mediaLifecycleObserver)
            }
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            else -> error("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("unknown view type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        Glide.with(binding.image)
            //.load("${BuildConfig.BASE_URL}media/${ad.image}")
            .load("${ad.image}")
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .into(binding.image)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val mediaLifecycleObserver: MediaLifecycleObserver
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            postText.text = post.content
            favorite.isChecked = post.likedByMe
            favorite.isCheckable = post.ownedByMe
            favorite.text = formatValue(post.likes)
            share.text = formatValue(post.shared)
            views.text = formatValue(post.views)

            avatar.isVisible = !post?.authorAvatar.isNullOrBlank()
            if (avatar.isVisible ) {
                Glide.with(avatar)
                    //.load("${BuildConfig.BASE_URL}avatars/${post.authorAvatar}")
                    .load("${post.authorAvatar}")
                    .circleCrop()
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(avatar)
            } else {
                avatar.setImageResource(R.drawable.baseline_account_circle_24)
                avatar.visibility = View.VISIBLE
            }

            //attachment.contentDescription = post.attachment?.description
            playVideo.visibility = View.GONE
            playAudio.visibility = View.GONE
            pauseAudio.visibility = View.GONE
            audioSlider.visibility = View.GONE
            attachment.visibility = View.GONE

            //attachment.isVisible = !post.attachment?.url.isNullOrBlank()
            if (!post.attachment?.url.isNullOrBlank()) {
                attachment.visibility = View.VISIBLE
                if (post.attachment?.type !== AttachmentType.AUDIO) {
                    Glide.with(attachment)
                        //.load("${BuildConfig.BASE_URL}media/${post.attachment?.url}")
                        .load("${post.attachment?.url}")
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .into(attachment)
                }
                if (post.attachment?.type === AttachmentType.VIDEO) {
                    playVideo.visibility = View.VISIBLE
                } else if (post.attachment?.type === AttachmentType.AUDIO) {
                    attachment.visibility = View.GONE
                    playAudio.visibility = View.VISIBLE
                    audioSlider.visibility = View.VISIBLE
                }
            }

            /*
            videoPreview.setOnClickListener {

            }
            */

            playAudio.setOnClickListener {
                val isPlaying = mediaLifecycleObserver?.mediaPlayer?.isPlaying() ?: false

                //if (!isPlaying) {
                    mediaLifecycleObserver?.stop()
                    mediaLifecycleObserver?.mediaPlayer?.reset()
                    mediaLifecycleObserver?.mediaPlayer?.setDataSource("${post.attachment?.url}")
                    mediaLifecycleObserver?.play()
                //} else {
                //    mediaLifecycleObserver?.play()
                //}
                playAudio.visibility = View.GONE
                pauseAudio.visibility = View.VISIBLE
            }

            pauseAudio.setOnClickListener {
                mediaLifecycleObserver.pause()
                playAudio.visibility = View.VISIBLE
                pauseAudio.visibility = View.GONE
            }

            playVideo.setOnClickListener {
                //onInteractionListener.onPlayVideo(post)
            }

            attachment.setOnClickListener {
                onInteractionListener.onViewAttachment(post)
            }

            favorite.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            root.setOnClickListener {
                onInteractionListener.onViewPost(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        /*
        if (oldItem::class != newItem::class) {
            return false
        }

        return oldItem.id == newItem.id
         */
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
