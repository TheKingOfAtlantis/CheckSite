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

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    }
}
