package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.dao.MentionDao
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.entity.MentionEntity
import javax.inject.Inject

class MentionsRepositotyImpl @Inject constructor(
    private val mentionDao: MentionDao
): MentionsRepository {
    override var data: Flow<List<UserItem>> =
        mentionDao.getPostMentions(-1).map { it.map(MentionEntity::toDto) }

    override fun getPostMentions(id: Long): Flow<List<UserItem>> {
        return  mentionDao.getPostMentions(id).map { it.map(MentionEntity::toDto) }
    }
}