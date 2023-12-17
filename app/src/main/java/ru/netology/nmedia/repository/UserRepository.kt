package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.UserItem

interface UserRepository {
    var data: Flow<List<User>>
    var mentionsData: Flow<List<UserItem>>
    var likeOwnersData: Flow<List<UserItem>>
    var eventLikeOwnersData: Flow<List<UserItem>>
    var participantsData: Flow<List<UserItem>>
    var speakersData: Flow<List<UserItem>>

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