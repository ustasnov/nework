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
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.JobModel
import ru.netology.nmedia.repository.JobRepository
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
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
    //private val jobDao: JobDao,
    private val repository: JobRepository,
) : ViewModel() {

    var data: LiveData<JobModel> =
        repository.data.map(::JobModel).asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _currentJob = MutableLiveData(emptyJob.copy())
    val currentJob: LiveData<Job>
        get() = _currentJob

    private val _currentUserId = MutableLiveData<Long>()
    val currentUserId: LiveData<Long>
        get() = _currentUserId

    private val _postSource = MutableLiveData(emptyPostSource.copy())
    val postSource: LiveData<PostsSource>
        get() = _postSource

    val edited = MutableLiveData(emptyJob)
    var isNewJob = false

    init {
        //println("From JobViewModel.init.clearJobs()")
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
        _currentJob.postValue(job)
        try {
            repository.saveMyJob(_currentJob.value!!)
            _currentJob.postValue(emptyJob)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun removeMyJob(job: Job) = viewModelScope.launch {
        _currentJob.postValue(job)
        try {
            repository.removeByIdMyJob(_currentJob.value!!.id)
            _currentJob.postValue(emptyJob)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun loadUserJobs(userId: Long) = viewModelScope.launch {
        _dataState.value = FeedModelState(loading = true)
        //_currentUserId.postValue(userId)
        //_currentUserId.value = userId
        try {
            println("From viewModel.loadUserJobs: ${userId}")
            //repository.getAllUserJobs(_currentUserId.value!!)
            repository.getAllUserJobs(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refreshUserJobs(userId: Long) = viewModelScope.launch {
        _dataState.value = FeedModelState(refreshing = true)
        //_currentUserId.postValue(userId)
        //_currentUserId.value = userId
        try {
            println("From viewModel.refreshUserJobs: ${userId}")
            //repository.getAllUserJobs(_currentUserId.value!!)
            repository.getAllUserJobs(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun clearJobs()  = viewModelScope.launch {
        //println("From clearJobs()")
        repository.clearJobs()
        //repository.data.map(::JobModel).asLiveData(Dispatchers.Default)
    }

    fun setPostSource(authorId: Long, sourceType: SourceType) {
        _postSource.postValue(PostsSource(authorId, sourceType))
    }

    fun edit(job: Job) {
        //toggleNewPost(false)
        edited.value = job
    }

}