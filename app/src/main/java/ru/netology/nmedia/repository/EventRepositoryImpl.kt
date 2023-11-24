package ru.netology.nmedia.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.EventDao
import ru.netology.nmedia.dao.EventRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.entity.EventEntity
import ru.netology.nmedia.entity.EventWithLists
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    context: Application,
    private val eventDao: EventDao,
    private val apiService: ApiService,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
) : EventRepository {
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val key = "newEventContent"
    private var newEventContentValue = MutableLiveData<String>()

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 300,
            //enablePlaceholders = false, initialLoadSize = 30, prefetchDistance = 10, maxSize = Int.MAX_VALUE, jumpThreshold = 1000),
            enablePlaceholders = false),
        pagingSourceFactory = { eventDao.getPagingSource() },
        remoteMediator = EventRemoteMediator(
            apiService = apiService,
            eventDao = eventDao,
            eventRemoteKeyDao = eventRemoteKeyDao,
            appDb = appDb
        )
    ).flow
        //.map { it.map(PostEntity::toDto)
        .map { it.map(EventWithLists::toDto)
            /*
            .insertSeparators { previous, _ ->
                if (previous?.id?.rem(5) == 0L) {
                    Ad(Random.nextLong(), "figma.jpg")
                } else {
                    null
                }
            }
            */
        }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)

            val response = apiService.getNewerEvents(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val events = response.body().orEmpty()
            //eventDao.insert(events.toEntity())
            eventDao.insertEventWithLists(events.map { EventWithLists.fromDto(it) })
            emit(events.size)
        }
    }.catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        val response = apiService.getAllEvents()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val events = response.body() ?: throw RuntimeException("body is null")
        eventDao.insertEventWithLists(events.map { EventWithLists.fromDto(it) })
    }

    override suspend fun likeById(id: Long) {
        try {
            eventDao.likeById(id)
            val response = apiService.likeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            eventDao.unlikeById(id)
            val response = apiService.unlikeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun save(event: Event) {
        try {
            val response = apiService.saveEvent(event.copy(attachment = null))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insertEventWithLists(EventWithLists.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun saveWithAttachment(event: Event, photoModel: PhotoModel) {
        try {
            val media = uploadMedia(photoModel)

            val response = apiService.saveEvent(
                event.copy(
                    attachment = Attachment(
                        media.url,
                        //"",§
                        AttachmentType.IMAGE
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insertEventWithLists(EventWithLists.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun uploadMedia(model: PhotoModel): Media {
        val response = apiService.uploadMedia(
            MultipartBody.Part.createFormData("file", "file", model.file.asRequestBody())
        )

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return requireNotNull(response.body())
    }

    override fun saveNewEventContent(text: String) {
        newEventContentValue.value = text
        with(prefs.edit()) {
            putString(key, newEventContentValue.value)
            apply()
        }
    }

    override fun getNewEventContent(): LiveData<String> {
        prefs.getString(key, "")?.let {
            newEventContentValue.value = it
        }
        return newEventContentValue
    }

    override suspend fun removeById(id: Long) {
        try {
            eventDao.removeById(id)
            apiService.removeEventById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }
}