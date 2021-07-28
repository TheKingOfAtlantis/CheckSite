package com.sksulai.checksite.db

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.sksulai.checksite.db.converter.UriConverter
import com.sksulai.checksite.workers.CheckSiteWorker
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.OffsetDateTime
import javax.inject.Inject

class WorkerRepo @Inject constructor(
    private val context: Context,
    private val dao: WorkerDao
) {
    fun create(
        name: String,
        description: String,
        frequency: Duration,
        url: Uri,
        autoStart: Boolean = true
    ) {
        val model = WorkerModel(
            id          = 0,
            name        = name,
            description = description,
            frequency   = frequency,
            url         = url,
            created     = OffsetDateTime.now(),
            lastRan     = null,
            count       = 0
        )
        start(context, model)
    }

    fun getAll()          = dao.getAll()
    fun get(name: String) = dao.getByName(name)
    fun get(url: Uri)     = dao.getByUrl(UriConverter.from(url)!!)

    /**
     * Starts a worker if one does not already exist
     */
    fun start(context: Context, work: WorkerModel) {
        val constraint = Constraints.Builder()
            // TODO: Check user preference i.e. Check whether or not to use only unmetered
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val job = PeriodicWorkRequestBuilder<CheckSiteWorker>(work.frequency)
            .setConstraints(constraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(work.name, ExistingPeriodicWorkPolicy.KEEP, job)
    }

    /**
     * Given a worker, sends a request to stop any further checking of the website
     */
    suspend fun stop(context: Context, work: WorkerModel) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(work.name)
            .await()
    }

    /**
     * Given a worker it removes the worker from the list of workers
     */
    suspend fun delete(context: Context, work: WorkerModel) {
        // Ensure it has been stopped before removing it from the database
        stop(context, work)
        dao.delete(work)
    }

}
