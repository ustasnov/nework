package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.JobDao
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.entity.JobEntity
import javax.inject.Inject

class JobRepositoryImpl@Inject constructor(
    private val jobDao: JobDao,
    private val apiService: ApiService,
): JobRepository {
    override var data: Flow<List<Job>> =
        jobDao.getAll().map { it.map(JobEntity::toDto) }

    override suspend fun getAllMyJobs() {
        val response = apiService.getAllMyJobs()
        if (!response.isSuccessful) {
            jobDao.clear()
            throw RuntimeException(response.message())
        }
        val jobs = response.body() ?: throw RuntimeException("body is null")
        jobDao.clear()
        jobDao.insert(jobs.map { JobEntity.fromDto(it) })
    }

    override suspend fun saveMyJob(job: Job) {
        val response = apiService.saveMyJob(job)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val job = response.body() ?: throw RuntimeException("body is null")
        jobDao.insert(JobEntity.fromDto(job))
    }

    override suspend fun removeByIdMyJob(jobId: Long) {
        val response = apiService.removeByIdMyJob(jobId)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        jobDao.removeById(jobId)
    }

    override suspend fun getAllUserJobs(userId: Long) {
        jobDao.clear()
        val response = apiService.getAllUserJobs(userId)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val jobs = response.body() ?: throw RuntimeException("body is null")
        jobDao.insert(jobs.map { JobEntity.fromDto(it) })
    }

    override suspend fun clearJobs() {
        jobDao.clear()
    }
}