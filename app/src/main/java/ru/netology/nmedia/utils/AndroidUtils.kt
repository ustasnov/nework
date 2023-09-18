package ru.netology.nmedia.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

object AndroidUtils {
    fun hideKeyboard(view: View) {
        view.postDelayed({
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }, 200)
    }

    fun showKeyboard(context: Context, view: View) {
        view.requestFocus()
        view.postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, 0)
        }, 200)
    }

    fun formatDate(date: String): String {
        val nativeDate = LocalDateTime.parse(date.subSequence(0, 19))
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
        return nativeDate.format(formatter)
    }
}
