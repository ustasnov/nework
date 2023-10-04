package ru.netology.nmedia.repository

import android.app.Application
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.UserDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.entity.UserEntity
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    context: Application,
    private val userDao: UserDao,
    private val apiService: ApiService,
    appDb: AppDb,
) : UserRepository {

    override val data: Flow<List<User>> =
        userDao.getAll().map { it.map(UserEntity::toDto) }

    override suspend fun getAll() {
        val response = apiService.getUsers()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val users = response.body() ?: throw RuntimeException("body is null")
        userDao.insert(users.map { UserEntity.fromDto(it) })
    }
}