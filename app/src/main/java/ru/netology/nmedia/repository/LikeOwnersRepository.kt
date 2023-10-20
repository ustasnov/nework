package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.UserItem

interface LikeOwnersRepository {
    var data: Flow<List<UserItem>>

    fun getPostLikeOwners(id: Long): Flow<List<UserItem>>
}