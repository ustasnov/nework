package ru.netology.nmedia.viewmodel

import android.os.Parcelable
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.dao.WallRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.WallItem
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.repository.WallRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

val emptyWall = Post(
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

val emptyWallItem = WallItem(
    ownerId = 0L,
    type = null
)

val emptyPostSource = PostsSource(
    authorId = 0L,
    sourceType = null
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WallViewModel @Inject constructor(
    private val repository: WallRepository,
    val postRemoteKeyDao: WallRemoteKeyDao,
    val appDb: AppDb,
) : ViewModel() {

    var data: LiveData<FeedModel> =
        repository.data.map(::FeedModel).asLiveData(Dispatchers.Default)

    private lateinit var state: Parcelable

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _media = MutableLiveData<MediaModel?>()
    val media: LiveData<MediaModel?>
        get() = _media

    val edited = MutableLiveData(empty)
    var isNewPost = false

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

    private val _wallItem = MutableLiveData(emptyWallItem.copy())
    val wallItem: LiveData<WallItem>
        get() = _wallItem

    private val _postSource = MutableLiveData(emptyPostSource.copy())
    val postSource: LiveData<PostsSource>
        get() = _postSource

    private val _currentMediaType =
        MutableLiveData(AttachmentType.IMAGE)

    val currentMediaType: LiveData<AttachmentType>
        get() = _currentMediaType

    init {
        //clearPosts()
        //loadPosts()
    }

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

    /*
    fun save() = viewModelScope.launch {
        try {
            edited.value?.let {
                val media = _media.value
                if (media != null) {
                    repository.saveWithAttachment(it.copy(ownedByMe = true), media)
                } else {
                    repository.save(it.copy(ownedByMe = true))
                }
            }
            edited.value = empty
            _postCreated.postValue(Unit)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.SAVE)
        }
    }
     */

    fun edit(post: Post) {
        //toggleNewPost(false)
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changeLink(link: String) {
        val text = link.trim()
        if (edited.value?.link == text) {
            return
        }
        edited.value = edited.value?.copy(link = text)
    }

    fun toggleNewPost(isNew: Boolean) {
        isNewPost = isNew
    }

    fun likeById(post: Post, user: User) = viewModelScope.launch {
        //_currentPost.value = post
        try {
            //if (_currentPost.value?.likedByMe == false) {
            if (!post.likedByMe) {
                //repository.likeById(_currentPost.value!!.id)
                repository.likeById(post.id)
                repository.insertLikeOwner(post.id, user)
            } else {
                //repository.unlikeById(_currentPost.value!!.id)
                repository.unlikeById(post.id)
                repository.removeLikeOwner(post.id, user.id)
            }
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LIKE)
        }
    }

    fun viewById(post: Post) {
        toggleNewPost(false)
        _currentPost.setValue(post)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        //_currentPostId.setValue(id)
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

    /*
    fun saveRecyclerViewState(parcelable: Parcelable) { state = parcelable }
    fun restoreRecyclerViewState() : Parcelable = state
    fun stateInitialized() : Boolean = ::state.isInitialized

     */

    fun setWallItem(id: Long, type: String) {
        _wallItem.postValue(WallItem(id, type))
    }

    fun setPostSource(authorId: Long, sourceType: SourceType) {
        _postSource.setValue(PostsSource(authorId, sourceType))
    }

    fun clearMediaType() {
        _currentMediaType.value = null
    }

    fun setMediaType(mediaType: AttachmentType) {
        _currentMediaType.value = mediaType
    }
}

