package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Coordinates
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.UserPreview

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    @Embedded
    var coords: CoordinatesEmbeddable?,
    val link: String?,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val ownedByMe: Boolean,
    val likes: Double,
    val shared: Double,
    val views: Double,
    val video: String? = null,
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        published = published,
        coords = coords?.toDto(),
        link = link,
        mentionedMe = mentionedMe,
        likedByMe = likedByMe,
        attachment = attachment?.toDto(),
        ownedByMe = ownedByMe,
        likes = likes,
        shared = shared,
        views = views,
        video = video,
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorJob = dto.authorJob,
                content = dto.content,
                published = dto.published,
                coords = CoordinatesEmbeddable.fromDto(dto.coords),
                link = dto.link,
                mentionedMe = dto.mentionedMe,
                likedByMe = dto.likedByMe,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                ownedByMe = dto.ownedByMe,
                likes = dto.likes,
                shared = dto.shared,
                views = dto.views,
                video = dto.video,
            )
    }
}

data class PostWithLists(
    @Embedded val post: PostEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "postId"
    )
    val likeOwners: List<LikeOwnerEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "postId"
    )
    val mentions: List<MentionEntity>
) {
    fun toDto() = post.toDto().copy(
        likeOwnerIds = likeOwners.map{ it.id },
        mentionIds = mentions.map{ it.id },
        users = getPostUsers()
    )

    fun getPostUsers(): Map<String, UserPreview> {
        val result: MutableMap<String, UserPreview> = mutableMapOf()
        likeOwners.forEach {
            val key = it.postId.toString()
            result.put(key, UserPreview(it.name, it.avatar))
        }
        mentions.forEach {
            val key = it.postId.toString()
            if (!result.containsKey(key)) {
                result.put(key, UserPreview(it.name, it.avatar))
            }
        }
        return  result.toMap()
    }
    companion object {
        fun fillLikeOwnersList(post: Post): List<LikeOwnerEntity> {
            val result: MutableList<LikeOwnerEntity> = mutableListOf()
            post.likeOwnerIds.forEach {
                val key = it.toString()
                if (post.users.containsKey(key)) {
                    val userPreview = post.users[key]
                    val userItem = UserItem(it, post.id, userPreview!!.name, userPreview.avatar)
                    result.add(LikeOwnerEntity.fromDto(userItem))
                }
            }
            return result.toList()
        }

        fun fillMentionsList(post: Post): List<MentionEntity> {
            val result: MutableList<MentionEntity> = mutableListOf()
            post.mentionIds.forEach {
                val key = it.toString()
                if (post.users.containsKey(key)) {
                    val userPreview = post.users[key]
                    val userItem = UserItem(it, post.id, userPreview!!.name, userPreview.avatar)
                    result.add(MentionEntity.fromDto(userItem))
                }
            }
            return result.toList()
        }

        fun fromDto(dto: Post) = dto.let {
            PostWithLists(
                post = PostEntity.fromDto(it),
                likeOwners = fillLikeOwnersList(it),
                mentions = fillMentionsList(it)
            )
        }

    }
}

data class CoordinatesEmbeddable(
    var lat_param: String,
    var long_param: String,
) {
    fun toDto() = Coordinates(lat_param, long_param)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.lat, it.long)
        }
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map {
    PostEntity.fromDto(it)
}

fun List<PostWithLists>.toDtoWithLists(): List<Post> = map(PostWithLists::toDto)
fun List<Post>.toEntityWithLists(): List<PostWithLists> = map {
    PostWithLists.fromDto(it)
}
