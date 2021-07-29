package com.sksulai.checksite.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import java.net.URL

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
			launch { repo.update(work.copy(lastChecksum = checksum)) }
        }
        return@withContext Result.success()
    }

}
