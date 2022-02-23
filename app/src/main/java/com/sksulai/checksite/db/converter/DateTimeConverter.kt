package com.sksulai.checksite.db.converter

import androidx.room.TypeConverter

import java.time.*
import java.time.format.DateTimeFormatter

object OffsetDateTimeConverter : IConverter<OffsetDateTime?, String?> {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    @TypeConverter override fun from(value: OffsetDateTime?) = value?.let(formatter::format)
    @TypeConverter override fun to(value: String?)           = value?.let { formatter.parse(it, OffsetDateTime::from) }
}

object DurationConverter : IConverter<Duration?, String?> {
    @TypeConverter override fun from(value: Duration?) = value?.toString()
    @TypeConverter override fun to(value: String?)     = value?.let(Duration::parse)
}
