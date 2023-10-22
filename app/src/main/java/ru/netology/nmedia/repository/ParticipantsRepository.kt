package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.UserItem

interface ParticipantsRepository {
    var data: Flow<List<UserItem>>

    fun getParticipants(id: Long): Flow<List<UserItem>>
}