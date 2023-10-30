package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.UserItem

interface LikeOwnersRepository {
    var data: Flow<List<UserItem>>

    fun getLikeOwners(id: Long): Flow<List<UserItem>>
}