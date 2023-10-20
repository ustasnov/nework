package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.Converters
import ru.netology.nmedia.dao.LikeOwnerDao
import ru.netology.nmedia.dao.MentionDao
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dao.UserDao
import ru.netology.nmedia.entity.LikeOwnerEntity
import ru.netology.nmedia.entity.MentionEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.UserEntity

@Database(entities = [PostEntity::class,
    PostRemoteKeyEntity::class,
    UserEntity::class,
    LikeOwnerEntity::class,
    MentionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract  fun userDao(): UserDao
    abstract  fun mentionDao(): MentionDao
    abstract  fun likeOwnersDao(): LikeOwnerDao
}
