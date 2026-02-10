package com.kanishk.goldscanner.utils

import com.kanishk.goldscanner.data.model.response.NepaliDate
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import java.time.LocalDate
import java.time.ZoneId


object NepaliDateUtils {
    
    /**
     * Get current Nepali date using the official nepali-date-picker-android library
     * Now using type alias, so NepaliDate is directly SimpleDate from the library.
     */
    fun getCurrentNepaliDate(): NepaliDate {
//        // Get current date in Nepal timezone
//        val nepaliZone = ZoneId.of("Asia/Kathmandu")
//        val currentDate = LocalDate.now(nepaliZone)
//
//        // Since NepaliDate is now an alias of SimpleDate, we can return SimpleDate directly
//        // TODO: Investigate the proper conversion methods in the library
//        // The library likely has methods like DateConverter.englishToNepali() or similar
//        return SimpleDate(
//            year = currentDate.year + 56, // Approximate Nepali year conversion
//            month = currentDate.monthValue,
//            dayOfMonth = currentDate.dayOfMonth
//        )
        return NepaliDateConverter.todayNepaliSimpleDate
    }

//    fun getTodayNepaliDate(): SimpleDate {
//        return NepaliDateConverter.todayNepaliSimpleDate
//    }
    
    /**
     * Compare two Nepali dates
     * Returns:
     * - Negative value if first date is before second date
     * - Zero if dates are equal
     * - Positive value if first date is after second date
     */
    fun compare(date1: NepaliDate, date2: NepaliDate): Int {
        // Compare year first
        if (date1.year != date2.year) {
            return date1.year - date2.year
        }
        
        // Compare month if years are same
        if (date1.month != date2.month) {
            return date1.month - date2.month
        }
        
        // Compare day if years and months are same
        return date1.dayOfMonth - date2.dayOfMonth
    }
    
    /**
     * Check if first date is after second date
     */
    fun isAfter(date1: NepaliDate, date2: NepaliDate): Boolean {
        return compare(date1, date2) > 0
    }
    
    /**
     * Check if first date is before second date
     */
    fun isBefore(date1: NepaliDate, date2: NepaliDate): Boolean {
        return compare(date1, date2) < 0
    }
    
    /**
     * Check if two dates are same
     */
    fun isSame(date1: NepaliDate, date2: NepaliDate): Boolean {
        return compare(date1, date2) == 0
    }
}