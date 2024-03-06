package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName
import net.comelite.kurdistanborsa.model.AdsValue1
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AdApi {
    @GET("webservices.php?")
    fun getAdsValue(@Query("function") func: String): Call<NetworkGetAds>

    /*data class NetworkGetAds(
        @SerializedName("status") val status: String,
        @SerializedName("data") val data: List<AdsValue>
    )*/

    data class NetworkGetAds(
        @SerializedName("status") val status: String?,
        @SerializedName("data") val data: List<AdsValue1>?
    )
}