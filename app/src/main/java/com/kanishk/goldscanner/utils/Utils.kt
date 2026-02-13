package com.kanishk.goldscanner.utils

import kotlin.math.pow
import java.text.DecimalFormat

class Utils {
    companion object {

        fun roundToTwoDecimalPlaces(value: Double, decimalPlace: Int): Double {
            return kotlin.math.round(value * 10.0.pow(decimalPlace)) / 10.0.pow(decimalPlace)
        }
        
        /**
         * Format double value to string representation suitable for input fields
         */
        fun formatDoubleToString(value: Double): String {
            return if (value == 0.0) "" else DecimalFormat("#.##").format(value)
        }
        
        /**
         * Parse double value from string input, handling empty/invalid strings
         */
        fun parseDoubleFromString(value: String): Double {
            return value.trim().toDoubleOrNull() ?: 0.0
        }
        
        /**
         * Format double for display with proper decimal places
         */
        fun formatDouble(value: Double): String {
            return DecimalFormat("#,##0.00").format(value)
        }
    }
}