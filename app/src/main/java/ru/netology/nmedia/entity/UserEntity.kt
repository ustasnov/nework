package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.User

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
    val checked: Boolean,
) {
    fun toDto() = User(
        id = id,
        login = login,
        name = name,
        avatar = avatar,
        checked = checked
    )

    companion object {
        fun fromDto(dto: User) =
            UserEntity(
                id = dto.id,
                login = dto.login,
                name = dto.name,
                avatar = dto.avatar,
                checked = dto.checked
            )
    }
}

fun List<UserEntity>.toDto(): List<User> = map(UserEntity::toDto)
fun List<User>.toEntity(): List<UserEntity> = map {
    UserEntity.fromDto(it)
}