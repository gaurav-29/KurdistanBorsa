package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface LogoutApi {
    @GET("webservices.php?")
    fun logoutUser(@Query("function") func: String,
                   @Query("user_id") userId: String,
                   @Query("is_notification_enabled") isNotificationEnabled: String = "0"): Call<NetworkGetLogoutUser>

    data class NetworkGetLogoutUser(
        @SerializedName("status") val status: String,
        @SerializedName("message") val message: String
    )
}