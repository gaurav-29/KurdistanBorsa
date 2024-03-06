package net.comelite.kurdistanborsa.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MarketValue1 (
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("from_id") val fromId: String?,
    @SerializedName("to_id") val toId: String?,
    @SerializedName("ask") val  ask: String?,
    @SerializedName("bid") val bid: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("from_currency_id") val fromCurrencyId: String?,
    @SerializedName("from_currency_code") val fromCurrencyCode: String?,
    @SerializedName("to_currency_id") val toCurrencyId: String?,
    @SerializedName("to_currency_code") val toCurrencyCode: String?,
    @SerializedName("from_flag") val fromFlag: String?,
    @SerializedName("to_flag") val toFlag: String?,
    @SerializedName("bid_arrow") val bidArrow: Int?,
    @SerializedName("ask_arrow") val askArrow: Int?
)

data class Market(
    val id: String,
    //val title: String,
    val fromId: String,
    val toId: String,
    val ask: String,
    val bid: String,
    val date: String,
    val time: String,
    val fromCurrencyId: String,
    val fromCurrencyCode: String,
    val toCurrencyId: String,
    val toCurrencyCode: String,
    val fromFlag: String,
    val toFlag: String,
    val bidArrow: Int,
    val askArrow: Int
) : Serializable {
    companion object {
        fun create(data: MarketValue1): Market? {
            try {
                return Market(
                    id = data.id ?: return null,
                    //title = data.title  ?: return null,
                    fromId = data.fromId ?: return null,
                    toId = data.toId ?: return null,
                    ask = data.ask ?: return null,
                    bid = data.bid ?: return null,
                    date = data.date ?: return null,
                    time = data.time ?: return null,
                    fromCurrencyId = data.fromCurrencyId ?: return null,
                    fromCurrencyCode = data.fromCurrencyCode ?: return null,
                    toCurrencyId = data.toCurrencyId ?: return null,
                    toCurrencyCode = data.toCurrencyCode ?: return null,
                    fromFlag = data.fromFlag ?: return null,
                    toFlag = data.toFlag ?: return null,
                    bidArrow = data.bidArrow ?: return null,
                    askArrow = data.askArrow ?: return null
                )
            } catch (e: Exception) {
                Log.e("NST", "Exception in Market.create()")
                e.printStackTrace()
                return null
            }

        }
    }
}