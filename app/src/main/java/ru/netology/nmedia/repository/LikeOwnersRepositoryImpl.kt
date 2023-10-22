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
        likeOwnerDao.getLikeOwners(-1).map { it.map(LikeOwnerEntity::toDto) }

    override fun getLikeOwners(id: Long): Flow<List<UserItem>> {
        return  likeOwnerDao.getLikeOwners(id).map { it.map(LikeOwnerEntity::toDto) }
    }
}