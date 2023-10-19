package ru.netology.nmedia.dto

data class UserItem(
    val id: Long,
    val postId: Long,
    val name: String,
    val avatar: String? = null
)
