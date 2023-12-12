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
import ru.netology.nmedia.entity.EventLikeOwnerEntity
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
    suspend fun insertLikeOwners(eventLikeOwnerEntities: List<EventLikeOwnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participantEntity: List<ParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakers(speakersEntity: List<SpeakerEntity>)

    @Transaction
    suspend fun insertEventWithLists(event: EventWithLists) {
        insert(event.event)
        clearEventLikeOwners(event.event.id)
        clearEventParticipants(event.event.id)
        clearEventSpeakers(event.event.id)
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
        UPDATE EventEntity SET likedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET likedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun unlikeById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET participatedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun participantById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET participatedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun unParticipantById(id: Long)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM EventEntity")
    suspend fun clear()

    @Query("DELETE FROM EventLikeOwnerEntity WHERE parentId = :parentId")
    suspend fun clearEventLikeOwners(parentId: Long)

    @Query("DELETE FROM EventLikeOwnerEntity")
    suspend fun clearLikeOwners()

    @Query("DELETE FROM ParticipantEntity WHERE parentId = :parentId")
    suspend fun clearEventParticipants(parentId: Long)

    @Query("DELETE FROM ParticipantEntity")
    suspend fun clearParticipants()

    @Query("DELETE FROM SpeakerEntity WHERE parentId = :parentId")
    suspend fun clearEventSpeakers(parentId: Long)

    @Query("DELETE FROM SpeakerEntity")
    suspend fun clearSpeakers()

    @Transaction
    suspend fun clearWithLists() {
        clearLikeOwners()
        clearParticipants()
        clearSpeakers()
        clear()
    }

    @Query("SELECT COUNT(*) FROM EventEntity")
    suspend fun countPosts(): Int
}
