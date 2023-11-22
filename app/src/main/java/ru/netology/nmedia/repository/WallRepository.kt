package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.entity.PostWithLists
import ru.netology.nmedia.entity.WallWithLists
import ru.netology.nmedia.model.PhotoModel

interface WallRepository {
    //var data: Flow<PagingData<FeedItem>>
    var data: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAllWallPosts(authorId: Long)
    suspend fun getMyWallPosts()
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun uploadMedia(model: PhotoModel): Media
    suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel)
    fun saveNewPostContent(text: String)
    fun getNewPostContent(): LiveData<String>
    suspend fun clearPosts()
    suspend fun insertLikeOwner(postId: Long, user: User)
    suspend fun removeLikeOwner(postId: Long, userId: Long)
}
