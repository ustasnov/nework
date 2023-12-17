package ru.netology.nmedia.adapter

import android.content.Context
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
import ru.netology.nmedia.databinding.CardEventBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.EventType
import ru.netology.nmedia.utils.AndroidUtils

interface OnInteractionEventListener {
    fun onLike(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onViewAttachment(event: Event) {}
    fun onViewLikeOwners(event: Event) {}
    fun onViewParticipants(event: Event) {}
    fun onViewSpeakers(event: Event) {}
    fun onParticipant(event: Event) {}
}

class EventsAdapter(
    private val onInteractionEventListener: OnInteractionEventListener,
    private val isAuthorized: Boolean,
    private val context: Context
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionEventListener, context, isAuthorized)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item!!)
    }

}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionEventListener: OnInteractionEventListener,
    private val context: Context,
    private val isAuthorized: Boolean,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {
            author.text = event.author
            published.text = AndroidUtils.formatDate(event.published)
            postText.text = event.content

            if (isAuthorized) {
                favorite.isEnabled = true
                favorite.isChecked = event.likedByMe

                participantMe.isEnabled = true
                participantMe.isChecked = event.participatedByMe
            } else {
                favorite.isEnabled = false
                favorite.isChecked = false

                participantMe.isEnabled = false
                participantMe.isChecked = false
            }

            likesCount.text = formatValue(event.likeOwnerIds.size.toDouble())

            if (event.type === EventType.ONLINE) {
                eventType.text = context.getString(R.string.type_online)
            } else {
                eventType.text = context.getString(R.string.type_offline)
            }
            eventDatetime.text = AndroidUtils.formatDate(event.datetime)

            if (event.participantsIds.isNotEmpty()) {
                if (participantMe.isChecked) {
                    participants.setIconTintResource(R.color.red)
                } else {
                    participants.setIconTintResource(R.color.teal_700)
                }
                participants.text = formatValue(event.participantsIds.size.toDouble())
            } else {
                participants.setIconTintResource(R.color.ext_gray)
                participants.text = ""
            }

            if (event.speakerIds.isNotEmpty()) {
                speakers.setIconTintResource(R.color.teal_700)
                speakers.text = formatValue(event.speakerIds.size.toDouble())
            } else {
                speakers.setIconTintResource(R.color.ext_gray)
                speakers.text = ""
            }
            if (event.coords != null) {
                geo.setIconTintResource(R.color.teal_700)
            } else {
                geo.setIconTintResource(R.color.ext_gray)
            }

            if (event.link.isNullOrBlank()) {
                siteGroup.visibility = View.GONE
            } else {
                siteGroup.visibility = View.VISIBLE
                siteUrl.text = event.link
            }

            avatar.isVisible = !event.authorAvatar.isNullOrBlank()
            if (avatar.isVisible) {
                Glide.with(avatar)
                    .load("${event.authorAvatar}")
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

            if (!event.attachment?.url.isNullOrBlank()) {
                attachment.visibility = View.VISIBLE
                if (event.attachment?.type !== AttachmentType.AUDIO) {
                    Glide.with(attachment)
                        .load("${event.attachment?.url}")
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .into(attachment)
                    if (event.attachment?.type === AttachmentType.VIDEO) {
                        playVideo.visibility = View.VISIBLE
                    }
                } else {
                    attachment.setImageResource(R.drawable.audio)
                }
            }

            attachment.setOnClickListener {
                onInteractionEventListener.onViewAttachment(event)
            }

            favorite.setOnClickListener {
                onInteractionEventListener.onLike(event)
            }

            likeCaption.setOnClickListener {
                onInteractionEventListener.onViewLikeOwners(event)
            }

            participants.setOnClickListener {
                onInteractionEventListener.onViewParticipants(event)
            }

            participantMe.setOnClickListener {
                onInteractionEventListener.onParticipant(event)
            }

            speakers.setOnClickListener {
                onInteractionEventListener.onViewSpeakers(event)
            }

            menu.isVisible = event.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionEventListener.onRemove(event)
                                true
                            }

                            R.id.edit -> {
                                onInteractionEventListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}
