package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.dao.SpeakerDao
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.entity.SpeakerEntity
import javax.inject.Inject

class SpeakersRepositoryImpl @Inject constructor(
    private val speakerDao: SpeakerDao
): SpeakersRepository {
    override var data: Flow<List<UserItem>> =
        speakerDao.getSpeakers(-1).map { it.map(SpeakerEntity::toDto) }

    override fun getSpeakers(id: Long): Flow<List<UserItem>> {
        return  speakerDao.getSpeakers(id).map { it.map(SpeakerEntity::toDto) }
    }
}