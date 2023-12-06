package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    var data: Flow<List<User>>
    suspend fun getAll()
    suspend fun getUser(id: Long): User
    suspend fun setChecked(id: Long, choice: Boolean)
    suspend fun clearAllChecks()

}