package ru.netology.nmedia.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import ru.netology.nmedia.dao.LikeOwnerDao
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.WallDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.PostWithLists
import ru.netology.nmedia.entity.WallWithLists
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.model.MediaModel
import java.io.IOException
import javax.inject.Inject

class WallRepositoryImpl @Inject constructor(
    context: Application,
    private val wallDao: WallDao,
    private val postDao: PostDao,
    private val likeOwnerDao: LikeOwnerDao,
    private val apiService: ApiService,
) : WallRepository {
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val key = "newWallPostContent"
    private var newPostContentValue = MutableLiveData<String>()

    override var data: Flow<List<Post>> =
        wallDao.getAll().map { it.map(WallWithLists::toDto) }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)

            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body().orEmpty()
            wallDao.insertPostsWithLists(posts.map { WallWithLists.fromDto(it) })
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
        //wallDao.clear()
        wallDao.insertPostsWithLists(posts.map { WallWithLists.fromDto(it) })
    }

    override suspend fun getMyWallPosts() {
        val response = apiService.getAllMyWallPosts()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        //wallDao.clear()
        wallDao.insertPostsWithLists(posts.map { WallWithLists.fromDto(it) })
    }

    override suspend fun likeById(id: Long) {
        try {
            wallDao.likeById(id)
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
            wallDao.unlikeById(id)
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
            wallDao.insertPostWithLists(WallWithLists.fromDto(body))
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
            wallDao.insertPostWithLists(WallWithLists.fromDto(body))
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
            //apiService.removeById(id)
            wallDao.removeByIdWithLists(id)
            //postDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun clearPosts() {
        try {
            //postDao.clearWithLists()
            wallDao.clear()
            //postRemoteKeyDao.clear()
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun insertLikeOwner(postId: Long, user: User) {
        likeOwnerDao.insert(
            LikeOwnerEntity.fromDto(
                UserItem(
                    id = user.id,
                    parentId = postId,
                    name = user.name,
                    avatar = user.avatar
                )
            )
        )
    }

    override suspend fun removeLikeOwner(postId: Long, userId: Long) {
        likeOwnerDao.removeById(userId, postId)
    }

}


