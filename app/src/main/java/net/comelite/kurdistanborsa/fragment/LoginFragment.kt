package net.comelite.kurdistanborsa.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_login.*
import net.comelite.kurdistanborsa.BuildConfig
import net.comelite.kurdistanborsa.MainActivity
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.api.LanguageApi
import net.comelite.kurdistanborsa.api.LoginApi
import net.comelite.kurdistanborsa.api.LogoutApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.SignInState
import net.comelite.kurdistanborsa.model.UserInfo
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import net.comelite.kurdistanborsa.utils.OneSignalKeys
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.exp

class LoginFragment : Fragment() {

    private lateinit var mView: View
    lateinit var etPassword: TextInputEditText
    lateinit var etMobile: TextInputEditText
    lateinit var btnLogin: Button
    lateinit var createAccount: Button
    lateinit var titleHeading: TextView
    private lateinit var progressDialog: Dialog
    var userSaveLanguageIs = ""
    val appDelegate = AppDelegate.applicationContext()
    var showValue = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_login, container, false)

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.hide()
            }
        }

        etPassword = mView.findViewById(R.id.etUserPassword)
        etMobile = mView.findViewById(R.id.etMobile)
        btnLogin = mView.findViewById(R.id.btnLogin)
        createAccount = mView.findViewById(R.id.btnCreateAccount)
        progressDialog = CustomProgressView.initWith(requireContext())
        titleHeading = mView.findViewById(R.id.titleHeading)

        /**
         * NST(Binjal) 29-6-20
         * If typeId = 1 its Borsa Login
         * If typeId = 2 its Market Login
         */
        if (arguments == null) {
            showValue = "2"
            titleHeading.text = requireContext().getString(R.string.login_title_market)
        } else {
            showValue = arguments?.getString("typeId")!!
            if (showValue == "1") {
                titleHeading.text = requireContext().getString(R.string.login_title_borsa)
            } else {
                titleHeading.text = requireContext().getString(R.string.login_title_market)
            }
        }

        if (!Constants.isNetworkAvailable(requireContext())) {
            val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            builder.setTitle(requireContext().getString(R.string.no_internet_available))
            builder.setPositiveButton(requireContext().getString(R.string.retry)) { dialog, which -> requireActivity().recreate() }
            builder.setNegativeButton(requireContext().getString(R.string.exit)) { dialog, which -> requireActivity().finishAffinity() }
            builder.setCancelable(false)
            builder.show()
        }

        btnLogin.setOnClickListener {
            appDelegate.hideKeyboard(it)
            if (checkValidation()) {
                progressDialog.show()
                signInUser(etPassword.text.toString(), etMobile.text.toString(), showValue, false)
            }
        }

        createAccount.setOnClickListener {
            val bundle = bundleOf("typeId" to showValue)
            NavHostFragment.findNavController(this).navigate(R.id.action_fragment_login_to_fragment_registration, bundle)
        }

        return mView
    }

    override fun onResume() {
        super.onResume()
        val userName = Constants
            .sharedPreferences
            .getString(Constants.PREF_KEY_USER_NAME, null)
        val password = Constants
            .sharedPreferences
            .getString(Constants.PREF_KEY_PASSWORD, null)
        val mobile = Constants
            .sharedPreferences
            .getString(Constants.PREF_KEY_MOBILE, null)

        if (!password.isNullOrBlank() && !mobile.isNullOrBlank()) {
            signInUser(password, mobile, showValue, true)
        }
    }

    private fun signInUser(password: String, mobile: String, showType: String, isAuto: Boolean) {
        if (AppDelegate.applicationContext().playerId.isBlank()) {
            AppDelegate.applicationContext().playerId = OneSignal.getDeviceState()?.userId ?: ""
        }
        val apiRequest = RetrofitClient.getInstance().create(LoginApi::class.java)
        Log.e("NST", "Password: ${password}\nMobile: ${mobile}\nuuid: ${appDelegate.androidDeviceId}\nShow: ${showType}\nPlayerId: ${appDelegate.playerId}\nLanguage: ${appDelegate.saveLanguageIs}")
        apiRequest.loginUser(
            func = "userLogin",
            username = mobile,
            password = password,
            mobile = mobile,
            uuid = appDelegate.androidDeviceId,
            deviceId = "deviceId",
            show = showType,
            lang = appDelegate.saveLanguageIs,
            playerId = appDelegate.playerId
        ).enqueue(object : Callback<LoginApi.NetworkGetLoginUser> {
            override fun onResponse(
                call: Call<LoginApi.NetworkGetLoginUser>,
                response: Response<LoginApi.NetworkGetLoginUser>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.status == "true" &&
                            !response.body()!!.data.id.isNullOrBlank()) {
                            val user = UserInfo.create(response.body()!!.data)
                            if (user != null) {
                                Log.e("NST", "Auto login successful: ${user}")
                                if (user.show == "1" || user.show == "3") {
                                    AppDelegate.applicationContext().isMarketLoggedIn = true
                                    AppDelegate.applicationContext().isBorsaLoggedIn = true
                                    appDelegate.sharedPrefShowValue = "1"
                                } else if (user.show == "2") {
                                    AppDelegate.applicationContext().isMarketLoggedIn = true
                                    AppDelegate.applicationContext().isBorsaLoggedIn = false
                                    appDelegate.sharedPrefShowValue = user.show
                                }
                                appDelegate.currentUserID = user.id
                                appDelegate.isNotificationEnable = user.isNotificationEnabled == "1"

                                val tags = JSONObject()
                                tags.put(OneSignalKeys.kShow, user.show)
//                                tags.put(OneSignalKeys.kNotificationEnable, user.isNotificationEnabled)
                                tags.put(OneSignalKeys.kLanguage, appDelegate.saveLanguageIs)
                                OneSignal.sendTags(tags)
                                OneSignal.disablePush(!appDelegate.isNotificationEnable)
                                if (user.language != appDelegate.saveLanguageIs) {
                                    updateLanguage(previousLanguage = user.language,
                                        newLanguage = appDelegate.saveLanguageIs)
                                }

                                val edit = Constants.sharedPreferences.edit()
                                edit.putBoolean(Constants.PREF_KEY_IS_LOGGED_IN, true)
                                edit.putString(Constants.PREF_KEY_USER_NAME, mobile)
                                edit.putString(Constants.PREF_KEY_PASSWORD, password)
                                edit.putString(Constants.PREF_KEY_MOBILE, mobile)
                                edit.putString(Constants.PREF_KEY_SHOW_VALUE, appDelegate.sharedPrefShowValue)
                                edit.putBoolean(Constants.PREF_KEY_IS_MARKET_LOGIN, AppDelegate.applicationContext().isMarketLoggedIn)
                                edit.putString(
                                    Constants.PREF_KEY_SHOW_VALUE,
                                    appDelegate.sharedPrefShowValue
                                )
                                edit.apply()

                                val expDate = Constants.dateFormat.parse(user.expiryDate)
                                if (expDate != null) {
                                    Log.e("DATE", expDate.toString())
                                } else {
                                    Log.e("DATE", expDate.toString())
                                }
                                expDate?.let {
                                    appDelegate.userExpiredDate = Constants.dateFormat.format(it)
                                }
                                //appDelegate.userExpiredDate = "2020-08-12"
                                Log.e("NST", "After 3 days date is ${appDelegate.after3DaysDate}")
                                Log.e("NST", "User Expired date is ${appDelegate.userExpiredDate}")

                                if (appDelegate.appIsFirstTimeOpen) {
                                    if (appDelegate.userExpiredDate.isNotBlank()) {
                                        if (appDelegate.after3DaysDate >= appDelegate.userExpiredDate) {
                                            Log.e("DATE", "True")
                                            try {
                                                showMessage(
                                                    getString(R.string.your_account_expiry) + " " + Constants.readableDateFormat.format(
                                                        Constants.dateFormat.parse(appDelegate.userExpiredDate) ?: Date()
                                                    )
                                                )
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        appDelegate.appIsFirstTimeOpen = false
                                    }
                                }
                                progressDialog.dismiss()
                                if (appDelegate.sharedPrefShowValue == "2") {
                                    findNavController().navigate(R.id.navigation_market)
                                } else {
                                    findNavController().navigate(R.id.navigation_borsa)
                                }
                            } else {
                                progressDialog.dismiss()
                                Log.e("NST", "E005: Auto login User null")
                                eraseUserData(true)
                            }
                        } else {
                            progressDialog.dismiss()
                            Log.e("NST", "E003: Auto login status False or UserId is null/blank")
                            showMessage(response.body()!!.message)
                            eraseUserData(false)
                        }
                    } else {
                        progressDialog.dismiss()
                        Log.e("NST", "E002: Auto login response body is null: ${response.errorBody()}")
                        showMessage(response.errorBody()?.string() ?: "Something went wrong!")
                        eraseUserData(true)
                    }
                } else {
                    progressDialog.dismiss()
                    Log.e("NST", "E001: Auto login response unsuccessful: ${response.errorBody()}")
                    showMessage(response.errorBody()?.string() ?: "Something went wrong!")
                    eraseUserData(true)
                }
            }

            override fun onFailure(call: Call<LoginApi.NetworkGetLoginUser>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("NST", "E004: Auto Login failed: ${t.localizedMessage}")
                showMessage(t.localizedMessage ?: "Something went wrong!")
                eraseUserData(true)
            }
        })
    }

    private fun eraseUserData(isForced: Boolean) {
        // Manthan 3-3-2022
        // Remove login credentials if sign response is false
        // Case: Auto sign-in with market creds in Borsa screen will always return false.
        // And if you remove sign-in creds, auto sign-in will not work in Market screen
        // Check if Market login is false
        val isMarketLoggedIn = Constants.sharedPreferences.getBoolean(Constants.PREF_KEY_IS_MARKET_LOGIN, false)
        Log.e("NST", "isMarket: ${isMarketLoggedIn}")
        Log.e("NST", "isForced: ${isForced}")
//        OneSignal.sendTag(OneSignalKeys.kNotificationEnable, "0")
        OneSignal.disablePush(true)
        if (!isMarketLoggedIn || isForced) {
            Log.e("NST", "User data erased")
            val edit = Constants.sharedPreferences.edit()
            edit.putBoolean(Constants.PREF_KEY_IS_LOGGED_IN, false)
            edit.remove(Constants.PREF_KEY_CURRENT_USER_ID)
            edit.remove(Constants.PREF_KEY_USER_NAME)
            edit.remove(Constants.PREF_KEY_PASSWORD)
            edit.remove(Constants.PREF_KEY_MOBILE)
            edit.remove(Constants.PREF_KEY_SHOW_VALUE)
            edit.apply()
            AppDelegate.applicationContext().isMarketLoginRequire = SignInState.UNIDEFINE
            AppDelegate.applicationContext().isBorsaLoginRequire = SignInState.UNIDEFINE
            AppDelegate.applicationContext().isMarketLoggedIn = false
            AppDelegate.applicationContext().isBorsaLoggedIn = false
            AppDelegate.applicationContext().currentUserID = ""
            AppDelegate.applicationContext().sharedPrefShowValue = ""
            AppDelegate.applicationContext().userExpiredDate = ""
        }
    }

    private fun updateLanguage(previousLanguage: String, newLanguage: String) {
        val apiRequest = RetrofitClient.getInstance().create(LanguageApi::class.java)
        apiRequest.updateLanguage(
            func = "updateLanguage",
            userId = appDelegate.currentUserID,
            language = newLanguage,
            preLanguage = previousLanguage
        ).enqueue(object : Callback<LanguageApi.NetworkGetLanguage> {
            override fun onFailure(call: Call<LanguageApi.NetworkGetLanguage>, t: Throwable) {
                Log.e("NST","E001: Unable to update language: ${t.message}")
            }

            override fun onResponse(call: Call<LanguageApi.NetworkGetLanguage>, response: Response<LanguageApi.NetworkGetLanguage>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.status == "true") {
                            Log.e("NST", "Language updated successfully")
                        } else {
                            Log.e("NST", "E002: Unable ot update language: ${response.body()!!.message}")
                        }
                    } else {
                        Log.e("NST", "E003: Unable ot update language: Body is null")
                    }
                } else {
                    Log.e("NST", "E004: Unable ot update language: ${response.errorBody()}")
                }
            }
        })
    }

    // Log-out previous user
    private fun logoutPreviousUser(previousUserId: String) {
        val apiRequest = RetrofitClient.getInstance().create(LogoutApi::class.java)
        apiRequest.logoutUser(
            func = "logout",
            userId = previousUserId,
            isNotificationEnabled = "0"
        ).enqueue(object : Callback<LogoutApi.NetworkGetLogoutUser> {
            override fun onResponse(
                call: Call<LogoutApi.NetworkGetLogoutUser>,
                response: Response<LogoutApi.NetworkGetLogoutUser>
            ) {
                if (response.isSuccessful && response.body()?.status == "true") {
                    Log.e("NST", "Previous user logged-out successfully")
                } else {
                    Log.e("NST", "Previous user logged-out failed")
                }
            }

            override fun onFailure(call: Call<LogoutApi.NetworkGetLogoutUser>, t: Throwable) {
                Log.e("NST", "Previous user logout failed: ${t.message}")
            }
        })
    }
    private fun checkValidation(): Boolean {
//        if (etUserName.text!!.isEmpty() || etUserName.text.isNullOrBlank()) {
//            etUserName.error = "Field Cannot be empty"
//            etUserName.requestFocus()
//            return false
//        }
        if (etMobile.text!!.isEmpty() || etMobile.text.isNullOrBlank()) {
            etMobile.error = getText(R.string.field_cannot_empty)
            etMobile.requestFocus()
            return false
        }
//        else if (etMobile.text!!.toString().length < 11) {
//            etMobile.error = "Please enter valid phone number"
//            etMobile.requestFocus()
//            return false
//        }
        else if (etPassword.text!!.isEmpty() || etPassword.text.isNullOrBlank()) {
            etPassword.error = getText(R.string.enter_password)
            etPassword.requestFocus()
            return false
        }
        return true
    }

    private fun showMessage(message: String) {
        try {
            if (requireContext() != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()

            try {
                if (requireActivity() != null) {
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}
