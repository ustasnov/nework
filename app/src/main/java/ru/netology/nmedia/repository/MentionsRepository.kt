package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.UserItem

interface MentionsRepository {
    var data: Flow<List<UserItem>>

    fun getPostMentions(id: Long): Flow<List<UserItem>>
}