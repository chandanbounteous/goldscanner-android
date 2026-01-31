package com.kanishk.goldscanner.utils


import kotlin.math.pow


class Utils {
    companion object {

        fun roundToTwoDecimalPlaces(value: Double, decimalPlace: Int): Double {
            return kotlin.math.round(value * 10.0.pow(decimalPlace)) / 10.0.pow(decimalPlace)
        }
    }
}