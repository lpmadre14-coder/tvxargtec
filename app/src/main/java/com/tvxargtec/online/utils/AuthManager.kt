package com.tvxargtec.online.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tvxargtec.online.api.AuthService
import com.tvxargtec.online.models.LoginRequest
import com.tvxargtec.online.models.LoginResponse
import com.tvxargtec.online.models.UserProfile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthManager private constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val authService = ApiClient.getInstance().createService(AuthService::class.java)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getUserName(): String? = prefs.getString(KEY_NAME, null)
    fun getPlanType(): String = prefs.getString(KEY_PLAN, "Free") ?: "Free"
    fun getPlanExpiry(): String = prefs.getString(KEY_PLAN_EXPIRY, "") ?: ""
    fun isLoggedIn(): Boolean = getToken() != null

    fun login(email: String, password: String, callback: AuthCallback) {
        authService.login(LoginRequest(email, password)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.isSuccess && body.token != null) {
                        saveSession(body.token, body.user, email)
                        callback.onSuccess(body.user)
                    } else {
                        callback.onError(body.message ?: "Error de autenticación")
                    }
                } else {
                    callback.onError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                callback.onError("Error de conexión: ${t.message}")
            }
        })
    }

    fun register(email: String, password: String, callback: AuthCallback) {
        authService.register(LoginRequest(email, password)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    callback.onSuccess(null)
                } else if (response.code() == 409) {
                    callback.onError("El email ya está registrado")
                } else {
                    callback.onError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                callback.onError("Error de conexión: ${t.message}")
            }
        })
    }

    fun fetchProfile(callback: ProfileCallback) {
        val token = getToken() ?: run {
            callback.onError("No hay sesión activa")
            return
        }
        authService.getUserProfile("Bearer $token").enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    updateProfile(profile)
                    callback.onSuccess(profile)
                } else if (response.code() == 401) {
                    clearSession()
                    callback.onError("Sesión expirada. Inicia sesión nuevamente.")
                } else {
                    callback.onError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                callback.onError("Error de conexión: ${t.message}")
            }
        })
    }

    fun logout(callback: LogoutCallback) {
        val token = getToken() ?: run {
            clearSession()
            callback.onDone()
            return
        }
        authService.logout("Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                clearSession()
                callback.onDone()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                clearSession()
                callback.onDone()
            }
        })
    }

    private fun saveSession(token: String, user: UserProfile?, email: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, user?.name ?: "Usuario")
            .putString(KEY_PLAN, user?.planType ?: "Free")
            .putString(KEY_PLAN_EXPIRY, user?.planExpiry ?: "")
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    private fun updateProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name ?: "Usuario")
            .putString(KEY_EMAIL, profile.email ?: "")
            .putString(KEY_PLAN, profile.planType ?: "Free")
            .putString(KEY_PLAN_EXPIRY, profile.planExpiry ?: "")
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    interface AuthCallback {
        fun onSuccess(user: UserProfile?)
        fun onError(error: String)
    }

    interface ProfileCallback {
        fun onSuccess(profile: UserProfile)
        fun onError(error: String)
    }

    interface LogoutCallback {
        fun onDone()
    }

    companion object {
        private const val PREFS_NAME = "tvxargtec_auth_secure"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
        private const val KEY_PLAN = "plan_status"
        private const val KEY_PLAN_EXPIRY = "plan_expiry"
        private const val KEY_LOGGED_IN = "is_logged_in"

        @Volatile private var instance: AuthManager? = null

        @JvmStatic fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
