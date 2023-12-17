package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.User

interface ApiService {

    // posts

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Int, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Int, @Query("count") count: Int): Response<List<Post>>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    // media

    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<Media>

    // users

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): Response<User>

    // events

    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getBeforeEvents(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getAfterEvents(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun unlikeEventById(@Path("id") id: Long): Response<Event>

    @POST("events/{id}/participants")
    suspend fun participantById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun unParticipantById(@Path("id") id: Long): Response<Event>

    // wall

    @GET("{author_id}/wall/")
    suspend fun getAllWallPosts(@Path("author_id") authorId: Long): Response<List<Post>>

    // my wall

    @GET("my/wall")
    suspend fun getAllMyWallPosts(): Response<List<Post>>

    // jobs

    @GET("my/jobs")
    suspend fun getAllMyJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveMyJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{job_id}")
    suspend fun removeByIdMyJob(@Path("job_id") jobId: Long): Response<Unit>

    @GET("{user_id}/jobs")
    suspend fun getAllUserJobs(@Path("user_id") userId: Long): Response<List<Job>>

}
