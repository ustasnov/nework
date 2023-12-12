package ru.netology.nmedia.utils

import android.app.DatePickerDialog
import android.content.Context
import android.text.TextUtils.split
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun formatDate(date: String, pattern: String = "dd MMM yyyy HH:mm:ss"): String {
        val nativeDate = LocalDateTime.parse(date.subSequence(0, 19))
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return nativeDate.format(formatter)
    }

    fun formatDateForDB(date: String, time: String = "T00:00:00", pattern: String = "yyyy-MM-dd'T'HH:mm:ss.000'Z'"): String {
        val dateStr = date.split(".").reversed().joinToString("-") + time
        return LocalDateTime.parse(dateStr).format(DateTimeFormatter.ofPattern( pattern))
    }

    fun showCalendar(context: Context, cal: Calendar, view: View, hasFocus: Boolean, callback: DatePickerDialog.OnDateSetListener) {
        if (hasFocus) {
            hideKeyboard(view)
            DatePickerDialog(
                context, callback,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    fun getDatePickerDialogListener(control: View, cal: Calendar): DatePickerDialog.OnDateSetListener {
        return DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            (control as TextInputEditText).setText(sdf.format(cal.time))
        }
    }
}
