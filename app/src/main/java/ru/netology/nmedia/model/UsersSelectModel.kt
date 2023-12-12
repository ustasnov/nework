package ru.netology.nmedia.model

data class UsersSelectModel(
    val title: String,
    val choice: Boolean,
    val type: String = "User"
)
