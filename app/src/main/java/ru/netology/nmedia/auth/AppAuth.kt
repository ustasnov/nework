package ru.netology.nmedia.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    }

    @Synchronized
    fun setToken(token: Token) {
        _data.value = token
        prefs.edit {
            putString(TOKEN_KEY, token.token)
            putLong(ID_KEY, token.id)
        }
    }

    @Synchronized
    fun clearAuth() {
        _data.value = null
        prefs.edit { clear() }
    }

}
