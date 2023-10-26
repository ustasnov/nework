package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.UserItem

@Entity(primaryKeys = ["id", "parentId"])
data class LikeOwnerEntity(
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
            LikeOwnerEntity(
                id = dto.id,
                parentId = dto.parentId,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}

fun List<LikeOwnerEntity>.toDto(): List<UserItem> = map(LikeOwnerEntity::toDto)
fun List<UserItem>.toLikeOwnersEntity(): List<LikeOwnerEntity> = map {
    LikeOwnerEntity.fromDto(it)
}