package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.dao.LikeOwnerDao
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.entity.LikeOwnerEntity
import javax.inject.Inject

class LikeOwnersRepositoryImpl  @Inject constructor(
    private val likeOwnerDao: LikeOwnerDao
): LikeOwnersRepository {
    override var data: Flow<List<UserItem>> =
        likeOwnerDao.getPostLikeOwners(-1).map { it.map(LikeOwnerEntity::toDto) }

    override fun getPostLikeOwners(id: Long): Flow<List<UserItem>> {
        return  likeOwnerDao.getPostLikeOwners(id).map { it.map(LikeOwnerEntity::toDto) }
    }
}