package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.entity.UserItem
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.UserModel
import ru.netology.nmedia.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    var data: LiveData<UserModel> =
        repository.data.map(::UserModel).asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    init {
        //loadUsers()
    }

    fun loadUsers() = viewModelScope.launch {
        _dataState.value = FeedModelState(loading = true)
        try {
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun loadMentors(id: Long?): LiveData<UserModel> {
        return repository.getPostMentions(id!!).map(::UserModel).asLiveData(Dispatchers.Default)
    }

    fun loadLikeOwners(id: Long?): LiveData<UserModel> {
        return repository.getLikeOwners(id!!).map(::UserModel).asLiveData(Dispatchers.Default)
    }

    fun refresh() = viewModelScope.launch {
        _dataState.value = FeedModelState(refreshing = true)
        try {
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun viewById(id: Long) {
        //toggleNewPost(false)
    }


    fun setDataByType(dataType: String?, id: Long?): LiveData<UserModel> {
        when (dataType) {
            "all" ->  return repository.data.map(::UserModel).asLiveData(Dispatchers.Default)
            "mentions" -> return repository.getPostMentions(id!!).map(::UserModel).asLiveData(Dispatchers.Default)
            "likeOwners" -> return repository.getLikeOwners(id!!).map(::UserModel).asLiveData(Dispatchers.Default)
            else -> return repository.data.map(::UserModel).asLiveData(Dispatchers.Default)
        }

    }
}