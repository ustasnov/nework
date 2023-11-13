package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.WallRemoteKeyEntity

@Dao
interface WallRemoteKeyDao {

    @Query("SELECT max(`key`) FROM WallRemoteKeyEntity")
    suspend fun max(): Int?

    @Query("SELECT min(`key`) FROM WallRemoteKeyEntity")
    suspend fun min(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: WallRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallRemoteKeyEntity: List<WallRemoteKeyEntity>)

    @Query("DELETE FROM WallRemoteKeyEntity")
    suspend fun clear()
}
