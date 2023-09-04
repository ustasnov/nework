package ru.netology.nmedia.utils

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object LongArg : ReadWriteProperty<Bundle, Long?> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long? {
        return thisRef.getLong(property.name)
    }

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long?) {
        if (value != null) {
            thisRef.putLong(property.name, value)
        }
    }
}
