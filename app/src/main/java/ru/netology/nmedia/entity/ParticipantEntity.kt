package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.UserItem

@Entity
data class ParticipantEntity(
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
            ParticipantEntity(
                id = dto.id,
                parentId = dto.parentId,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}

fun List<ParticipantEntity>.toDto(): List<UserItem> = map(ParticipantEntity::toDto)
fun List<UserItem>.toParticipantEntity(): List<ParticipantEntity> = map {
    ParticipantEntity.fromDto(it)
}
