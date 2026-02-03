package com.kanishk.goldscanner.utils

import com.kanishk.goldscanner.data.model.response.NepaliDate
import java.time.LocalDate
import java.time.ZoneId

object NepaliDateUtils {
    
    /**
     * Get current Nepali date
     * This is a simplified conversion. For production use, you might want to use
     * a proper Nepali calendar library for accurate conversion.
     */
    fun getCurrentNepaliDate(): NepaliDate {
        // Get current date in Nepal timezone
        val nepaliZone = ZoneId.of("Asia/Kathmandu")
        val currentDate = LocalDate.now(nepaliZone)
        
        // Simple approximation: Nepali year is approximately 56-57 years ahead
        // This is a basic conversion and should be replaced with proper library
        val nepaliYear = currentDate.year + 56
        
        // For simplicity, using similar month and day
        // In reality, Nepali calendar has different month lengths and structure
        return NepaliDate(
            year = nepaliYear,
            month = currentDate.monthValue,
            dayOfMonth = currentDate.dayOfMonth
        )
    }
    
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