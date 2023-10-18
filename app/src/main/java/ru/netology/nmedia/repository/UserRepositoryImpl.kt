package ru.netology.nmedia.repository

import android.app.Application
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.UserDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.entity.UserEntity
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity
import ru.netology.nmedia.entity.UserItem
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    context: Application,
    private val userDao: UserDao,
    private val apiService: ApiService,
    appDb: AppDb,
) : UserRepository {

    override var data: Flow<List<UserItem>> =
        userDao.getAll().map { it.map(UserEntity::toDto) }

    override suspend fun getAll() {
        val response = apiService.getUsers()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val users = response.body() ?: throw RuntimeException("body is null")
        userDao.insert(users.map { UserEntity.fromDto(it) })
    }

    override fun getPostMentions(id: Long): Flow<List<UserItem>> {
        return userDao.getPostMentions(id).map {it.map(MentionEntity::toDto)}
    }

    override fun getLikeOwners(id: Long): Flow<List<UserItem>> {
        return userDao.getLikeOwners(id).map {it.map(LikeOwnerEntity::toDto)}
    }
}
