package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.ApiClient

/**
 * JWT token এবং logged-in user ID SharedPreferences-এ save করে।
 * App restart-এ auto-login support করে।
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("blood_connect_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN   = "jwt_token"
        private const val KEY_USER_ID = "user_id"
    }

    fun saveSession(token: String, userId: Int) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .apply()
        // ApiClient interceptor-এ token set করো
        ApiClient.authToken = token
    }

    fun loadToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        // App start-এ ApiClient-এ set করো
        if (token != null) ApiClient.authToken = token
        return token
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun clearSession() {
        prefs.edit().clear().apply()
        ApiClient.authToken = null
    }

    fun isLoggedIn(): Boolean = prefs.getString(KEY_TOKEN, null) != null
}
