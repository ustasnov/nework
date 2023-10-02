package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val data: Flow<List<User>>
    suspend fun getAll()
}