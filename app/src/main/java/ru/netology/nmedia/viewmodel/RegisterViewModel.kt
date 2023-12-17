package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.AuthApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.MediaModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val appAuth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData<AuthModel>()
    val dataState: LiveData<AuthModel>
        get() = _dataState

    private val _photo = MutableLiveData<MediaModel?>()
    val photo: LiveData<MediaModel?>
        get() = _photo

    private fun registerWithAvatar(login: String, pass: String, name: String) =
        viewModelScope.launch {
            _dataState.value = AuthModel(loading = true)
            try {
                val response = AuthApi.service.registerUser(
                    login.toRequestBody("text/plain".toMediaType()),
                    pass.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    MultipartBody.Part.createFormData(
                        "file", "file",
                        photo.value?.file?.asRequestBody()!!
                    )
                )
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val token: Token = requireNotNull(response.body())
                appAuth.setToken(token)
                _dataState.value = AuthModel(success = true)
            } catch (e: Exception) {
                e.printStackTrace()
                _dataState.value = AuthModel(error = true)
            }
        }

    private fun registerWithoutAvatar(login: String, pass: String, name: String) =
        viewModelScope.launch {
            _dataState.value = AuthModel(loading = true)
            try {
                val response = AuthApi.service.registerUserWithoutAvatar(login, pass, name)

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val token: Token = requireNotNull(response.body())
                appAuth.setToken(token)
                _dataState.value = AuthModel(success = true)
            } catch (e: Exception) {
                e.printStackTrace()
                _dataState.value = AuthModel(error = true)
            }
        }

    fun register(login: String, pass: String, name: String) {
        if (photo.value === null) {
            registerWithoutAvatar(login, pass, name)
        } else {
            registerWithAvatar(login, pass, name)
        }
    }

    fun clean() {
        _dataState.value = AuthModel(loading = false, error = false, success = false)
    }

    fun setPhoto(mediaModel: MediaModel) {
        _photo.value = mediaModel
    }
}
