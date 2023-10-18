package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    var data: Flow<List<User>>
    suspend fun getAll()
}