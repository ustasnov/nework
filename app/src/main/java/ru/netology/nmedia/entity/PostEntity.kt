package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

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
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)


