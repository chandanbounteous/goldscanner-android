package com.kanishk.goldscanner.utils

import kotlinx.serialization.json.Json
import android.util.Base64
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

object JWTTokenUtils {
    
    /**
     * Decode JWT token payload without verification
     * Returns payload as JsonObject or null if invalid
     */
    fun decodePayload(token: String): JsonObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            // Add padding if needed
            val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decodedBytes = Base64.decode(paddedPayload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes)
            
            Json.parseToJsonElement(decodedString) as JsonObject
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if token is expired
     * Returns true if expired or invalid
     */
    fun isTokenExpired(token: String): Boolean {
        val payload = decodePayload(token) ?: return true
        return try {
            val exp = payload["exp"]?.jsonPrimitive?.long ?: return true
            val currentTime = System.currentTimeMillis() / 1000
            currentTime >= exp
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * Get remaining time in seconds until token expires
     * Returns -1 if token is invalid or expired
     */
    fun getTimeUntilExpiry(token: String): Long {
        val payload = decodePayload(token) ?: return -1
        return try {
            val exp = payload["exp"]?.jsonPrimitive?.long ?: return -1
            val currentTime = System.currentTimeMillis() / 1000
            val remaining = exp - currentTime
            if (remaining > 0) remaining else -1
        } catch (e: Exception) {
            -1
        }
    }
    
    /**
     * Extract user ID from token
     */
    fun getUserId(token: String): String? {
        val payload = decodePayload(token) ?: return null
        return try {
            payload["userId"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}