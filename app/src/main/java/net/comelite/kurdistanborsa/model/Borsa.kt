package net.comelite.kurdistanborsa.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.Exception

data class BorsaValue1 (
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("title_en") val titleEnglish: String?,
    @SerializedName("title_ar") val titleArabic: String?,
    @SerializedName("title_ku") val titleKurdish: String?,
    @SerializedName("ask") val  ask: String?,
    @SerializedName("bid") val bid: String?,
    @SerializedName("bid_arrow") val bidArrow: Int?,
    @SerializedName("ask_arrow") val askArrow: Int?,
    @SerializedName("is_open") val isOpen: Int?,
    @SerializedName("is_hidden") val isHidden: Int?
)

data class Borsa(
    val id: String,
    val title: String,
    val titleEnglish: String,
    val titleArabic: String,
    val titleKurdish: String,
    val ask: String,
    val bid: String,
    val bidArrow: Int,
    val askArrow: Int,
    val isOpen: Int,
    val isHidden: Int
) : Serializable {
    companion object {
        fun create(data: BorsaValue1) : Borsa? {
            try {
                return Borsa(
                    id = data.id ?: return null,
                    title = data.title ?: return null,
                    titleEnglish = data.titleEnglish ?: return null,
                    titleArabic = data.titleArabic ?: return null,
                    titleKurdish = data.titleKurdish ?: return null,
                    ask = data.ask ?: return null,
                    bid = data.bid ?: return null,
                    bidArrow = data.bidArrow ?: return null,
                    askArrow = data.askArrow ?: return null,
                    isOpen = data.isOpen ?: return null,
                    isHidden = data.isHidden ?: return null
                    )
            } catch (e: Exception) {
                Log.e("NST", "Exception in Borsa.create()")
                e.printStackTrace()
                return null
            }
        }
    }
}