package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.model.UserItemModel
import ru.netology.nmedia.repository.SpeakersRepository
import javax.inject.Inject

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SpeakersViewModel @Inject constructor(
    private val repository: SpeakersRepository,
) : ViewModel() {

    var data: LiveData<UserItemModel> =
        repository.data.map(::UserItemModel).asLiveData(Dispatchers.Default)

    fun setData(id: Long) {
        data = repository.getSpeakers(id!!).map(::UserItemModel).asLiveData(
            Dispatchers.Default)
    }

    fun getSpeakers(id: Long): LiveData<UserItemModel> {
        return repository.getSpeakers(id).map(::UserItemModel).asLiveData(
            Dispatchers.Default)
    }

    fun viewById(id: Long) {
        //toggleNewPost(false)
    }
}