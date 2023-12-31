package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.Converters
import ru.netology.nmedia.dao.EventDao
import ru.netology.nmedia.dao.EventLikeOwnerDao
import ru.netology.nmedia.dao.EventRemoteKeyDao
import ru.netology.nmedia.dao.JobDao
import ru.netology.nmedia.dao.LikeOwnerDao
import ru.netology.nmedia.dao.MentionDao
import ru.netology.nmedia.dao.ParticipantDao
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dao.SpeakerDao
import ru.netology.nmedia.dao.UserDao
import ru.netology.nmedia.dao.WallDao
import ru.netology.nmedia.entity.EventEntity
import ru.netology.nmedia.entity.EventLikeOwnerEntity
import ru.netology.nmedia.entity.EventRemoteKeyEntity
import ru.netology.nmedia.entity.JobEntity
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity
import ru.netology.nmedia.entity.ParticipantEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.SpeakerEntity
import ru.netology.nmedia.entity.UserEntity
import ru.netology.nmedia.entity.WallEntity

@Database(
    entities = [PostEntity::class,
        PostRemoteKeyEntity::class,
        UserEntity::class,
        LikeOwnerEntity::class,
        EventLikeOwnerEntity::class,
        MentionEntity::class,
        ParticipantEntity::class,
        SpeakerEntity::class,
        EventRemoteKeyEntity::class,
        EventEntity::class,
        JobEntity::class,
        WallEntity::class], version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun userDao(): UserDao
    abstract fun mentionDao(): MentionDao
    abstract fun likeOwnersDao(): LikeOwnerDao
    abstract fun eventLikeOwnersDao(): EventLikeOwnerDao
    abstract fun participantDao(): ParticipantDao
    abstract fun speakerDao(): SpeakerDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun jobDao(): JobDao
    abstract fun wallDao(): WallDao
}
