package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.AndroidUtils.formatDate
import java.util.Locale

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPlayVideo(post: Post) {}
    fun onViewPost(post: Post) {}
    fun onViewAttachment(post: Post) {}
    fun onViewLikeOwners(post: Post) {}
    fun onViewMentions(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val isAuthorized: Boolean
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, isAuthorized)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item!!)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val isAuthorized: Boolean
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = formatDate(post.published)
            postText.text = post.content

            if (isAuthorized) {
                favorite.isEnabled = true
                favorite.isChecked = post.likedByMe
            } else {
                favorite.isEnabled = false
                favorite.isChecked = false
            }

            likesCount.text = formatValue(post.likeOwnerIds.size.toDouble())
            siteUrl.text = post.link
            if (post.link.isNullOrBlank()) {
                siteGroup.visibility = View.GONE
            } else {
                siteGroup.visibility = View.VISIBLE
            }

            if (post.mentionIds.size > 0) {
                if (post.mentionedMe) {
                    ment.setIconTintResource(R.color.red)
                } else {
                    ment.setIconTintResource(R.color.teal_700)
                }
                ment.text = formatValue(post.mentionIds.size.toDouble())
            } else {
                ment.setIconTintResource(R.color.ext_gray)
                ment.text = ""
            }
            if (post.coords != null) {
                geo.setIconTintResource(R.color.teal_700)
            } else {
                geo.setIconTintResource(R.color.ext_gray)
            }

            avatar.isVisible = !post.authorAvatar.isNullOrBlank()
            if (avatar.isVisible) {
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

            playVideo.visibility = View.GONE
            attachment.visibility = View.GONE

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
                    if (post.attachment?.type === AttachmentType.VIDEO) {
                        playVideo.visibility = View.VISIBLE
                    }
                } else {
                    attachment.setImageResource(R.drawable.audio)
                }
            }

            attachment.setOnClickListener {
                onInteractionListener.onViewAttachment(post)
            }

            favorite.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            likeCaption.setOnClickListener {
                onInteractionListener.onViewLikeOwners(post)
            }

            ment.setOnClickListener {
                onInteractionListener.onViewMentions(post)
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

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}

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
