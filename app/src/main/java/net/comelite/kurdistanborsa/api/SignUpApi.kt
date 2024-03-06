package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SignUpApi {
    @GET("webservices.php?")
    fun signUpUser(@Query("function") func: String, @Query("username") userName: String, @Query("password") password: String,
                   @Query("contact") mobNo: String, @Query("uuid") uuid: String,
                   @Query("andriod_id") deviceId: String,
                   @Query("show") show: String, @Query("lang") lang: String, @Query("type") type: String): Call<NetworkGetSignUpUser>

    data class NetworkGetSignUpUser(
        @SerializedName("status") val status: String,
        @SerializedName("message") val message: String
    )
}