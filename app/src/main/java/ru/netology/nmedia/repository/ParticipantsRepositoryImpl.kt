package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.dao.ParticipantDao
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.entity.ParticipantEntity
import javax.inject.Inject

class ParticipantsRepositoryImpl @Inject constructor(
    private val participantDao: ParticipantDao
): ParticipantsRepository {
    override var data: Flow<List<UserItem>> =
        participantDao.getParticipants(-1).map { it.map(ParticipantEntity::toDto) }

    override fun getParticipants(id: Long): Flow<List<UserItem>> {
        return  participantDao.getParticipants(id).map { it.map(ParticipantEntity::toDto) }
    }
}