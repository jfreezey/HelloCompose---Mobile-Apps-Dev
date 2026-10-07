package com.example.ch07.util

fun formatRelativeTime(now: Long, then: Long): String {
    val elapsed = (now - then).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        elapsed < minute -> "baru saja"
        elapsed < hour -> "${elapsed / minute} menit lalu"
        elapsed < day -> "${elapsed / hour} jam lalu"
        elapsed < 30 * day -> "${elapsed / day} hari lalu"
        elapsed < 365 * day -> "${elapsed / (30 * day)} bulan lalu"
        else -> "${elapsed / (365 * day)} tahun lalu"
    }
}