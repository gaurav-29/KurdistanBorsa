package net.comelite.kurdistanborsa.model

import com.google.gson.annotations.SerializedName

data class BorsaValue (
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("title_en") val titleEnglish: String,
    @SerializedName("title_ar") val titleArabic: String,
    @SerializedName("title_ku") val titleKurdish: String,
    @SerializedName("ask") val  ask: String,
    @SerializedName("bid") val bid: String,
    @SerializedName("bid_arrow") val bidArrow: Int,
    @SerializedName("ask_arrow") val askArrow: Int
)

data class MarketValue (
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("from_id") val fromId: String,
    @SerializedName("to_id") val toId: String,
    @SerializedName("ask") val  ask: String,
    @SerializedName("bid") val bid: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("from_currency_id") val fromCurrencyId: String,
    @SerializedName("from_currency_code") val fromCurrencyCode: String,
    @SerializedName("to_currency_id") val toCurrencyId: String,
    @SerializedName("to_currency_code") val toCurrencyCode: String,
    @SerializedName("from_flag") val fromFlag: String,
    @SerializedName("to_flag") val toFlag: String,
    @SerializedName("bid_arrow") val bidArrow: Int,
    @SerializedName("ask_arrow") val askArrow: Int
)

data class AdsValue (
    @SerializedName("id") val id: String,
    //@SerializedName("title") val title: String,
    @SerializedName("name") val name: String,
    @SerializedName("publish_date") val publishDate: String,
    @SerializedName("expiry_date") val  expiryDate: String,
    @SerializedName("image") val image: String
)

data class User(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("show") val show: String,
    @SerializedName("language") val language: String,
    @SerializedName("expired") val expiryDate: String
)

data class Notifications(
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("info") val notificationInfo: List<NotificationInfo>
)

data class NotificationInfo(
    @SerializedName("eng_title") val engTitle: String,
    @SerializedName("eng_message") val engMessage: String,
    @SerializedName("kur_title") val kurTitle: String,
    @SerializedName("kur_message") val kurMessage: String,
    @SerializedName("ar_title") val arTitle: String,
    @SerializedName("ar_message") val arMessage: String,
    @SerializedName("not_image") val notificationImg: String,
    @SerializedName("date") val date: String
)
