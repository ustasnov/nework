package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LikeOwnerEntity(
    @PrimaryKey val id: Long,
    val postId: Long,
    val name: String,
    val avatar: String? = null
) {
    fun toDto() = UserItem(
        id = id,
        postId = postId,
        name = name,
        avatar = avatar,
    )

    companion object {
        fun fromDto(dto: UserItem) =
            LikeOwnerEntity(
                id = dto.id,
                postId = dto.postId,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}
