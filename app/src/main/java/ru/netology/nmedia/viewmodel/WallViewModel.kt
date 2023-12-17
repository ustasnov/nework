package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.repository.WallRepository
import javax.inject.Inject

val emptyPostSource = PostsSource(
    authorId = 0L,
    sourceType = null
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WallViewModel @Inject constructor(
    private val repository: WallRepository,
    val appDb: AppDb,
) : ViewModel() {

    var data: LiveData<FeedModel> =
        repository.data.map(::FeedModel).asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _media = MutableLiveData<MediaModel?>()
    val media: LiveData<MediaModel?>
        get() = _media

    val edited = MutableLiveData(empty)

    private val _postSource = MutableLiveData(emptyPostSource.copy())
    val postSource: LiveData<PostsSource>
        get() = _postSource

    fun loadWallPosts(ownerId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAllWallPosts(ownerId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refreshWall(ownerId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAllWallPosts(ownerId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun loadMyWallPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getMyWallPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refreshMyWall() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getMyWallPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refresh() = viewModelScope.launch {
        if (postSource.value!!.sourceType == SourceType.MYWALL) {
            refreshMyWall()
        } else {
            refreshWall(postSource.value!!.authorId!!)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeById(post: Post, user: User) = viewModelScope.launch {
        try {
            if (!post.likedByMe) {
                repository.likeById(post.id)
                repository.insertLikeOwner(post.id, user)
            } else {
                repository.unlikeById(post.id)
                repository.removeLikeOwner(post.id, user.id)
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

    fun clearMedia() {
        _media.value = null
    }

    fun clearPosts() = viewModelScope.launch {
        repository.clearPosts()
    }

    fun setPostSource(authorId: Long, sourceType: SourceType) {
        _postSource.setValue(PostsSource(authorId, sourceType))
    }

}

