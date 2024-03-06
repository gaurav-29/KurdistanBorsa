package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName

import net.comelite.kurdistanborsa.model.BorsaValue
import net.comelite.kurdistanborsa.model.BorsaValue1
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BorsaApi {
    @GET("webservices.php?")
    fun getBorsaValue(@Query ("function") func: String): Call<NetworkGetBorsa>

    data class NetworkGetBorsa(
        @SerializedName("status") val status: String?,
        @SerializedName("is_active") val isActive: String?,
        @SerializedName("data") val data: List<BorsaValue1>?,
        @SerializedName("expired") val expired: Int?,
        @SerializedName("is_login_required") val isLoginRequired: String?,
        @SerializedName("is_borsa_login_required") val isBorsaLoginRequired: String?,
        @SerializedName("show") val show: String?)

}