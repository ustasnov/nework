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
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.WallItem
import ru.netology.nmedia.entity.WallWithLists
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.repository.WallPostRemoteMediator
import ru.netology.nmedia.repository.WallRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProfileViewModel @Inject constructor(
    val postRemoteKeyDao: WallRemoteKeyDao,
    val appDb: AppDb,
) : ViewModel() {

    private val _postSource = MutableLiveData(emptyPostSource.copy())
    val postSource: LiveData<PostsSource>
        get() = _postSource

    /*
    fun setPostSource(authorI: User, sourceType: SourceType) {
        _postSource.postValue(PostsSource(author, sourceType))
    }
     */

    private val _sourceType = MutableLiveData(SourceType.WALL)
    val sourceType: LiveData<SourceType>
        get() = _sourceType

    private val _currentUserId = MutableLiveData<Long>()
    val currentUserId: LiveData<Long>
        get() = _currentUserId

    fun setPostSource(postSource: PostsSource) {
        _postSource.value = postSource
    }

    fun setSourceType(sourceType: SourceType) {
        _sourceType.value = sourceType
    }
}

