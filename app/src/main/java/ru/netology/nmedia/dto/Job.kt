package ru.netology.nmedia.dto

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
) : FeedItem
