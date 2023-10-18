package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardUserBinding
import ru.netology.nmedia.dto.UserItem

interface OnMentionsInteractionListener {
    fun onViewUser(user: UserItem) {}
}

class MentionsAdapter(
    private val onMentionsInteractionListener: OnMentionsInteractionListener
) : ListAdapter<UserItem, MentionViewHolder>(MentionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MentionViewHolder(binding, onMentionsInteractionListener)
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class MentionViewHolder(
    private val binding: CardUserBinding,
    private val onMentionsInteractionListener: OnMentionsInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(userItem: UserItem) {
        binding.apply {
            userName.text = userItem.name

            userAvatar.isVisible = !userItem.avatar.isNullOrBlank()
            if (userAvatar.isVisible) {
                Glide.with(userAvatar)
                    .load("${userItem.avatar}")
                    .circleCrop()
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(userAvatar)
            } else {
                userAvatar.setImageResource(R.drawable.baseline_account_circle_24)
                userAvatar.visibility = View.VISIBLE
            }

            root.setOnClickListener {
                onMentionsInteractionListener.onViewUser(userItem)
            }
        }
    }
}

class MentionDiffCallback : DiffUtil.ItemCallback<UserItem>() {
    override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem == newItem
    }
}