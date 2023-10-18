package ru.netology.nmedia.model

import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.UserItem

data class FeedModel (
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)

data class UserModel (
    val users: List<User> = emptyList(),
    val empty: Boolean = false
)

data class UserItemModel (
    val users: List<UserItem> = emptyList(),
    val empty: Boolean = false
)

data class FeedModelState (
    val error: ErrorType? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
)
