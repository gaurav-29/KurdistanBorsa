package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName
import net.comelite.kurdistanborsa.model.BaseSettings

import net.comelite.kurdistanborsa.model.Notifications
import net.comelite.kurdistanborsa.model.Notifications1
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NotificationApi {
    @GET("webservices.php?")
    fun getNotification(@Query("function") func: String,
                        @Query("user_id") userId: String): Call<NetworkGetNotification>

    @GET("webservices.php?")
    fun readAllNotifications(@Query("function") func: String,
                             @Query("user_id") userId: String,
                             @Query("lang") language: String): Call<BaseSettings>

    data class NetworkGetNotification (
        @SerializedName("status") val status: String,
        @SerializedName("data") val data: List<Notifications1>,
        @SerializedName("count") val count: String?,
    )
}