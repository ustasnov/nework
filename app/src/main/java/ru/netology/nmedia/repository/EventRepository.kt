package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.model.MediaModel

interface EventRepository {
    val data: Flow<PagingData<FeedItem>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAll()
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(event: Event)
    suspend fun uploadMedia(model: MediaModel): Media
    suspend fun saveWithAttachment(event: Event, mediaModel: MediaModel)
    fun saveNewEventContent(text: String)
    fun getNewEventContent(): LiveData<String>
}