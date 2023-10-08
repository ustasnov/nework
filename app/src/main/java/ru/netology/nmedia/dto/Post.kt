package ru.netology.nmedia.dto

data class Post(
    override val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: String,
    val coords: Coordinates? = null,
    val link: String? = null,
    val likeOwnerIds: List<Long> = emptyList(),
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    var attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val users: Map<String, UserPreview> = mutableMapOf(),
    val likes: Double,
    val shared: Double,
    val views: Double,
    val video: String? = null,
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem

enum class ErrorType {
    LOADING,
    SAVE,
    REMOVE,
    LIKE
}