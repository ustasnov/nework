package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity
import ru.netology.nmedia.entity.WallEntity
import ru.netology.nmedia.entity.WallWithLists

@Dao
interface WallDao {
    @Transaction
    @Query("SELECT * FROM WallEntity ORDER BY id DESC")
    fun getAll(): Flow<List<WallWithLists>>

    @Transaction
    @Query("SELECT * FROM WallEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, WallWithLists>
    //fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM WallEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: WallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<WallEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikeOwners(likeOwnerEntities: List<LikeOwnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMentions(mentionEntities: List<MentionEntity>)

    @Transaction
    suspend fun insertPostWithLists(post: WallWithLists) {
        insert(post.post)
        insertLikeOwners(post.likeOwners)
        insertMentions(post.mentions)
    }

    @Transaction
    suspend fun insertPostsWithLists(posts: List<WallWithLists>) {
        posts.forEach {
            insertPostWithLists(it)
        }
    }

    @Query("UPDATE WallEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: WallEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
        UPDATE WallEntity SET likedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
        UPDATE WallEntity SET likedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun unlikeById(id: Long)


    @Query("DELETE FROM WallEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Transaction
    suspend fun removeByIdWithLists(id: Long) {
        removeLikeOwnersByParentId(id)
        removeMentionsByParentId(id)
        removeById(id)
    }

     @Query("DELETE FROM LikeOwnerEntity WHERE parentId = :id")
     suspend fun removeLikeOwnersByParentId(id: Long)

     @Query("DELETE FROM MentionEntity WHERE parentId = :id")
     suspend fun removeMentionsByParentId(id: Long)

    @Query("DELETE FROM WallEntity")
    suspend fun clear()

    @Query("DELETE FROM LikeOwnerEntity")
    suspend fun clearLikeOwners()

    @Query("DELETE FROM MentionEntity")
    suspend fun clearMentions()

    @Transaction
    suspend fun clearWithLists() {
        clearLikeOwners()
        clearMentions()
        clear()
    }

    @Query("SELECT COUNT(*) FROM WallEntity")
    suspend fun countPosts(): Int
}


