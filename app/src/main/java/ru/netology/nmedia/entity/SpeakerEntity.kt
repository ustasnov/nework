package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.UserItem

@Entity(primaryKeys = ["id", "parentId"])
data class SpeakerEntity(
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
            SpeakerEntity(
                id = dto.id,
                parentId = dto.parentId,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}

fun List<SpeakerEntity>.toDto(): List<UserItem> = map(SpeakerEntity::toDto)
