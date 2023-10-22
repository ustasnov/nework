package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.UserItem

@Entity
data class SpeakerEntity(
    @PrimaryKey val id: Long,
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
fun List<UserItem>.toSpeakerEntity(): List<SpeakerEntity> = map {
    SpeakerEntity.fromDto(it)
}
