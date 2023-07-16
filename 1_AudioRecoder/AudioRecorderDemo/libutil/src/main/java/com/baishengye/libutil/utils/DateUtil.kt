package com.baishengye.libutil.utils

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import java.text.DateFormat
import java.util.*

object DateUtil {
    @SuppressLint("SimpleDateFormat")
    val ALL_FORMAT = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS")

    fun calenderToFormatString(calendar: Calendar, format: SimpleDateFormat = ALL_FORMAT):String{
        return format.format(calendar.time)
    }

    @SuppressLint("SimpleDateFormat")
    fun formatTime(ms: Int): String {
        val dateFormat: SimpleDateFormat = SimpleDateFormat("m:ss")
        return dateFormat.format(Date(ms.toLong()))
    }
}