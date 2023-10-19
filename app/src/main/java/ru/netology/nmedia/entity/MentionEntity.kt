package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.UserItem

@Entity
data class MentionEntity(
    @PrimaryKey val id: Long,
    val postId: Long,
    val name: String,
    val avatar: String?,
) {
    fun toDto() = UserItem(
        id = id,
        postId = postId,
        name = name,
        avatar = avatar,
    )

    companion object {
        fun fromDto(dto: UserItem) =
            MentionEntity(
                id = dto.id,
                postId = dto.postId,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}

fun List<MentionEntity>.toDto(): List<UserItem> = map(MentionEntity::toDto)
fun List<UserItem>.toMentionsEntity(): List<LikeOwnerEntity> = map {
    LikeOwnerEntity.fromDto(it)
}