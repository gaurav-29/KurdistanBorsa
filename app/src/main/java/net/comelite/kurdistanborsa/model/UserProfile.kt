package net.comelite.kurdistanborsa.model
import com.google.gson.annotations.SerializedName


data class UserProfile(
    @SerializedName("data")
    val data: Data? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null
)

data class Data(
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("comments")
    val comments: String? = null,
    @SerializedName("company_name")
    val companyName: String? = null,
    @SerializedName("contact")
    val contact: String? = null,
    @SerializedName("expired")
    val expired: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("img")
    val img: String? = null,
    @SerializedName("is_active")
    val isActive: Int? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("show")
    val show: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("user_type")
    val userType: String? = null,
    @SerializedName("username")
    val username: String? = null
)