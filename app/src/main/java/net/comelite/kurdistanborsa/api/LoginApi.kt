package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName
import net.comelite.kurdistanborsa.model.User
import net.comelite.kurdistanborsa.model.User1
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface LoginApi {
    @GET("webservices.php?")
    fun loginUser(@Query("function") func: String,
                  @Query("username") username: String,
                  @Query("password") password: String,
                  @Query("mobile") mobile: String?,
                  @Query("uuid") uuid: String,
                  @Query("andriod_id") deviceId: String,
                  @Query("show") show: String,
                  @Query("lang") lang: String,
                  @Query("player_id") playerId: String): Call<NetworkGetLoginUser>

    data class NetworkGetLoginUser(
        @SerializedName("status") val status: String,
        @SerializedName("data") val data: User1,
        @SerializedName("message") val message: String
    )
}