package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WallRemoteKeyEntity (
    @PrimaryKey
    val type: KeyType,
    val key: Long,
) {
   enum class KeyType {
       AFTER,
       BEFORE,
   }
}