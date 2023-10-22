package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    var coords: CoordinatesEmbeddable?,
    val eventType: String,
    val likedByMe: Boolean,
    val participatedByMe: Boolean,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val link: String?,
    val ownedByMe: Boolean,
) {
    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        datetime = datetime,
        published = published,
        coords = coords?.toDto(),
        type = enumValueOf<EventType>(eventType),
        likedByMe = likedByMe,
        participatedByMe = participatedByMe,
        attachment = attachment?.toDto(),
        link = link,
        ownedByMe = ownedByMe,
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorJob = dto.authorJob,
                content = dto.content,
                datetime = dto.datetime,
                published = dto.published,
                coords = CoordinatesEmbeddable.fromDto(dto.coords),
                eventType = dto.type.name,
                likedByMe = dto.likedByMe,
                participatedByMe = dto.participatedByMe,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                link = dto.link,
                ownedByMe = dto.ownedByMe,
            )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)

fun List<Event>.toEntity(): List<EventEntity> = map {
    EventEntity.fromDto(it)
}
