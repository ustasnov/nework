package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
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
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

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


}
