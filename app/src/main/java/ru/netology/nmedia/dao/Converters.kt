package ru.netology.nmedia.dao

import androidx.room.TypeConverter
import ru.netology.nmedia.dto.AttachmentType

class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name
}
