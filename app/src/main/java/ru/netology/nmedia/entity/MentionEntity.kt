package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.UserItem

@Entity(primaryKeys = ["id", "parentId"])
data class MentionEntity(
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
            MentionEntity(
                id = dto.id,
                parentId = dto.parentId,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}

fun List<MentionEntity>.toDto(): List<UserItem> = map(MentionEntity::toDto)
fun List<UserItem>.toMentionsEntity(): List<MentionEntity> = map {
    MentionEntity.fromDto(it)
}