package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.model.UserItemModel
import ru.netology.nmedia.model.UserModel
import ru.netology.nmedia.repository.MentionsRepository
import javax.inject.Inject


@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MentionViewModel @Inject constructor(
    private val repository: MentionsRepository,
) : ViewModel() {

    var data: LiveData<UserItemModel> =
        repository.data.map(::UserItemModel).asLiveData(Dispatchers.Default)

    //private val _dataState = MutableLiveData<FeedModelState>()
    //val dataState: LiveData<FeedModelState>
    //    get() = _dataState

    //init {
    //    repository.getPostMentions(postId)
    //}

    fun setData(id: Long) {
        data = repository.getPostMentions(id!!).map(::UserItemModel).asLiveData(Dispatchers.Default)
    }

    fun getMentions(id: Long): LiveData<UserItemModel> {
        return repository.getPostMentions(id).map(::UserItemModel).asLiveData(Dispatchers.Default)
    }

    fun viewById(id: Long) {
        //toggleNewPost(false)
    }
}