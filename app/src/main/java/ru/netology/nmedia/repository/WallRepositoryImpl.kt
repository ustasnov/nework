package ru.netology.nmedia.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dao.WallDao
import ru.netology.nmedia.dao.WallRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.WallWithLists
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException
import javax.inject.Inject

class WallRepositoryImpl @Inject constructor(
    context: Application,
    private val postDao: WallDao,
    private val apiService: ApiService,
    private val postRemoteKeyDao: WallRemoteKeyDao,
    appDb: AppDb,
) : WallRepository {
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val key = "newWallPostContent"
    private var newPostContentValue = MutableLiveData<String>()

    @OptIn(ExperimentalPagingApi::class)
    override var data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 300,
            //enablePlaceholders = false, initialLoadSize = 30, prefetchDistance = 10, maxSize = Int.MAX_VALUE, jumpThreshold = 1000),
            enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = WallPostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
            PostsSource(0, SourceType.POSTS),
        )
    ).flow
        .map { it.map(WallWithLists::toDto)
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)

            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body().orEmpty()
            postDao.insertPostsWithLists(posts.map { WallWithLists.fromDto(it) })
            emit(posts.size)

        }
    }.catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun getAllWallPosts(authorId: Long) {
        val response = apiService.getAllWallPosts(authorId)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        postDao.insertPostsWithLists(posts.map { WallWithLists.fromDto(it) })
    }

    override suspend fun likeById(id: Long) {
        try {
            postDao.likeById(id)
            val response = apiService.likeById(id)
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
            postDao.unlikeById(id)
            val response = apiService.unlikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post.copy(attachment = null))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insertPostWithLists(WallWithLists.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel) {
        try {
            val media = uploadMedia(photoModel)

            val response = apiService.save(
                post.copy(
                    attachment = Attachment(
                        media.id,
                        //"",§
                        AttachmentType.IMAGE
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insertPostWithLists(WallWithLists.fromDto(body))
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

    override fun saveNewPostContent(text: String) {
        newPostContentValue.value = text
        with(prefs.edit()) {
            putString(key, newPostContentValue.value)
            apply()
        }
    }

    override fun getNewPostContent(): LiveData<String> {
        prefs.getString(key, "")?.let {
            newPostContentValue.value = it
        }
        return newPostContentValue
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            apiService.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun clearPosts() {
        try {
            //postDao.clearWithLists()
            postDao.clear()
            postRemoteKeyDao.clear()
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }
}

