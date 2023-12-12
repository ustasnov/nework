package ru.netology.nmedia.dto

data class EventCash(
    val eventType: String = "",
    val content: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val coords: Coordinates? = null,
    val link: String = "",
)
