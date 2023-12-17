package ru.netology.nmedia.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dao.WallDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostWithLists
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.model.MediaModel
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    context: Application,
    private val postDao: PostDao,
    private val wallDao: WallDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val key = "newPostContent"
    private var newPostContentValue = MutableLiveData<String>()

    @OptIn(ExperimentalPagingApi::class)
    override var data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(
            pageSize = 300,
            //enablePlaceholders = false, initialLoadSize = 30, prefetchDistance = 10, maxSize = Int.MAX_VALUE, jumpThreshold = 1000),
            enablePlaceholders = false
        ),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
        )
    ).flow
        .map {
            it.map(PostWithLists::toDto)
        }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val posts = response.body() ?: throw RuntimeException("body is null")
            postDao.clearWithLists()
            postDao.insertPostsWithLists(posts.map { PostWithLists.fromDto(it) })
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            postDao.likeById(id)
            val response = apiService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insertPostWithLists(PostWithLists.fromDto(body))
            wallDao.likeById(id)
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
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insertPostWithLists(PostWithLists.fromDto(body))
            wallDao.unlikeById(id)
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
            postDao.insertPostWithLists(PostWithLists.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, mediaModel: MediaModel) {
        try {
            var media = Media(url = mediaModel.uri.toString())
            if (mediaModel.file != null) {
                media = uploadMedia(mediaModel)
            }

            val curPost = if (mediaModel.file != null) post.copy(
                attachment = Attachment(
                    media.url,
                    mediaModel.attachmentType!!
                )
            ) else post

            val response = apiService.save(curPost)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insertPostWithLists(PostWithLists.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun uploadMedia(model: MediaModel): Media {
        val response = apiService.uploadMedia(
            MultipartBody.Part.createFormData("file", "file", model.file!!.asRequestBody())
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
            apiService.removeById(id)
            postDao.removeById(id)
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
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

}
