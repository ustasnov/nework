package ru.netology.nmedia.dto

data class Event(
    override val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coordinates: Coordinates? = null,
    val type: String,
    val likeOwnerIds: Array<Long> = emptyArray(),
    val likedByMe: Boolean = false,
    val speakerIds: Array<Long> = emptyArray(),
    val participantsIds: Array<Long> = emptyArray(),
    val participatedByMe: Boolean = false,
    var attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val users: Users = Users()
) : FeedItem