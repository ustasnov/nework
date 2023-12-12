package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.UserItemModel
import ru.netology.nmedia.model.UserModel
import ru.netology.nmedia.model.UsersSelectModel
import ru.netology.nmedia.repository.UserRepository
import javax.inject.Inject

val emptyUser = User(
    id = 0L,
    login = "",
    name = "",
    avatar = null,
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    var data: LiveData<UserModel> =
        repository.data.map(::UserModel).asLiveData(Dispatchers.Default)

    var mentionsData: LiveData<UserItemModel> =
        repository.mentionsData.map(::UserItemModel).asLiveData(Dispatchers.Default)

    var likeOwnersData: LiveData<UserItemModel> =
        repository.likeOwnersData.map(::UserItemModel).asLiveData(Dispatchers.Default)

    var eventLikeOwnersData: LiveData<UserItemModel> =
        repository.eventLikeOwnersData.map(::UserItemModel).asLiveData(Dispatchers.Default)

    var participantsData: LiveData<UserItemModel> =
        repository.participantsData.map(::UserItemModel).asLiveData(Dispatchers.Default)

    var speakersData: LiveData<UserItemModel> =
        repository.speakersData.map(::UserItemModel).asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _currentUserId = MutableLiveData<Long>()
    val currentUserId: LiveData<Long>
        get() = _currentUserId

    private val _currentUser = MutableLiveData(emptyUser.copy())
    val currentUser: LiveData<User>
        get() = _currentUser

    private val _forSelection = MutableLiveData<UsersSelectModel>()
    val forSelection: LiveData<UsersSelectModel>
        get() = _forSelection

    private val _checkList =  MutableLiveData<List<User>?>()
    val checkList: LiveData<List<User>?>
        get() = _checkList

    /*
    init {
        loadUsers()
    }
     */

    fun getMentions(id: Long) {
        mentionsData = repository.getMentions(id).map(::UserItemModel).asLiveData(Dispatchers.Default)
    }

    fun getLikeOwners(id: Long) {
        likeOwnersData = repository.getLikeOwners(id).map(::UserItemModel).asLiveData(Dispatchers.Default)
    }

    fun getEventLikeOwners(id: Long) {
        eventLikeOwnersData = repository.getEventLikeOwners(id).map(::UserItemModel).asLiveData(Dispatchers.Default)
    }

    fun getParticipants(id: Long) {
        participantsData = repository.getParticipants(id).map(::UserItemModel).asLiveData(Dispatchers.Default)
    }

    fun getSpeakers(id: Long) {
        speakersData = repository.getSpeakers(id).map(::UserItemModel).asLiveData(Dispatchers.Default)
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

    fun refresh() = viewModelScope.launch {
        _dataState.value = FeedModelState(refreshing = true)
        try {
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun viewUser(user: User) {
        _currentUserId.postValue(user.id)
        _currentUser.postValue(user)
        //println("From UserViewModel.viewUser: ${user}")
    }

    fun getUserById(id: Long) = viewModelScope.launch {
        try {
            //println("From viewModel getUserById(): ${id}")
            val user = repository.getUser(id)
            //println("From viewModel getUserById(): ${user}")
            viewUser(user)
        } catch (e: Exception) {
            println(e.message)
        }
    }

    fun setForSelection(title: String, choice: Boolean, type: String) {
        _forSelection.value = UsersSelectModel(title, choice, type)
    }

    fun setChecked(id: Long, choice: Boolean) = viewModelScope.launch {
        repository.setChecked(id, choice)
    }

    fun clearAllChecks()  = viewModelScope.launch {
        repository.clearAllChecks()
    }

    fun setCheckList(list: List<User>?) {
        _checkList.value = list
    }

}