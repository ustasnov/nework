package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.WallDao
import ru.netology.nmedia.dao.WallRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.WallRemoteKeyEntity
import ru.netology.nmedia.entity.WallWithLists
import ru.netology.nmedia.entity.toWallEntityWithLists
import ru.netology.nmedia.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class WallPostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: WallDao,
    private val postRemoteKeyDao: WallRemoteKeyDao,
    private val appDb: AppDb,
    private val postSource: PostsSource
//) : RemoteMediator<Int, PostEntity>() {
) : RemoteMediator<Int, WallWithLists>() {

    override suspend fun load(
        loadType: LoadType,
        //state: PagingState<Int, PostEntity>
        state: PagingState<Int, WallWithLists>
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
                    when (postSource.sourceType) {
                        SourceType.WALL -> apiService.getLatestWallPosts(
                            postSource.authorId,
                            state.config.initialLoadSize
                        )

                        SourceType.MYWALL -> apiService.getLatestMyWallPosts(state.config.initialLoadSize)
                        else -> apiService.getLatestMyWallPosts(state.config.initialLoadSize)
                    }
                }

                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(
                        //endOfPaginationReached = false
                        endOfPaginationReached = true
                    )
                    when (postSource.sourceType) {
                        SourceType.WALL -> apiService.getAfterWallPosts(
                            postSource.authorId,
                            id,
                            state.config.pageSize
                        )

                        SourceType.MYWALL -> apiService.getAfterMyWallPosts(
                            id,
                            state.config.pageSize
                        )

                        else -> apiService.getAfterMyWallPosts(
                            id,
                            state.config.pageSize
                        )
                    }
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    when (postSource.sourceType) {
                        SourceType.WALL -> apiService.getBeforeWallPosts(
                            postSource.authorId,
                            id,
                            state.config.pageSize
                        )

                        SourceType.MYWALL -> apiService.getBeforeMyWallPosts(
                            id,
                            state.config.pageSize
                        )

                        else -> apiService.getBeforeMyWallPosts(
                            id,
                            state.config.pageSize
                        )
                    }
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

                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                listOf(
                                    WallRemoteKeyEntity(
                                        type = WallRemoteKeyEntity.KeyType.AFTER,
                                        key = body.first().id,
                                    ),
                                    WallRemoteKeyEntity(
                                        type = WallRemoteKeyEntity.KeyType.BEFORE,
                                        key = body.last().id,
                                    ),
                                )
                            )
                        }
                    }

                    LoadType.PREPEND -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                WallRemoteKeyEntity(
                                    type = WallRemoteKeyEntity.KeyType.AFTER,
                                    key = body.first().id,
                                )
                            )
                        }
                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                WallRemoteKeyEntity(
                                    type = WallRemoteKeyEntity.KeyType.BEFORE,
                                    key = body.last().id
                                )
                            )
                        }
                    }
                    //else -> Unit
                }

                //postDao.insert(body.map(PostEntity::fromDto))
                //postDao.insert(body.toEntity())
                postDao.insertPostsWithLists(body.toWallEntityWithLists())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}
