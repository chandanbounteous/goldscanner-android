package com.kanishk.goldscanner.utils

import com.kanishk.goldscanner.data.model.response.NepaliDate

object NepaliDateFormatter {
    
    private val nepaliMonths = listOf(
        "Baisakh", "Jestha", "Ashar", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )
    
    fun formatNepaliDate(nepaliDate: NepaliDate): String {
        val monthName = if (nepaliDate.month in 1..12) {
            nepaliMonths[nepaliDate.month - 1]
        } else {
            "Invalid"
        }
        return "${nepaliDate.dayOfMonth} $monthName ${nepaliDate.year}"
    }
}