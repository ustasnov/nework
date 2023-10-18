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
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.entity.UserItem

interface OnUsersInteractionListener {
    fun onViewUser(user: UserItem) {}
}

class UsersAdapter(
    private val onUsersInteractionListener: OnUsersInteractionListener
) : ListAdapter<UserItem, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUsersInteractionListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUsersInteractionListener: OnUsersInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: UserItem) {
        binding.apply {
            userName.text = user.name

            userAvatar.isVisible = !user.avatar.isNullOrBlank()
            if (userAvatar.isVisible) {
                Glide.with(userAvatar)
                    .load("${user.avatar}")
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
                onUsersInteractionListener.onViewUser(user)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<UserItem>() {
    override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem == newItem
    }
}

