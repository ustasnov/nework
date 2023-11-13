package ru.netology.nmedia.viewmodel

import android.os.Parcelable
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.WallDao
import ru.netology.nmedia.dao.WallRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.WallWithLists
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.WallPostRemoteMediator
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

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WallViewModel @Inject constructor(
    private val repository: WallRepository,
    appAuth: AppAuth,
    private val postDao: WallDao,
    private val apiService: ApiService,
    val postRemoteKeyDao: WallRemoteKeyDao,
    val appDb: AppDb,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

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

    private lateinit var state: Parcelable

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
        MutableLiveData(empty.copy())

    val currentPost: LiveData<Post>
        get() = _currentPost


    init {
        //clearPosts()
        //loadPosts()
    }


    fun setData(postSource: PostsSource) {
        @OptIn(ExperimentalPagingApi::class)
        repository.data = Pager(
            config = PagingConfig(pageSize = 300,
                //enablePlaceholders = false, initialLoadSize = 30, prefetchDistance = 10, maxSize = Int.MAX_VALUE, jumpThreshold = 1000),
                enablePlaceholders = false),
            pagingSourceFactory = { postDao.getPagingSource() },
            remoteMediator = WallPostRemoteMediator(
                apiService = apiService,
                postDao = postDao,
                postRemoteKeyDao = postRemoteKeyDao,
                appDb = appDb,
                postSource = postSource
            )
        ).flow
            .map { it.map(WallWithLists::toDto) }
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

    fun viewById(post: Post) {
        toggleNewPost(false)
        _currentPost.setValue(post)
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

    fun clearPosts() = viewModelScope.launch {
        repository.clearPosts()
    }

    /*
    fun saveRecyclerViewState(parcelable: Parcelable) { state = parcelable }
    fun restoreRecyclerViewState() : Parcelable = state
    fun stateInitialized() : Boolean = ::state.isInitialized

     */
}

