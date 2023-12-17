package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.UserItem

@Entity(primaryKeys = ["id", "parentId"])
data class EventLikeOwnerEntity(
    val id: Long,
    val parentId: Long,
    val name: String,
    val avatar: String?,
) {
    fun toDto() = UserItem(
        id = id,
        parentId = parentId,
        name = name,
        avatar = avatar,
    )

    companion object {
        fun fromDto(dto: UserItem) =
            EventLikeOwnerEntity(
                id = dto.id,
                parentId = dto.parentId,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}

fun List<EventLikeOwnerEntity>.toDto(): List<UserItem> = map(EventLikeOwnerEntity::toDto)
