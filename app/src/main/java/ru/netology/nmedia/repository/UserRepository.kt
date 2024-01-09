package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.UserItem

interface UserRepository {
    val data: Flow<List<User>>
    val mentionsData: Flow<List<UserItem>>
    val likeOwnersData: Flow<List<UserItem>>
    val eventLikeOwnersData: Flow<List<UserItem>>
    val participantsData: Flow<List<UserItem>>
    val speakersData: Flow<List<UserItem>>

    suspend fun getAll()
    suspend fun getUser(id: Long): User
    suspend fun setChecked(id: Long, choice: Boolean)
    suspend fun clearAllChecks()
    fun getMentions(id: Long): Flow<List<UserItem>>
    fun getLikeOwners(id: Long): Flow<List<UserItem>>
    fun getEventLikeOwners(id: Long): Flow<List<UserItem>>
    fun getParticipants(id: Long): Flow<List<UserItem>>
    fun getSpeakers(id: Long): Flow<List<UserItem>>

}