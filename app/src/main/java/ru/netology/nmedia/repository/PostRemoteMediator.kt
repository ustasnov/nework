package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.PostWithLists
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.entity.toEntityWithLists
import ru.netology.nmedia.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
//) : RemoteMediator<Int, PostEntity>() {
) : RemoteMediator<Int, PostWithLists>() {

    override suspend fun load(
        loadType: LoadType,
        //state: PagingState<Int, PostEntity>
        state: PagingState<Int, PostWithLists>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                /*
                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id != null && !postDao.isEmpty()) {
                        apiService.getAfter(id, state.config.pageSize)
                        //apiService.getBefore(id, state.config.pageSize)
                    } else {
                        apiService.getLatest(state.config.initialLoadSize)
                    }
                }*/
                LoadType.REFRESH -> {
                    apiService.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(
                        //endOfPaginationReached = false
                        endOfPaginationReached = true
                    )
                    apiService.getAfter(id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        /*
                        //postRemoteKeyDao.clear()
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    key = body.last().id,
                                )
                            )
                        }
                        */
                        postDao.clearWithLists()
                        postRemoteKeyDao.clear()
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    key = body.first().id,
                                ),
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    key = body.last().id,
                                ),
                            )
                        )
                    }
                    LoadType.PREPEND -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    key = body.first().id,
                                )
                            )
                        }
                    }
                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    key = body.last().id
                                )
                            )
                        }
                    }
                    //else -> Unit
                }

                //postDao.insert(body.map(PostEntity::fromDto))
                //postDao.insert(body.toEntity())
                postDao.insertPostsWithLists(body.toEntityWithLists())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}
