package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.MentionEntity

@Dao
interface MentionDao {
    @Query("SELECT * FROM MentionEntity ORDER BY name")
    fun getAll(): Flow<List<MentionEntity>>

    @Query("SELECT * FROM MentionEntity WHERE parentId = :id ORDER BY name")
    fun getMentions(id: Long): Flow<List<MentionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mentionEntity: MentionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mentionEntities: List<MentionEntity>)

    @Query("DELETE FROM MentionEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM MentionEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM MentionEntity")
    suspend fun countMentions(): Int
}
