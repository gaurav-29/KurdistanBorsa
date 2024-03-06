package net.comelite.kurdistanborsa.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.onesignal.OneSignal
import net.comelite.kurdistanborsa.api.LoginApi
import net.comelite.kurdistanborsa.model.SignInState
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AppDelegate: Application() {

    companion object {
        private var instance: AppDelegate? = null

        fun applicationContext(): AppDelegate {
            return instance as AppDelegate
        }
    }
    init {
        instance = this
    }

    var isMarketLoginRequire: SignInState = SignInState.UNIDEFINE
    var isBorsaLoginRequire: SignInState = SignInState.UNIDEFINE

    var isMarketLoggedIn: Boolean = false
    var isBorsaLoggedIn: Boolean = false

    var saveLanguageIs = "en"
    var currentUserID: String = ""

    var androidDeviceId: String = ""
    var playerId: String = ""
    var sharedPrefShowValue = ""

    var after3DaysDate = ""
    var userExpiredDate = ""

    var appIsFirstTimeOpen = true

    var isNotificationEnable = true
    var unseenNotifications = 0

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

        androidDeviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        Log.e("NST","Android device id is $androidDeviceId")

        val sharedPrefCurrentUser = Constants.sharedPreferences.getString(Constants.PREF_KEY_CURRENT_USER_ID, null)
        if (!sharedPrefCurrentUser.isNullOrBlank()) {
            currentUserID = sharedPrefCurrentUser
            Log.e("NST","Current user id is $currentUserID")
        } else {
            Constants.sharedPreferences.edit().putBoolean(Constants.PREF_KEY_IS_LOGGED_IN, false).apply()
        }

        val sharedPrefSelectedLang = Constants.sharedPreferences.getString(Constants.PREF_KEY_SELECT_LANGUAGE, null)
        if (!sharedPrefSelectedLang.isNullOrBlank()) {
            saveLanguageIs = sharedPrefSelectedLang
            Log.e("NST","Save Language is $saveLanguageIs")
        }
        OneSignal.sendTag(OneSignalKeys.kLanguage, saveLanguageIs)

        val calender = Calendar.getInstance()
        calender.add(Calendar.DAY_OF_YEAR, 3)

        after3DaysDate = Constants.dateFormat.format(calender.time)

        OneSignal.initWithContext(this)
        OneSignal.setAppId(Constants.ONESIGNAL_APP_ID)
        playerId = OneSignal.getDeviceState()?.userId ?: ""
        OneSignal.addSubscriptionObserver {
            playerId = it.to.userId
            Log.e("NST", "OneSignal Player Id(Observer):\t${playerId}")
        }
        Log.e("NST", "OneSignal Player Id:\t${playerId}")
    }

    fun hideKeyboard(view: View) {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
//OLd
//Firebase Server Key
//AIzaSyBNGrnIiZ2SGkiKj1R-gZ30JnbZw8lTjgw
//Firebase Sender Id
//789664744070