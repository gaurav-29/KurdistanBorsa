package net.comelite.kurdistanborsa.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.Exception

data class User1(
    @SerializedName("uuid") val uuid: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("show") val show: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("expired") val expiryDate: String?,
    @SerializedName("is_notification_enabled") val isNotificationEnabled: String?
)

data class UserInfo(
    val uuid: String,
    val id: String,
    val type: String,
    val show: String,
    val language: String,
    val expiryDate: String,
    val isNotificationEnabled: String
): Serializable {
    companion object {
        fun create(data: User1): UserInfo? {
            try {
                return UserInfo(
                    uuid = data.uuid ?: return null,
                    id = data.id ?: return null,
                    type = data.type ?: return null,
                    show = data.show ?: return null,
                    language = data.language ?: return null,
                    expiryDate = data.expiryDate ?: return null,
                    isNotificationEnabled = data.isNotificationEnabled ?: "1"
                )
            } catch (e: Exception) {
                Log.e("NST", "Exception in User.create()")
                e.printStackTrace()
                return null
            }
        }
    }
}