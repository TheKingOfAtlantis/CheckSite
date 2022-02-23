package com.sksulai.checksite.ui

import java.text.*
import java.util.*
import java.time.Duration

import android.os.Build

import android.icu.text.MeasureFormat
import android.icu.util.MeasureUnit

enum class TimeUnit {
    Day,
    Hour,
    Minute,
    Second
}

open class DurationFormatter(private val locale: Locale = Locale.getDefault()) {
    private fun getLocalisedNumber(value: Int) = NumberFormat.getInstance(locale).format(value)
    private fun getUnitRepresentation(timeUnit: TimeUnit) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val measureFormat = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.NARROW)
            when(timeUnit) {
                TimeUnit.Day -> measureFormat.getUnitDisplayName(MeasureUnit.DAY)
                TimeUnit.Hour -> measureFormat.getUnitDisplayName(MeasureUnit.HOUR)
                TimeUnit.Minute -> measureFormat.getUnitDisplayName(MeasureUnit.MINUTE)
                TimeUnit.Second -> measureFormat.getUnitDisplayName(MeasureUnit.SECOND)
            }
        } else when(timeUnit) {
            TimeUnit.Day -> "day"
            TimeUnit.Hour -> "hour"
            TimeUnit.Minute -> "min"
            TimeUnit.Second -> "sec"
        }


    open fun format(duration: Duration, smallestUnit: TimeUnit = TimeUnit.Minute) = mapOf(
        TimeUnit.Day    to duration.toDays().toInt(),
        TimeUnit.Hour   to (duration.toHours() % 24).toInt(),
        TimeUnit.Minute to (duration.toMinutes() % 60).toInt(),
        TimeUnit.Second to (duration.seconds % 60).toInt(),
    ).filterKeys { it <= smallestUnit }.let { components ->
        var nonZero = false
        components.filterValues {
            // Filter everything until we find non-zero value then keep everything
            if(it != 0) nonZero = true
            nonZero
        }.map { (k, v) -> "${getLocalisedNumber(v)}${getUnitRepresentation(k)}" }
            .joinToString(" ")
    }

    companion object Default : DurationFormatter()
}
