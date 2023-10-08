package ru.netology.nmedia.entity

data class UserItem(
    val id: Long,
    val postId: Long,
    val name: String,
    val avatar: String? = null
)
