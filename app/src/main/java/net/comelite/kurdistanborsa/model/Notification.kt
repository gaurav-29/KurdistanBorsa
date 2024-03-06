package net.comelite.kurdistanborsa.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Notifications1(
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("info") val notificationInfo: List<NotificationInfo1>
)

data class NotificationInfo1(
    @SerializedName("eng_title") val engTitle: String?,
    @SerializedName("eng_message") val engMessage: String?,
    @SerializedName("kur_title") val kurTitle: String?,
    @SerializedName("kur_message") val kurMessage: String?,
    @SerializedName("ar_title") val arTitle: String?,
    @SerializedName("ar_message") val arMessage: String?,
    @SerializedName("not_image") val notificationImg: String?,
    @SerializedName("date") val date: String?
)

data class Notification(
    val engTitle: String,
    val engMessage: String,
    val kurTitle: String,
    val kurMessage: String,
    val arTitle: String,
    val arMessage: String,
    val notificationImg: String,
    val date: String
) : Serializable {
    companion object {
        fun create( data: NotificationInfo1) : Notification? {
            try {
                return Notification(
                    engTitle = data.engTitle ?: return null,
                    engMessage = data.engMessage ?: return null,
                    kurTitle = data.kurTitle ?: return null,
                    kurMessage = data.kurMessage ?: return null,
                    arTitle = data.arTitle ?: return null,
                    arMessage = data.arMessage ?: return null,
                    notificationImg = data.notificationImg ?: return null,
                    date = data.date ?: return null
                )
            } catch (e: Exception) {
                Log.e("NST", "Exception in Notification.create()")
                e.printStackTrace()
                return null
            }
        }
    }
}