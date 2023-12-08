package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostWithLists
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity

@Dao
interface PostDao {
    @Transaction
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostWithLists>>

    @Transaction
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostWithLists>
    //fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikeOwners(likeOwnerEntities: List<LikeOwnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMentions(mentionEntities: List<MentionEntity>)

    @Transaction
    suspend fun insertPostWithLists(post: PostWithLists) {
        insert(post.post)
        clearPostLikeOwners(post.post.id)
        clearPostMentions(post.post.id)
        insertLikeOwners(post.likeOwners)
        insertMentions(post.mentions)
    }

    @Transaction
    suspend fun insertPostsWithLists(posts: List<PostWithLists>) {
        posts.forEach {
            insertPostWithLists(it)
        }
    }

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
        UPDATE PostEntity SET likedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
        UPDATE PostEntity SET likedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun unlikeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun clear()

    @Query("DELETE FROM LikeOwnerEntity")
    suspend fun clearLikeOwners()

    @Query("DELETE FROM LikeOwnerEntity WHERE parentId = :parentId")
    suspend fun clearPostLikeOwners(parentId: Long)

    @Query("DELETE FROM MentionEntity")
    suspend fun clearMentions()

    @Query("DELETE FROM MentionEntity WHERE parentId = :parentId")
    suspend fun clearPostMentions(parentId: Long)

    @Transaction
    suspend fun clearWithLists() {
        clearLikeOwners()
        clearMentions()
        clear()
    }

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun countPosts(): Int
}


