package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.SpeakerEntity

@Dao
interface SpeakerDao {
    @Query("SELECT * FROM SpeakerEntity ORDER BY name")
    fun getAll(): Flow<List<SpeakerEntity>>

    @Query("SELECT * FROM SpeakerEntity WHERE parentId = :id ORDER BY name")
    fun getSpeakers(id: Long): Flow<List<SpeakerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(speakerEntity: SpeakerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(speakerEntities: List<SpeakerEntity>)

    @Query("DELETE FROM speakerEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM SpeakerEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM SpeakerEntity")
    suspend fun countSpeakers(): Int
}
