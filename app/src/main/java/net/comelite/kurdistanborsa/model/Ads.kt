package net.comelite.kurdistanborsa.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AdsValue1 (
    @SerializedName("id") val id: String?,
    //@SerializedName("title") val title: String,
    @SerializedName("name") val name: String?,
    @SerializedName("publish_date") val publishDate: String?,
    @SerializedName("expiry_date") val  expiryDate: String?,
    @SerializedName("image") val image: String?
)

data class Ads(
    val id: String,
    val name: String,
    val publishDate: String,
    val expiryDate: String,
    val image: String
): Serializable {
    companion object {
        fun create(data: AdsValue1): Ads? {
            try {
                return Ads(
                    id = data.id ?: return null,
                    name = data.name ?: return null,
                    publishDate = data.publishDate ?: return null,
                    expiryDate = data.expiryDate ?: return null,
                    image = data.image ?: return null
                )
            } catch (e: Exception) {
                Log.e("NST", "Exception in Ads.create()")
                e.printStackTrace()
                return null
            }
        }
    }
}