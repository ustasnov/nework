package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Job

interface JobRepository {
    var data: Flow<List<Job>>
    suspend fun getAllMyJobs()
    suspend fun saveMyJob(job: Job)
    suspend fun removeByIdMyJob(jobId: Long)
    suspend fun getAllUserJobs(userId: Long)
}