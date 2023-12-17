package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
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

    @Query(
        """
        UPDATE UserEntity SET checked = 1
        WHERE id = :id
        """
    )
    suspend fun setChecked(id: Long)

    @Query(
        """
        UPDATE UserEntity SET checked = 0
        WHERE id = :id
        """
    )
    suspend fun setUnChecked(id: Long)

    @Query(
        """
        UPDATE UserEntity SET checked = 0
        WHERE checked = 1
        """
    )
    suspend fun clearAllChecks()
}
