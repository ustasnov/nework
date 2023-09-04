package ru.netology.nmedia.model

import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Post

data class FeedModel (
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)

data class FeedModelState (
    val error: ErrorType? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
)
