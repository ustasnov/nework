package ru.netology.nmedia.entity

data class UserItem(
    val id: Long,
    val postId: Long,
    val name: String,
    val login: String? = null,
    val avatar: String? = null
)
