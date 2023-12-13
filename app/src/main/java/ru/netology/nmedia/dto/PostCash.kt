package ru.netology.nmedia.dto

data class PostCash(
    val content: String = "",
    val link: String = "",
    val coords: Coordinates? = null,
)
