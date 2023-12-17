package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Coordinates

data class CoordinatesEmbeddable(
    var lat_param: String,
    var long_param: String,
) {
    fun toDto() = Coordinates(lat_param, long_param)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.lat, it.long)
        }
    }

}
