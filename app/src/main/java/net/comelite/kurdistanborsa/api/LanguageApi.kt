package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface LanguageApi {
    @GET("webservices.php?")
    fun updateLanguage(@Query("function") func: String, @Query("user_id") userId: String, @Query("lang") language: String, @Query("prelang") preLanguage: String): Call<NetworkGetLanguage>

    data class NetworkGetLanguage(
        @SerializedName("status") val status: String,
        @SerializedName("message") val message: String
    )
}