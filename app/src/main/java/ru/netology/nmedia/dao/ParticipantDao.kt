package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.ParticipantEntity

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM ParticipantEntity ORDER BY name")
    fun getAll(): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM ParticipantEntity WHERE parentId = :id ORDER BY name")
    fun getParticipants(id: Long): Flow<List<ParticipantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ParticipantEntity: ParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ParticipantEntity: List<ParticipantEntity>)

    @Query("DELETE FROM ParticipantEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM ParticipantEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM ParticipantEntity")
    suspend fun countParticipants(): Int
}
