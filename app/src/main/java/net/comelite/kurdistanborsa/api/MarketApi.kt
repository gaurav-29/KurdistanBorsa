package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName

import net.comelite.kurdistanborsa.model.MarketValue
import net.comelite.kurdistanborsa.model.MarketValue1
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApi {
    @GET("webservices.php?")
    fun getMarketValue(@Query("function") func: String): Call<NetworkGetMarket>

    data class NetworkGetMarket (
        @SerializedName("status") val status: String,
        @SerializedName("is_active") val isActive: String,
        @SerializedName("data") val data: List<MarketValue1>,
        @SerializedName("signupCheck") val signUpCheck: String,
        @SerializedName("expired") val expired: Int,
        @SerializedName("is_login_required") val isLoginRequired: String?,
        @SerializedName("is_borsa_login_required") val isBorsaLoginRequired: String,
        @SerializedName("show") val show: String)

}