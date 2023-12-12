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
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
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
    appAuth: AppAuth,
    private val postDao: PostDao,
    private val apiService: ApiService,
    val postRemoteKeyDao: PostRemoteKeyDao,
    val appDb: AppDb,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

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

    //var isNewPost = false

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _currentPostId = MutableLiveData<Long>()
    val currentPostId: LiveData<Long>
        get() = _currentPostId

    private val _currentPost =
        MutableLiveData(empty.copy())

    val currentPost: LiveData<Post>
        get() = _currentPost

    private val _currentMediaType =
        MutableLiveData(AttachmentType.IMAGE)

    val currentMediaType: LiveData<AttachmentType>
        get() = _currentMediaType


    init {
        //clearPosts()
        //loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            //repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            //repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
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
        //toggleNewPost(false)
        _edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (_edited.value?.content == text) {
            return
        }
        _edited.value = edited.value?.copy(content = text)
    }

    fun changeLink(link: String) {
        val text: String? = link.ifBlank { null }
        if (text != null && _edited.value?.link == text) {
            return
        }
        _edited.value = _edited.value?.copy(link = text)
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

    fun viewById(post: Post) {
        //toggleNewPost(false)
        _currentPost.setValue(post)
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

    fun clearPosts() = viewModelScope.launch {
        repository.clearPosts()
    }

    fun clearMediaType() {
        _currentMediaType.value = null
    }

    fun setMediaType(mediaType: AttachmentType) {
        _currentMediaType.value = mediaType
    }
}

