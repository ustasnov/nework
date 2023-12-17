package ru.netology.nmedia.dto

data class User(
    override val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
    var checked: Boolean = false,
) : FeedItem

data class UserPreview(
    val name: String,
    val avatar: String? = null
)
