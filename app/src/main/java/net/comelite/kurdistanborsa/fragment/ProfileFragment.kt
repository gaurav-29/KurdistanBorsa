package net.comelite.kurdistanborsa.fragment

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_main.view.*
import net.comelite.kurdistanborsa.MainActivity
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.api.LogoutApi
import net.comelite.kurdistanborsa.api.UserApi
import net.comelite.kurdistanborsa.api.WatchlistApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.SignInState
import net.comelite.kurdistanborsa.model.UserProfile
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private val appDelegate = AppDelegate.applicationContext()
    private lateinit var progressDialog: Dialog
    lateinit var usernameTV: TextView
    lateinit var usernameTV2: TextView
    lateinit var phoneNumberTV: TextView
    lateinit var phoneNumberTV2: TextView
    lateinit var kPasswordTV: TextView
    lateinit var passwordTV: TextView
    lateinit var kExpiryDateTV: TextView
    lateinit var expiryDateTV: TextView
    lateinit var contactUsBTN: Button
    lateinit var logoutBTN: Button
    lateinit var mSpannableString: SpannableString

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        progressDialog = CustomProgressView.initWith(requireContext())
        usernameTV = view.findViewById(R.id.usernameTV)
        usernameTV2 = view.findViewById(R.id.usernameTV2)
        phoneNumberTV = view.findViewById(R.id.phoneNumberTV)
        phoneNumberTV2 = view.findViewById(R.id.phoneNumberTV2)
        contactUsBTN = view.findViewById(R.id.contactUsBTN)
        logoutBTN = view.findViewById(R.id.logoutBTN)
        kPasswordTV = view.findViewById(R.id.kPasswordTV)
        passwordTV = view.findViewById(R.id.passwordTV)
        kExpiryDateTV = view.findViewById(R.id.kExpiryDateTV)
        expiryDateTV = view.findViewById(R.id.expiryDateTV)

        getUser()

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.show()
                it.supportActionBar!!.setTitle(R.string.title_profile)
                it.toolbar.setNavigationOnClickListener {
                    findNavController().navigate(R.id.navigation_settings)
                }
            }
        }

        onClickListeners()

        if (appDelegate.saveLanguageIs == "ku" || appDelegate.saveLanguageIs == "ar") {
            usernameTV.layoutDirection = View.LAYOUT_DIRECTION_RTL
            usernameTV.textDirection = View.TEXT_DIRECTION_RTL
            usernameTV2.layoutDirection = View.LAYOUT_DIRECTION_RTL
            usernameTV2.textDirection = View.TEXT_DIRECTION_RTL

            phoneNumberTV.layoutDirection = View.LAYOUT_DIRECTION_RTL
            phoneNumberTV.textDirection = View.TEXT_DIRECTION_RTL
            phoneNumberTV2.layoutDirection = View.LAYOUT_DIRECTION_RTL
            phoneNumberTV2.textDirection = View.TEXT_DIRECTION_RTL

            kPasswordTV.layoutDirection = View.LAYOUT_DIRECTION_RTL
            kPasswordTV.textDirection = View.TEXT_DIRECTION_RTL
            passwordTV.layoutDirection = View.LAYOUT_DIRECTION_RTL
            passwordTV.textDirection = View.TEXT_DIRECTION_RTL

            kExpiryDateTV.layoutDirection = View.LAYOUT_DIRECTION_RTL
            kExpiryDateTV.textDirection = View.TEXT_DIRECTION_RTL
            expiryDateTV.layoutDirection = View.LAYOUT_DIRECTION_RTL
            expiryDateTV.textDirection = View.TEXT_DIRECTION_RTL
            Log.e("Lang", "en false")
        } else {
            usernameTV.layoutDirection = View.LAYOUT_DIRECTION_LTR
            usernameTV.textDirection = View.TEXT_DIRECTION_LTR
            usernameTV2.layoutDirection = View.LAYOUT_DIRECTION_LTR
            usernameTV2.textDirection = View.TEXT_DIRECTION_LTR

            phoneNumberTV.layoutDirection = View.LAYOUT_DIRECTION_LTR
            phoneNumberTV.textDirection = View.TEXT_DIRECTION_LTR
            phoneNumberTV2.layoutDirection = View.LAYOUT_DIRECTION_LTR
            phoneNumberTV2.textDirection = View.TEXT_DIRECTION_LTR
            Log.e("Lang", "en true")
        }

        return view
    }

    private fun getUser() {
        if (Constants.isNetworkAvailable(requireContext())) {
            progressDialog.show()
            Log.e("UID", appDelegate.currentUserID)
            val apiRequest = RetrofitClient.getInstance().create(UserApi::class.java)
            apiRequest.getUser("displayUser", appDelegate.currentUserID)
                .enqueue(object : Callback<UserProfile> {
                    override fun onResponse(
                        call: Call<UserProfile>,
                        response: Response<UserProfile>
                    ) {
                        progressDialog.dismiss()
                        if (response.isSuccessful && response.body() != null) {
                            if (response.body()?.status == "true") {
                                usernameTV2.text = response.body()!!.data?.username
                                phoneNumberTV2.text = response.body()!!.data?.contact
                                passwordTV.text = response.body()!!.data?.password
                                expiryDateTV.text = response.body()!!.data?.expired
                            } else {
                                Toast.makeText(requireContext(),resources.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show()
                                Log.e("NST_Profile", response.body()!!.message.toString())
                            }
                        }
                    }
                    override fun onFailure(
                        call: Call<UserProfile>,
                        t: Throwable
                    ) {
                        progressDialog.dismiss()
                        Log.e("NST_Profile", "Fail- ${t.message}")
                        Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_LONG).show()
                    }
                })
        } else {
            val builder =
                android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            builder.setTitle(requireContext().getString(R.string.no_internet_available))
            builder.setPositiveButton(requireContext().getString(R.string.retry)) { dialog, which -> requireActivity().recreate() }
            builder.setNegativeButton(requireContext().getString(R.string.exit)) { dialog, which -> requireActivity().finishAffinity() }
            builder.setCancelable(false)
            builder.show()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            it.toolbar.navigationIcon = ContextCompat.getDrawable(it, R.drawable.ic_back)
        }
    }
    override fun onStop() {
        super.onStop()
        activity?.let {
            it.toolbar.navigationIcon = ContextCompat.getDrawable(it, R.mipmap.ic_action_logo)
        }
    }

    private fun onClickListeners() {
        contactUsBTN.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("kurdistan.borsa@outlook.com"))
            try { // the user can choose the email client
                startActivity(Intent.createChooser(intent, "Choose an email client from..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(activity, "No email client configured!", Toast.LENGTH_LONG).show()
            }
        }
        logoutBTN.setOnClickListener {
            logout()
        }
    }
    private fun logout() {
        val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
        builder.setTitle(this.getString(R.string.are_you_sure_logout))
        builder.setPositiveButton(this.getString(R.string.yes)) { dialog, which ->
            progressDialog.show()
            getLogout { flag ->
                progressDialog.dismiss()
                if (flag) {
                    // Manthan 29-12-2021
                    // Disable notification tag in OneSignal, so user will not receive push notification after getting logged out (Tag based notifications)
                    //OneSignal.sendTag(OneSignalKeys.kNotificationEnable, "0")
                    OneSignal.disablePush(true)
                    appDelegate.unseenNotifications = 0
                    (requireActivity() as? MainActivity)?.changeNotificationBadge()
                    val edit = Constants.sharedPreferences.edit()
                    edit.putBoolean(Constants.PREF_KEY_IS_LOGGED_IN, false)
                    edit.putBoolean(Constants.PREF_KEY_IS_MARKET_LOGIN, false)
                    edit.remove(Constants.PREF_KEY_CURRENT_USER_ID)
                    edit.remove(Constants.PREF_KEY_USER_NAME)
                    edit.remove(Constants.PREF_KEY_PASSWORD)
                    edit.remove(Constants.PREF_KEY_SHOW_VALUE)
                    edit.apply()
                    AppDelegate.applicationContext().isMarketLoginRequire = SignInState.UNIDEFINE
                    AppDelegate.applicationContext().isBorsaLoginRequire = SignInState.UNIDEFINE
                    AppDelegate.applicationContext().isMarketLoggedIn = false
                    AppDelegate.applicationContext().isBorsaLoggedIn = false
                    AppDelegate.applicationContext().currentUserID = ""
                    AppDelegate.applicationContext().sharedPrefShowValue = ""
                    AppDelegate.applicationContext().userExpiredDate = ""
                    Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.navigation_settings)
                    Log.e("NST", "Logout")
                }
            }
        }
        builder.setNegativeButton(this.getString(R.string.cancel)) { dialog, which ->
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun getLogout(mCallback: (Boolean) -> Unit) {
        val apiRequest = RetrofitClient.getInstance().create(LogoutApi::class.java)
        apiRequest.logoutUser("logout", appDelegate.currentUserID!!, "1")
            .enqueue(object : Callback<LogoutApi.NetworkGetLogoutUser> {
                override fun onFailure(call: Call<LogoutApi.NetworkGetLogoutUser>, t: Throwable) {
                    Log.e("NST_Logout", "Logout Failed ${t.message}")
                    Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                    mCallback.invoke(false)
                }

                override fun onResponse(
                    call: Call<LogoutApi.NetworkGetLogoutUser>,
                    response: Response<LogoutApi.NetworkGetLogoutUser>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.status == "true") {
                            mCallback.invoke(true)
                            appDelegate.appIsFirstTimeOpen = true
                        } else {
                            Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_SHORT).show()
                            mCallback.invoke(false)
                        }
                    } else {
                        Toast.makeText(requireContext(), response.errorBody()?.string(), Toast.LENGTH_SHORT).show()
                        mCallback.invoke(false)
                    }
                }
            })
    }
}