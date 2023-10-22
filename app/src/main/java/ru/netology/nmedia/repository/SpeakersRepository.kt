package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.UserItem

interface SpeakersRepository {
    var data: Flow<List<UserItem>>

    fun getSpeakers(id: Long): Flow<List<UserItem>>
}