package com.kanishk.goldscanner.utils

import android.content.Context
import android.content.SharedPreferences

class LocalStorage(context: Context) {
    
    companion object {
        private const val PREFERENCES_NAME = "gold_scanner_prefs"
    }
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    
    fun storeString(key: StorageKey, value: String) {
        preferences.edit().putString(key.name, value).apply()
    }
    
    fun getString(key: StorageKey): String? {
        return preferences.getString(key.name, null)
    }
    
    fun storeBoolean(key: StorageKey, value: Boolean) {
        preferences.edit().putBoolean(key.name, value).apply()
    }
    
    fun getBoolean(key: StorageKey): Boolean? {
        return if (preferences.contains(key.name)) {
            preferences.getBoolean(key.name, false)
        } else null
    }
    
    fun storeFloat(key: StorageKey, value: Float) {
        preferences.edit().putFloat(key.name, value).apply()
    }
    
    fun getFloat(key: StorageKey): Float? {
        return if (preferences.contains(key.name)) {
            preferences.getFloat(key.name, 0f)
        } else null
    }
    
    fun storeLong(key: StorageKey, value: Long) {
        preferences.edit().putLong(key.name, value).apply()
    }
    
    fun getLong(key: StorageKey): Long? {
        return if (preferences.contains(key.name)) {
            preferences.getLong(key.name, 0L)
        } else null
    }
    
    fun remove(key: StorageKey) {
        preferences.edit().remove(key.name).apply()
    }
    
    fun clear() {
        preferences.edit().clear().apply()
    }
    
    enum class StorageKey {
        RATE_PER_TOLA,
        LAST_UPDATED_TIMESTAMP,
        CURRENT_RATE,
        IS_LOGGED_IN,
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        USER_INFO,
        API_BASE_URL,
        SPLASH_DURATION
    }
}