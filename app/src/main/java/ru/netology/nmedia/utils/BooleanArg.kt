package ru.netology.nmedia.utils

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object BooleanArg : ReadWriteProperty<Bundle, Boolean?> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Boolean? {
        return thisRef.getBoolean(property.name)
    }

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Boolean?) {
        if (value != null) {
            thisRef.putBoolean(property.name, value)
        }
    }
}
