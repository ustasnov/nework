package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.MediaModel

interface WallRepository_1 {
    var data: Flow<PagingData<FeedItem>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAllWallPosts(authorId: Long)
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun uploadMedia(model: MediaModel): Media
    suspend fun saveWithAttachment(post: Post, mediaModel: MediaModel)
    fun saveNewPostContent(text: String)
    fun getNewPostContent(): LiveData<String>
    suspend fun clearPosts()
}
