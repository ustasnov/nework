package ru.netology.nmedia.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao()

    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()

    @Provides
    fun provideUserDao(db: AppDb): UserDao = db.userDao()

    @Provides
    fun provideMentionDao(db: AppDb): MentionDao = db.mentionDao()

    @Provides
    fun provideLikeOwnersDao(db: AppDb): LikeOwnerDao = db.likeOwnersDao()

    @Provides
    fun provideParticipantDao(db: AppDb): ParticipantDao = db.participantDao()

    @Provides
    fun provideSpeakerDao(db: AppDb): SpeakerDao = db.speakerDao()

    @Provides
    fun provideEventRemoteKeyDao(db: AppDb): EventRemoteKeyDao = db.eventRemoteKeyDao()

    @Provides
    fun provideEventDao(db: AppDb): EventDao = db.eventDao()

    @Provides
    fun provideJobDao(db: AppDb): JobDao = db.jobDao()

    @Provides
    fun provideWallDao(db: AppDb): WallDao = db.wallDao()

    @Provides
    fun provideWallRemoteKeyDao(db: AppDb): WallRemoteKeyDao = db.wallRemoteKeyDao()

}
