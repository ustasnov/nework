package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.EventLikeOwnerEntity
import ru.netology.nmedia.entity.LikeOwnerEntity

@Dao
interface EventLikeOwnerDao {
    @Query("SELECT * FROM EventLikeOwnerEntity ORDER BY name")
    fun getAll(): Flow<List<EventLikeOwnerEntity>>

    @Query("SELECT * FROM EventLikeOwnerEntity WHERE parentId = :id ORDER BY name")
    fun getLikeOwners(id: Long): Flow<List<EventLikeOwnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventLikeOwnerEntity: EventLikeOwnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventLikeOwnerEntities: List<EventLikeOwnerEntity>)

    @Query("DELETE FROM EventLikeOwnerEntity WHERE id = :id AND parentId = :parentId ")
    suspend fun removeById(id: Long, parentId: Long)

    @Query("DELETE FROM EventLikeOwnerEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM EventLikeOwnerEntity")
    suspend fun countEventLikeOwners(): Int
}