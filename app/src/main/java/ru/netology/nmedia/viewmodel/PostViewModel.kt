package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

val empty = Post(
    id = 0L,
    author = "",
    authorId = 0L,
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    likes = 0.0,
    shared = 0.0,
    views = 0.0,
    video = ""
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    val data: Flow<PagingData<FeedItem>> = appAuth.data
        .flatMapLatest { token ->
            repository.data.map { posts ->
                posts.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == token?.id)
                    } else {
                        post
                    }
                }
            }
    }.flowOn(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData<PhotoModel?>()
    val photo: LiveData<PhotoModel?>
        get() = _photo

    val edited = MutableLiveData(empty)
    var isNewPost = false

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _currentPostId = MutableLiveData<Long>()
    val currentPostId: LiveData<Long>
        get() = _currentPostId

    private val _currentPost =
        MutableLiveData(
            Post(
                0,
                "",
                0L,
                "",
                "",
                "",
                false,
                0.0,
                0.0,
                0.0,
                null,
            )
        )
    val currentPost: LiveData<Post>
        get() = _currentPost

    init {
        loadPosts()
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
            edited.value?.let {
                when (val photo = _photo.value) {
                    null -> repository.save(it.copy(ownedByMe = true))
                    else -> repository.saveWithAttachment(it.copy(ownedByMe = true), photo)
                }
            }
            edited.value = empty
            _postCreated.postValue(Unit)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.SAVE)
        }
    }

    fun edit(post: Post) {
        toggleNewPost(false)
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun toggleNewPost(isNew: Boolean) {
        isNewPost = isNew
    }

    fun likeById(post: Post) = viewModelScope.launch {
        _currentPost.setValue(post)
        try {
            if (_currentPost.value?.likedByMe == false) {
                repository.likeById(_currentPost.value!!.id)
            } else {
                repository.unlikeById(_currentPost.value!!.id)
            }
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LIKE)
        }
    }

    fun shareById(id: Long) = viewModelScope.launch {
        _currentPostId.setValue(id)
        repository.shareById(_currentPostId.value!!)
    }

    fun viewById(id: Long) {
        toggleNewPost(false)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        _currentPostId.setValue(id)
        try {
            repository.removeById(_currentPostId.value!!)
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

    fun clearPhoto() {
        _photo.value = null
    }

    fun setPhoto(photoModel: PhotoModel) {
        _photo.value = photoModel
    }
}
