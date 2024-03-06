package net.comelite.kurdistanborsa.api

import net.comelite.kurdistanborsa.model.BaseSettings
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SettingsApi {

    @GET("webservices.php?")
    fun getAllSettings(@Query("function") function: String, @Query("user_id") userId: String): Call<BaseSettings>

    @GET("webservices.php?")
    fun updateNotificationStatus(@Query("function") function: String,
                                 @Query("user_id") userId: String,
                                 @Query("lang") language: String,
                                 @Query("is_notification_enabled") isNotificationEnabled: String): Call<BaseSettings>

    companion object {
        @Volatile private var instance: SettingsApi? = null

        fun create(): SettingsApi =
            instance ?: synchronized(this) {
                instance ?: RetrofitClient.getInstance().create(SettingsApi::class.java).also { instance = it }
            }
    }
}