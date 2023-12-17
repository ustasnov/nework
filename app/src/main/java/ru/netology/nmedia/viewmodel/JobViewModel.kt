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
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.JobModel
import ru.netology.nmedia.repository.JobRepository
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

val emptyJob = Job(
    id = 0L,
    name = "",
    position = "",
    start = "",
    finish = null,
    link = null
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
) : ViewModel() {

    var data: LiveData<JobModel> =
        repository.data.map(::JobModel).asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    private val _postSource = MutableLiveData(emptyPostSource.copy())
    val postSource: LiveData<PostsSource>
        get() = _postSource

    val edited = MutableLiveData(emptyJob)

    init {
        clearJobs()
    }

    fun loadMyJobs() = viewModelScope.launch {
        _dataState.value = FeedModelState(loading = true)
        try {
            repository.getAllMyJobs()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refreshMyJobs() = viewModelScope.launch {
        _dataState.value = FeedModelState(refreshing = true)
        try {
            repository.getAllMyJobs()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun saveMyJob(job: Job) = viewModelScope.launch {
        try {
            repository.saveMyJob(job)
            edited.value = emptyJob
            _jobCreated.postValue(Unit)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun removeMyJob(job: Job) = viewModelScope.launch {
        try {
            repository.removeByIdMyJob(job.id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun loadUserJobs(userId: Long) = viewModelScope.launch {
        _dataState.value = FeedModelState(loading = true)
        try {
            repository.getAllUserJobs(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refreshUserJobs(userId: Long) = viewModelScope.launch {
        _dataState.value = FeedModelState(refreshing = true)
        try {
            repository.getAllUserJobs(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun clearJobs() = viewModelScope.launch {
        repository.clearJobs()
    }

    fun setPostSource(authorId: Long, sourceType: SourceType) {
        _postSource.postValue(PostsSource(authorId, sourceType))
    }

    fun edit(job: Job) {
        edited.value = job
    }

}