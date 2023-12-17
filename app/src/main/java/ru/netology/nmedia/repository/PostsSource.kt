package ru.netology.nmedia.repository

data class PostsSource(
    val authorId: Long?,
    val sourceType: SourceType?
)

enum class SourceType {
    POSTS,
    WALL,
    MYWALL
}
