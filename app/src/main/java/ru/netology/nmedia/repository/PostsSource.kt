package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.User

data class PostsSource(
    //val author: User?,
    val authorId: Long?,
    val sourceType: SourceType?
)

enum class SourceType {
    POSTS,
    WALL,
    MYWALL
}
