package com.kanishk.goldscanner.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalStorage(context: Context) {
    
    companion object {
        private const val PREFERENCES_NAME = "gold_scanner_prefs"
    }
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    
    // Generic method to save primitive types
    internal fun <T> save(key: StorageKey, value: T) {
        val editor = preferences.edit()
        when (value) {
            is String -> editor.putString(key.name, value)
            is Int -> editor.putInt(key.name, value)
            is Boolean -> editor.putBoolean(key.name, value)
            is Float -> editor.putFloat(key.name, value)
            is Long -> editor.putLong(key.name, value)
            else -> throw IllegalArgumentException("Unsupported type: ${value?.javaClass?.simpleName}")
        }
        editor.apply()
    }

    // Generic method to get primitive types
    @Suppress("UNCHECKED_CAST")
    internal fun <T> get(key: StorageKey, clazz: Class<T>): T? {
        return when (clazz) {
            String::class.java -> preferences.getString(key.name, null) as T?
            Int::class.java -> if (preferences.contains(key.name)) preferences.getInt(key.name, 0) as T else null
            Integer::class.java -> if (preferences.contains(key.name)) preferences.getInt(key.name, 0) as T else null
            Boolean::class.java -> if (preferences.contains(key.name)) preferences.getBoolean(key.name, false) as T else null
            Float::class.java -> if (preferences.contains(key.name)) preferences.getFloat(key.name, 0f) as T else null
            Long::class.java -> if (preferences.contains(key.name)) preferences.getLong(key.name, 0L) as T else null
            else -> null
        }
    }

    // Inline convenience methods for getting values
    internal inline fun <reified T> getValue(key: StorageKey): T? = get(key, T::class.java)

    // Generic method to save objects (serialize to JSON)
    internal fun <T> saveObject(key: StorageKey, value: T) {
        val gson = Gson()
        val json = gson.toJson(value)
        preferences.edit().putString(key.name, json).apply()
    }

    // Generic method to get objects (deserialize from JSON)
    internal inline fun <reified T> getObject(key: StorageKey): T? {
        val json = preferences.getString(key.name, null) ?: return null
        val gson = Gson()
        return try {
            gson.fromJson(json, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            null
        }
    }
    
    
    
    fun remove(key: StorageKey) {
        preferences.edit().remove(key.name).apply()
    }

    // Public method to remove a key by string (for custom keys)
    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
    
    // Public method to get a string
    fun getString(key: String): String? {
        return preferences.getString(key, null)
    }
    
    // Public method to put a string
    fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
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
        SPLASH_DURATION,
        CURRENT_GOLD_RATE_INFO,
        GOLD_RATE_UPDATE_HOUR,
        DEFAULT_ARTICLES_OFFSET,
        DEFAULT_ARTICLES_LIMIT,
        ACTIVE_BASKET
    }
}