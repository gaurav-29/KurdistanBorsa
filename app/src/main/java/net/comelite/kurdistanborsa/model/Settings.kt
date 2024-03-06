package net.comelite.kurdistanborsa.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.Serializable

enum class SignInState {
    UNIDEFINE, REQUIRE, NOTREQUIRE
}

data class BaseSettings(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: List<NetworkSettings>?
)

data class NetworkSettings(
    @SerializedName("is_signup") val isSignup: String?,
    @SerializedName("is_login_required") val isLoginRequired: String?,
    @SerializedName("is_borsa_login_required") val isBorsaLoginRequired: String?,
    @SerializedName("contact") val contact: String?,
    @SerializedName("info") val info: String?,
    @SerializedName("show") val show: String?,
    @SerializedName("is_notification_enabled") val isNotificationEnabled: String?
    )


data class Settings(
    val isSignup: String,
    val isLoginRequired: String,
    val isBorsaLoginRequired: String,
    val contact: String,
    val info: String,
    val show: String,
    val isNotificationEnabled: String
) : Serializable {
    companion object {
        fun create(data: NetworkSettings): Settings? {
            try {
                return Settings(
                    isSignup = data.isSignup ?: return null,
                    isLoginRequired = data.isLoginRequired  ?: return null,
                    isBorsaLoginRequired = data.isBorsaLoginRequired ?: "",
                    contact = data.contact ?: return null,
                    info = data.info ?: return null,
                    show = data.show ?: return null,
                    isNotificationEnabled = data.isNotificationEnabled ?: "1"
                    )
            } catch (e: Exception) {
                Log.e("NST", "Exception in Settings.create()")
                e.printStackTrace()
                return null
            }

        }
    }
}