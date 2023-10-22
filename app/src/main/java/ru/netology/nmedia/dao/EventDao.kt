package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.entity.EventEntity
import ru.netology.nmedia.entity.EventWithLists
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity
import ru.netology.nmedia.entity.ParticipantEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostWithLists
import ru.netology.nmedia.entity.SpeakerEntity

@Dao
interface EventDao {
    @Transaction
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAll(): Flow<List<EventWithLists>>

    @Transaction
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, EventWithLists>

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikeOwners(likeOwnerEntities: List<LikeOwnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participantEntity: List<ParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakers(speakersEntity: List<SpeakerEntity>)

    @Transaction
    suspend fun insertEventWithLists(event: EventWithLists) {
        insert(event.event)
        insertLikeOwners(event.likeOwners)
        insertParticipants(event.participants)
        insertSpeakers(event.speakers)
    }

    @Transaction
    suspend fun insertEventWithLists(events: List<EventWithLists>) {
        events.forEach {
            insertEventWithLists(it)
        }
    }

    @Query("UPDATE EventEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(event: EventEntity) =
        if (event.id == 0L) insert(event) else updateContentById(event.id, event.content)

    @Query(
        """
        UPDATE EventEntity SET likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun unlikeById(id: Long)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM EventEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM EventEntity")
    suspend fun countPosts(): Int
}
