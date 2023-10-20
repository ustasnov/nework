package ru.netology.nmedia.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.repository.LikeOwnersRepository
import ru.netology.nmedia.repository.LikeOwnersRepositoryImpl
import ru.netology.nmedia.repository.MentionsRepository
import ru.netology.nmedia.repository.MentionsRepositotyImpl
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.repository.UserRepository
import ru.netology.nmedia.repository.UserRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Singleton
    @Binds
    fun bindsPostRepository(impl: PostRepositoryImpl): PostRepository

    @Singleton
    @Binds
    fun bindsUserRepository(impl: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    fun bindsMentionRepository(impl: MentionsRepositotyImpl): MentionsRepository

    @Singleton
    @Binds
    fun bindsLikeOwnersRepository(impl: LikeOwnersRepositoryImpl): LikeOwnersRepository
}
