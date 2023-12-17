package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.EventDao
import ru.netology.nmedia.dao.EventRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.EventRemoteKeyEntity
import ru.netology.nmedia.entity.EventWithLists
import ru.netology.nmedia.entity.toEntityWithLists
import ru.netology.nmedia.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, EventWithLists>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventWithLists>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatestEvents(state.config.initialLoadSize)
                }

                LoadType.PREPEND -> {
                    val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    apiService.getAfterEvents(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    apiService.getBeforeEvents(id, state.config.pageSize)
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
                        eventRemoteKeyDao.clear()
                        eventRemoteKeyDao.insert(
                            listOf(
                                EventRemoteKeyEntity(
                                    type = EventRemoteKeyEntity.KeyType.AFTER,
                                    key = body.first().id,
                                ),
                                EventRemoteKeyEntity(
                                    type = EventRemoteKeyEntity.KeyType.BEFORE,
                                    key = body.last().id,
                                ),
                            )
                        )
                    }

                    LoadType.PREPEND -> {
                        if (body.isNotEmpty()) {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    type = EventRemoteKeyEntity.KeyType.AFTER,
                                    key = body.first().id,
                                )
                            )
                        }
                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    type = EventRemoteKeyEntity.KeyType.BEFORE,
                                    key = body.last().id
                                )
                            )
                        }
                    }
                }

                eventDao.insertEventWithLists(body.toEntityWithLists())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }

}
