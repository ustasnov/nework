package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity
import ru.netology.nmedia.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity ORDER BY name")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) == 0 FROM UserEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<UserEntity>)

    @Query("DELETE FROM UserEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM UserEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM UserEntity")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM MentionEntity WHERE postId = :id ORDER BY name")
    fun getPostMentions(id: Long): Flow<List<MentionEntity>>

    @Query("SELECT * FROM LikeOwnerEntity WHERE postId = :id ORDER BY name")
    fun getLikeOwners(id: Long): Flow<List<LikeOwnerEntity>>

}
