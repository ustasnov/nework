package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.EventLikeOwnerDao
import ru.netology.nmedia.dao.LikeOwnerDao
import ru.netology.nmedia.dao.MentionDao
import ru.netology.nmedia.dao.ParticipantDao
import ru.netology.nmedia.dao.SpeakerDao
import ru.netology.nmedia.dao.UserDao
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.entity.EventLikeOwnerEntity
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity
import ru.netology.nmedia.entity.ParticipantEntity
import ru.netology.nmedia.entity.SpeakerEntity
import ru.netology.nmedia.entity.UserEntity
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val mentionDao: MentionDao,
    private val likeOwnerDao: LikeOwnerDao,
    private val eventLikeOwnerDao: EventLikeOwnerDao,
    private val participantDao: ParticipantDao,
    private val speakerDao: SpeakerDao,
    private val apiService: ApiService,
) : UserRepository {

    override val data: Flow<List<User>> =
        userDao.getAll().map { it.map(UserEntity::toDto) }

    override val mentionsData: Flow<List<UserItem>> = emptyFlow()
    override fun getMentions(id: Long): Flow<List<UserItem>> {
        return mentionDao.getMentions(id).map { it.map(MentionEntity::toDto) }
    }

    override val likeOwnersData: Flow<List<UserItem>> = emptyFlow()
    override fun getLikeOwners(id: Long): Flow<List<UserItem>> {
        return likeOwnerDao.getLikeOwners(id).map { it.map(LikeOwnerEntity::toDto) }
    }

    override val eventLikeOwnersData: Flow<List<UserItem>> = emptyFlow()
    override fun getEventLikeOwners(id: Long): Flow<List<UserItem>> {
        return eventLikeOwnerDao.getLikeOwners(id).map { it.map(EventLikeOwnerEntity::toDto) }
    }

    override val participantsData: Flow<List<UserItem>> = emptyFlow()
    override fun getParticipants(id: Long): Flow<List<UserItem>> {
        return participantDao.getParticipants(id).map { it.map(ParticipantEntity::toDto) }
    }

    override val speakersData: Flow<List<UserItem>> = emptyFlow()
    override fun getSpeakers(id: Long): Flow<List<UserItem>> {
        return speakerDao.getSpeakers(id).map { it.map(SpeakerEntity::toDto) }
    }

    override suspend fun getAll() {
        val response = apiService.getUsers()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val users = response.body() ?: throw RuntimeException("body is null")
        userDao.insert(users.map { UserEntity.fromDto(it) })
    }

    override suspend fun getUser(id: Long): User {
        val response = apiService.getUser(id)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        return response.body() ?: throw RuntimeException("body is null")
    }

    override suspend fun setChecked(id: Long, choice: Boolean) {
        if (choice) {
            userDao.setChecked(id)
        } else {
            userDao.setUnChecked(id)
        }
    }

    override suspend fun clearAllChecks() {
        userDao.clearAllChecks()
    }

}
