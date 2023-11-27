package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.dao.WallRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
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

