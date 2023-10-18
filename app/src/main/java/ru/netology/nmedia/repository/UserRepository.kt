package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.User
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.UserItem

interface UserRepository {
    var data: Flow<List<UserItem>>
    suspend fun getAll()
    fun getPostMentions(id: Long): Flow<List<UserItem>>
    fun getLikeOwners(id: Long): Flow<List<UserItem>>
}