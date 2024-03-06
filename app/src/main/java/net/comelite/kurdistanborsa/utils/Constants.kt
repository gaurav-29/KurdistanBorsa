package net.comelite.kurdistanborsa.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import java.text.SimpleDateFormat


enum class OperatedCountry(val countryCode: String) {
    UNITED_KINGDOM("UK"),
    NETHERLANDS("NL"),
    GERMANY("DE"),
    SWEDEN("SE")


}


class Constants {
    companion object {
        const val ONESIGNAL_APP_ID = "e8d307cb-b9da-4e02-aa02-b4224dba606a"

        private const val PREF_NAME = "net.comelite.kurdistanborsa"

        const val PREF_KEY_IS_LOGGED_IN = "prefKeyIsLoggedIn"

        const val PREF_KEY_USER_NAME = "prefKeyUserName"

        const val PREF_KEY_PASSWORD = "prefKeyUserPassword"

        const val PREF_KEY_MOBILE = "prefKeyUserMobile"

        const val PREF_KEY_SHOW_VALUE = "prefKeyUserShowValue"

        const val PREF_KEY_CURRENT_USER_ID = "pref_key_current_user"

        const val PREF_KEY_SELECT_LANGUAGE = "pref_key_select_language"

        const val PREF_KEY_STORE_BORSALIST = "pref_key_store_borsalist"

        const val PREF_KEY_FILTERED_WATCHLIST_IDS = "pref_key_filtered_watchlist_ids"

        const val PREF_KEY_FILTERED_WATCHLIST_MARKET_IDS = "pref_key_filtered_watchlist_market_ids"

        const val PREF_KEY_STORE_MARKETLIST = "pref_key_store_marketlist"

        const val PREF_KEY_IS_MARKET_LOGIN = "prefKeyIsMarketLogin"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        val readableDateFormat = SimpleDateFormat("dd-MM-yyyy")

        val code12Hours = SimpleDateFormat("hh:mm:ss")

        val sharedPreferences: SharedPreferences =
            AppDelegate.applicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        const val CHANNEL_ID = "kurdistan_borsa"

        const val CHANNEL_NAME = "Simplified Coding"

        const val CHANNEL_DESC = "Android Push Notification Tutorial"


        fun isNetworkAvailable(context: Context): Boolean {
            var result = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                connectivityManager.run {
                    connectivityManager.activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }

                    }
                }
            }

            return result
        }

        fun getDeviceWidth(context: Context): Int {
            val displayMetrics = DisplayMetrics()
            val windowmanager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowmanager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }

        fun getDeviceHeight(context: Context): Int {
            val displayMetrics = DisplayMetrics()
            val windowmanager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowmanager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }

    }


}