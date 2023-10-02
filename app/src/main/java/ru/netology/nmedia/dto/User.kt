package ru.netology.nmedia.dto

data class User (
    override val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
) : FeedItem