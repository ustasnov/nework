package ru.netology.nmedia.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val TOKEN_KEY = "TOKEN_KEY"
    private val ID_KEY = "ID_KEY"

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _data = MutableStateFlow<Token?>(null)
    val data = _data.asStateFlow()

    /*
    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging
     */

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        if (token == null || id == 0L) {
            _data.value = null
            prefs.edit {
                clear()
                apply()
            }
        } else {
            _data.value = Token(id, token)
        }
        //sendPushToken()
    }

    @Synchronized
    fun setToken(token: Token) {
        _data.value = token
        prefs.edit {
            putString(TOKEN_KEY, token.token)
            putLong(ID_KEY, token.id)
        }
        //sendPushToken()
    }

    @Synchronized
    fun clearAuth() {
        _data.value = null
        prefs.edit { clear() }
        //sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): ApiService
    }

    /*
    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val request = PushToken(token ?: firebaseMessaging.token.await())
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().sendPushToken(request)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
     */
}
