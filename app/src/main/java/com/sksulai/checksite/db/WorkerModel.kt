package com.sksulai.checksite.db

import android.net.Uri
import java.time.Duration
import java.time.OffsetDateTime

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

import com.sksulai.checksite.db.converter.DurationConverter
import com.sksulai.checksite.db.converter.OffsetDateTimeConverter
import com.sksulai.checksite.db.converter.UriConverter

@TypeConverters(
    OffsetDateTimeConverter::class,
    DurationConverter::class,
    UriConverter::class
)
@Entity data class WorkerModel(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,             // Name of the task
    val description: String,      // Description of the task
    val frequency: Duration,      // Information about the job frequency
    val url: Uri,                 // The website to check

    val running: Boolean,         // Whether this worker is running
    val lastChecksum: String,     // Last calculated checksum

    // Metadata & Stats
    val created: OffsetDateTime,  // When this task was created
    val lastRan: OffsetDateTime?, // When this task was last run
    val count: Int                // Number of times this task has been run
)
