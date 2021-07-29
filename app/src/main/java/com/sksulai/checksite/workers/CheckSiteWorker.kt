package com.sksulai.checksite.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sksulai.checksite.R
import com.sksulai.checksite.db.WorkerRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest

@HiltWorker class CheckSiteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    val repo: WorkerRepo
) : CoroutineWorker(context, params) {

    companion object {
        const val WorkID = "WORK_ID"
    }

    private fun fromUrl(url: Uri): ByteArray {
        return try {
            val inputStream = URL(url.toString()).openStream()
            inputStream.readBytes().also {
                inputStream.close()
            }
        } catch(e: IOException) {
            e.printStackTrace()
            byteArrayOf()
        }
    }

    private fun calculateChecksum(data: ByteArray) =
        MessageDigest
            .getInstance("MD5")
            .digest(data)
            .let { BigInteger(1, it) }
            .toString()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Get the ID and check that it is valid
        val workId = inputData.getLong(WorkID, 0)
        if(workId == 0L) return@withContext Result.failure()

        // Retrieve the Url for that Job
        val data = repo.get(workId).first().let { work ->
            if(work == null) return@withContext Result.failure()

            val data = fromUrl(work.url)
            val checksum = calculateChecksum(data)

            launch { repo.justRan(work) }

            if(work.lastChecksum != checksum) {
                val webpageIntent = Intent(Intent.ACTION_VIEW).also { it.data = work.url }

                val notification = NotificationCompat.Builder(applicationContext, work.name)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("One of your websites have changed")
                    .setContentText("The site labelled ${work.name} has changed")
                    .setContentIntent(PendingIntent.getActivity(applicationContext, 0, webpageIntent, 0))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                val name = "Site Changed"
                val descriptionText = "Notifies when a watched site has been changed"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(work.name, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager = getSystemService(applicationContext, NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)

                NotificationManagerCompat
                    .from(applicationContext)
                    .notify(0, notification)

                launch { repo.update(work.copy(lastChecksum = checksum)) }
            }
        }
        return@withContext Result.success()
    }

}
