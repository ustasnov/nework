package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.User

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String?,
    val name: String,
    val avatar: String?,
) {
    fun toDto() = UserItem(
        id = id,
        postId = -1L,
        login = login,
        name = name,
        avatar = avatar,
    )

    companion object {
        fun fromDto(dto: UserItem) =
            UserEntity(
                id = dto.id,
                login = dto.login,
                name = dto.name,
                avatar = dto.avatar,
            )
    }
}

fun List<UserEntity>.toDto(): List<UserItem> = map(UserEntity::toDto)
fun List<UserItem>.toEntity(): List<UserEntity> = map {
    UserEntity.fromDto(it)
}