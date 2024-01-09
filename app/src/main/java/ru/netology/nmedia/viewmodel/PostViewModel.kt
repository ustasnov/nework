package ru.netology.nmedia.viewmodel

import android.os.Parcelable
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostCash
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

val empty = Post(
    id = 0L,
    authorId = 0L,
    author = "",
    authorAvatar = null,
    authorJob = null,
    content = "",
    published = "",
    coords = null,
    link = null,
    likeOwnerIds = emptyList(),
    mentionIds = emptyList(),
    mentionedMe = false,
    likedByMe = false,
    attachment = null,
    ownedByMe = false,
    users = mutableMapOf(),
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    val data: Flow<PagingData<Post>> = appAuth.data
        .flatMapLatest { token ->
            repository.data.map { posts ->
                posts.map { post ->
                    post.copy(ownedByMe = post.authorId == token?.id)
                }
            }
        }.flowOn(Dispatchers.Default)

    private lateinit var state: Parcelable

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _media = MutableLiveData<MediaModel?>()
    val media: LiveData<MediaModel?>
        get() = _media

    private val _edited = MutableLiveData<Post>()
    val edited: LiveData<Post>
        get() = _edited

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    var newPostCash: PostCash? = null

    private val _currentMediaType =
        MutableLiveData(AttachmentType.IMAGE)

    val currentMediaType: LiveData<AttachmentType>
        get() = _currentMediaType

    private val _refreshList = SingleLiveEvent<Unit>()
    val refreshList: LiveData<Unit>
        get() = _refreshList


    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refreshList() {
        _refreshList.postValue(Unit)
    }

    fun save() = viewModelScope.launch {
        try {
            _edited.value?.let {
                val media = _media.value
                if (media != null) {
                    repository.saveWithAttachment(it.copy(ownedByMe = true), media)
                } else {
                    repository.save(it.copy(ownedByMe = true))
                }
            }
            _edited.value = empty
            _postCreated.postValue(Unit)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.SAVE)
        }
    }

    fun edit(post: Post) {
        _edited.value = post
    }

    fun likeById(post: Post) = viewModelScope.launch {
        try {
            if (!post.likedByMe) {
                repository.likeById(post.id)
            } else {
                repository.unlikeById(post.id)
            }
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LIKE)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.REMOVE)
        }
    }

    fun saveNewPostContent(text: String) {
        repository.saveNewPostContent(text)
    }

    fun getNewPostCont(): LiveData<String> {
        return repository.getNewPostContent()
    }

    fun clearMedia() {
        _media.value = null
    }

    fun setMedia(mediaModel: MediaModel) {
        _media.value = mediaModel
    }

    fun setMediaType(mediaType: AttachmentType) {
        _currentMediaType.value = mediaType
    }

}

