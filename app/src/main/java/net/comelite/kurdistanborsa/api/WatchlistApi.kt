package net.comelite.kurdistanborsa.api

import com.google.gson.annotations.SerializedName
import net.comelite.kurdistanborsa.model.BorsaValue1
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WatchlistApi {

    @GET("webservices.php?")
    fun addToBorsaWatchlist(@Query("function") func: String,
                       @Query("user_id") userId: String,
                       @Query("markets_id") marketsId: String,
                       @Query("lang") lang: String
    ): Call<NetworkAddDeleteWatchlist>

    @GET("webservices.php?")
    fun addToMarketWatchlist(@Query("function") func: String,
                            @Query("user_id") userId: String,
                            @Query("currencies_id") currenciesId: String,
                            @Query("lang") lang: String
    ): Call<NetworkAddDeleteWatchlist>

    @GET("webservices.php?")
    fun deleteFromBorsaWatchlist(@Query("function") func: String,
                       @Query("user_id") userId: String,
                       @Query("markets_id") marketsId: String,
                       @Query("lang") lang: String
    ): Call<NetworkAddDeleteWatchlist>

    @GET("webservices.php?")
    fun deleteFromMarketWatchlist(@Query("function") func: String,
                                 @Query("user_id") userId: String,
                                 @Query("currencies_id") currenciesId: String,
                                 @Query("lang") lang: String
    ): Call<NetworkAddDeleteWatchlist>

    @GET("webservices.php?")
    fun showBorsaWatchlist(@Query("function") func: String,
                       @Query("user_id") userId: String,
                       @Query("lang") lang: String
    ): Call<BorsaApi.NetworkGetBorsa>

    @GET("webservices.php?")
    fun showMarketWatchlist(@Query("function") func: String,
                      @Query("user_id") userId: String,
                      @Query("lang") lang: String
    ): Call<MarketApi.NetworkGetMarket>

    data class NetworkAddDeleteWatchlist(
        @SerializedName("status") val status: String?,
        @SerializedName("message") val message: String?
    )
}