package com.sksulai.checksite.db

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.sksulai.checksite.db.converter.UriConverter
import com.sksulai.checksite.workers.CheckSiteWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class WorkerRepo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: WorkerDao
) {
    suspend fun create(
        name: String,
        description: String,
        frequency: Duration,
        url: Uri,
        autoStart: Boolean = true
    ) {
        val model = WorkerModel(
            id           = 0,
            name         = name,
            description  = description,
            frequency    = frequency,
            url          = url,
            lastChecksum = "",
            created      = OffsetDateTime.now(),
            lastRan      = null,
            count        = 0,
            running      = false,
        )

        // TODO: Perform initial check of the site

        val id = dao.insert(model)
        if(autoStart) start(model.copy(id = id))
    }

    fun getAll()          = dao.getAll()
    fun get(id: Long)     = dao.getById(id)
    fun get(name: String) = dao.getByName(name)
    fun get(url: Uri)     = dao.getByUrl(UriConverter.from(url)!!)

    /**
     * Starts a worker if one does not already exist
     */
    suspend fun start(work: WorkerModel) {
        // TODO: Restart workers if we reboot or something else stops us
        val constraint = Constraints.Builder()
            // TODO: Check user preference i.e. Check whether or not to use only unmetered
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val job = PeriodicWorkRequestBuilder<CheckSiteWorker>(work.frequency)
            .setConstraints(constraint)
            .setInputData(Data.Builder()
                .putLong(CheckSiteWorker.WorkID, work.id)
                .build()
            ).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(work.name, ExistingPeriodicWorkPolicy.KEEP, job)

        update(work.copy(running = true))
    }

    /**
     * Given a worker, sends a request to stop any further checking of the website
     */
    suspend fun stop(work: WorkerModel) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(work.name)
            .await()
        update(work.copy(running = false))
    }

    /**
     * Given a worker it removes the worker from the list of workers
     */
    suspend fun delete(work: WorkerModel) {
        // Ensure it has been stopped before removing it from the database
        stop(work)
        dao.delete(work)
    }

    suspend fun update(work: WorkerModel) = dao.update(work)

    suspend fun justRan(work: WorkerModel) = update(work.copy(
        lastRan = OffsetDateTime.now(),
        count   = work.count + 1
    ))

}
