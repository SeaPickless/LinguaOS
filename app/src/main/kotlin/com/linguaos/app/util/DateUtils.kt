package com.linguaos.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun today(): String = fmt.format(Date())

    fun isToday(dateStr: String): Boolean = dateStr == today()

    fun isYesterday(dateStr: String): Boolean {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateStr == fmt.format(cal.time)
    }

    fun daysBetween(from: String, to: String): Int {
        return try {
            val d1 = fmt.parse(from) ?: return 0
            val d2 = fmt.parse(to) ?: return 0
            val diff = d2.time - d1.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) { 0 }
    }
}
