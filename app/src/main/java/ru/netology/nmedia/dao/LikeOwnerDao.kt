package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.LikeOwnerEntity

@Dao
interface LikeOwnerDao {
    @Query("SELECT * FROM LikeOwnerEntity ORDER BY name")
    fun getAll(): Flow<List<LikeOwnerEntity>>

    @Query("SELECT * FROM LikeOwnerEntity WHERE parentId = :id ORDER BY name")
    fun getLikeOwners(id: Long): Flow<List<LikeOwnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(likeOwnerEntity: LikeOwnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(likeOwnerEntities: List<LikeOwnerEntity>)

    @Query("DELETE FROM likeOwnerEntity WHERE id = :id AND parentId = :parentId ")
    suspend fun removeById(id: Long, parentId: Long)

    @Query("DELETE FROM likeOwnerEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM likeOwnerEntity")
    suspend fun countLikeOwners(): Int
}