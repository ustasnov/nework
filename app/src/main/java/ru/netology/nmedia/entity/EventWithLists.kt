package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Relation
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.dto.UserPreview

data class EventWithLists(
    @Embedded val event: EventEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val likeOwners: List<EventLikeOwnerEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val participants: List<ParticipantEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val speakers: List<SpeakerEntity>
) {
    fun toDto() = event.toDto().copy(
        likeOwnerIds = likeOwners.map { it.id },
        participantsIds = participants.map { it.id },
        speakerIds = speakers.map { it.id },
        users = getEventUsers()
    )

    private fun getEventUsers(): Map<String, UserPreview> {
        val result: MutableMap<String, UserPreview> = mutableMapOf()
        likeOwners.forEach {
            val key = it.id.toString()
            result[key] = UserPreview(it.name, it.avatar)
        }
        participants.forEach {
            val key = it.id.toString()
            if (!result.containsKey(key)) {
                result[key] = UserPreview(it.name, it.avatar)
            }
        }
        speakers.forEach {
            val key = it.id.toString()
            if (!result.containsKey(key)) {
                result[key] = UserPreview(it.name, it.avatar)
            }
        }
        return result.toMap()
    }

    companion object {
        private fun fillLikeOwnersList(event: Event): List<EventLikeOwnerEntity> {
            val result: MutableList<EventLikeOwnerEntity> = mutableListOf()
            event.likeOwnerIds.forEach {
                val key = it.toString()
                if (event.users.containsKey(key)) {
                    val userPreview = event.users[key]
                    val userItem = UserItem(it, event.id, userPreview!!.name, userPreview.avatar)
                    result.add(EventLikeOwnerEntity.fromDto(userItem))
                }
            }
            return result.toList()
        }

        private fun fillParticipantsList(event: Event): List<ParticipantEntity> {
            val result: MutableList<ParticipantEntity> = mutableListOf()
            event.participantsIds.forEach {
                val key = it.toString()
                if (event.users.containsKey(key)) {
                    val userPreview = event.users[key]
                    val userItem = UserItem(it, event.id, userPreview!!.name, userPreview.avatar)
                    result.add(ParticipantEntity.fromDto(userItem))
                }
            }
            return result.toList()
        }

        private fun fillSpeakersList(event: Event): List<SpeakerEntity> {
            val result: MutableList<SpeakerEntity> = mutableListOf()
            event.speakerIds.forEach {
                val key = it.toString()
                if (event.users.containsKey(key)) {
                    val userPreview = event.users[key]
                    val userItem = UserItem(it, event.id, userPreview!!.name, userPreview.avatar)
                    result.add(SpeakerEntity.fromDto(userItem))
                }
            }
            return result.toList()
        }

        fun fromDto(dto: Event) = dto.let {
            EventWithLists(
                event = EventEntity.fromDto(it),
                likeOwners = fillLikeOwnersList(it),
                participants = fillParticipantsList(it),
                speakers = fillSpeakersList(it),
            )
        }

    }
}

fun List<Event>.toEntityWithLists(): List<EventWithLists> = map {
    EventWithLists.fromDto(it)
}

