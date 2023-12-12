package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.EventCash
import ru.netology.nmedia.dto.EventType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.EventRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

val emptyEvent = Event(
    id = 0L,
    authorId = 0L,
    author = "",
    authorAvatar = null,
    authorJob = null,
    content = "",
    datetime = "",
    published = "",
    coords = null,
    type = EventType.OFFLINE,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    speakerIds = emptyList(),
    participantsIds = emptyList(),
    participatedByMe = false,
    attachment = null,
    link = null,
    ownedByMe = false,
    users = mutableMapOf(),
)



@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    appAuth: AppAuth,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Event>> = appAuth.data
        .flatMapLatest { token ->
            repository.data.map { events ->
                events.map { event ->
                    event.copy(ownedByMe = event.authorId == token?.id)
                }
            }
        }.flowOn(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _media = MutableLiveData<MediaModel?>()
    val media: LiveData<MediaModel?>
        get() = _media

    private val _edited = MutableLiveData<Event>()
    val edited: LiveData<Event>
        get() = _edited

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _currentEventId = MutableLiveData<Long>()
    val currentEventId: LiveData<Long>
        get() = _currentEventId

    private val _currentEvent =
        MutableLiveData(emptyEvent.copy())

    val currentEvent: LiveData<Event>
        get() = _currentEvent

    var newEventCash: EventCash? = null

    private val _currentMediaType =
        MutableLiveData(AttachmentType.IMAGE)

    val currentMediaType: LiveData<AttachmentType>
        get() = _currentMediaType

    init {
        //loadEvents()
    }

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LOADING)
        }
    }

    fun save() = viewModelScope.launch {
        try {
            _edited.value?.let {
                val media = _media.value
                if (media != null) {
                    repository.saveWithAttachment(it.copy(ownedByMe = true), media)
                } else {
                    repository.save(it.copy(ownedByMe = true))
                }
            }
            _edited.value = emptyEvent
            _eventCreated.postValue(Unit)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.SAVE)
        }
    }

    fun edit(event: Event) {
        //toggleNewEvent(false)
        _edited.value = event
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (_edited.value?.content == text) {
            return
        }
        _edited.value = edited.value?.copy(content = text)
    }

    fun likeById(event: Event) = viewModelScope.launch {
        try {
            if (!event.likedByMe) {
                repository.likeById(event.id)
            } else {
                repository.unlikeById(event.id)
            }
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LIKE)
        }
    }

    fun participantById(event: Event) = viewModelScope.launch {
        try {
            if (!event.participatedByMe) {
                repository.participantById(event.id)
            } else {
                repository.unParticipantById(event.id)
            }
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.LIKE)
        }
    }

    fun viewById(event: Event) {
        //toggleNewEvent(false)
        _currentEvent.setValue(event)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = ErrorType.REMOVE)
        }
    }

    fun saveNewEventContent(text: String) {
        repository.saveNewEventContent(text)
    }

    fun getNewEventCont(): LiveData<String> {
        return repository.getNewEventContent()
    }

    fun setMediaType(mediaType: AttachmentType) {
        _currentMediaType.value = mediaType
    }

    fun clearMedia() {
        _media.value = null
    }

    fun setMedia(mediaModel: MediaModel) {
        _media.value = mediaModel
    }
}
