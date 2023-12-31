package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Relation
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.dto.UserPreview

data class WallWithLists(
    @Embedded val post: WallEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val likeOwners: List<LikeOwnerEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val mentions: List<MentionEntity>
) {
    fun toDto() = post.toDto().copy(
        likeOwnerIds = likeOwners.map { it.id },
        mentionIds = mentions.map { it.id },
        users = getWallUsers()
    )

    private fun getWallUsers(): Map<String, UserPreview> {
        val result: MutableMap<String, UserPreview> = mutableMapOf()
        likeOwners.forEach {
            val key = it.id.toString()
            result[key] = UserPreview(it.name, it.avatar)
        }
        mentions.forEach {
            val key = it.id.toString()
            if (!result.containsKey(key)) {
                result[key] = UserPreview(it.name, it.avatar)
            }
        }
        return result.toMap()
    }

    companion object {
        private fun fillLikeOwnersList(post: Post): List<LikeOwnerEntity> {
            val result: MutableList<LikeOwnerEntity> = mutableListOf()
            post.likeOwnerIds.forEach {
                val key = it.toString()
                if (post.users.containsKey(key)) {
                    val userPreview = post.users[key]
                    val userItem = UserItem(it, post.id, userPreview!!.name, userPreview.avatar)
                    result.add(LikeOwnerEntity.fromDto(userItem))
                }
            }
            return result.toList()
        }

        private fun fillMentionsList(post: Post): List<MentionEntity> {
            val result: MutableList<MentionEntity> = mutableListOf()
            post.mentionIds.forEach {
                val key = it.toString()
                if (post.users.containsKey(key)) {
                    val userPreview = post.users[key]
                    val userItem = UserItem(it, post.id, userPreview!!.name, userPreview.avatar)
                    result.add(MentionEntity.fromDto(userItem))
                }
            }
            return result.toList()
        }

        fun fromDto(dto: Post) = dto.let {
            WallWithLists(
                post = WallEntity.fromDto(it),
                likeOwners = fillLikeOwnersList(it),
                mentions = fillMentionsList(it)
            )
        }
    }
}
