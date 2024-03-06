package net.comelite.kurdistanborsa.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_main.view.*
import net.comelite.kurdistanborsa.MainActivity
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.api.LanguageApi
import net.comelite.kurdistanborsa.api.LogoutApi
import net.comelite.kurdistanborsa.api.SettingsApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.BaseSettings
import net.comelite.kurdistanborsa.model.Settings
import net.comelite.kurdistanborsa.model.SignInState
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import net.comelite.kurdistanborsa.utils.OneSignalKeys
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingFragment : Fragment() {
    lateinit var mView: View
    lateinit var spinner: TextView
    lateinit var spinnerRTL: TextView
    lateinit var contactInfo: TextView
    lateinit var btnSave: Button
    lateinit var notificationSwitch: SwitchMaterial
    var selectedLanguage = ""
    private val appDelegate = AppDelegate.applicationContext()
    private lateinit var progressDialog: Dialog
    lateinit var btnShareApp: Button

    var array = arrayOf<String>()

    var checkedItem = 0

    var mActivity: Activity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(appDelegate.isMarketLoggedIn && appDelegate.currentUserID.isNotBlank())
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity) {
            mActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment_setting, container, false)

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.show()
                it.supportActionBar!!.setTitle(R.string.title_settings)
            }
        }

        array = arrayOf(
            requireContext().getString(R.string.english_lan),
            requireContext().getString(R.string.kurdish_lan),
            requireContext().getString(R.string.arabic_lan)
        )

        btnShareApp = mView.findViewById(R.id.btnShareApp)
        btnSave = mView.findViewById(R.id.btnSaveSetting)
        spinner = mView.findViewById(R.id.spinnerLanguage)
        spinnerRTL = mView.findViewById(R.id.spinnerLanguageRTL)
        contactInfo = mView.findViewById(R.id.tvContactNumberInfo)
        progressDialog = CustomProgressView.initWith(requireContext())

        notificationSwitch = mView.findViewById(R.id.notificationSwitch)
        notificationSwitch.isChecked = appDelegate.isNotificationEnable
        notificationSwitch.setOnCheckedChangeListener { _ , isChecked ->
            changeNotificationState(isChecked)
        }

        val dataAdapter: ArrayAdapter<CharSequence> =
            ArrayAdapter.createFromResource(requireContext(), R.array.SpinnerLanguageArray, R.layout.spinner_item)

        dataAdapter.setDropDownViewResource(R.layout.spinner_dialog)

        if (appDelegate.saveLanguageIs == "ku" || appDelegate.saveLanguageIs == "ar") {
            spinner.visibility = View.GONE
            spinnerRTL.visibility = View.VISIBLE
            notificationSwitch.layoutDirection = View.LAYOUT_DIRECTION_RTL
            notificationSwitch.textDirection = View.TEXT_DIRECTION_RTL
            Log.e("Lang", "en false")
        } else {
            spinnerRTL.visibility = View.GONE
            spinner.visibility = View.VISIBLE
            notificationSwitch.layoutDirection = View.LAYOUT_DIRECTION_LTR
            notificationSwitch.textDirection = View.TEXT_DIRECTION_LTR
            Log.e("Lang", "en true")
        }

        if (!appDelegate.saveLanguageIs.isBlank()) {
            val sharedPrefSelectedLang = appDelegate.saveLanguageIs
            var sLanguage = 0
            sLanguage = when (sharedPrefSelectedLang) {
                "gu" -> {
                    3
                }
                "ku" -> {
                    1
                }
                "ar" -> {
                    2
                }
                else -> {
                    0
                }
            }

            checkedItem = sLanguage

            if (appDelegate.saveLanguageIs == "en") {
                spinner.setText(array[sLanguage])
                contactInfo.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            } else {
                spinnerRTL.setText(array[sLanguage])
                contactInfo.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            }

        }

        spinner.setOnClickListener {
            var spinnerSelectedLang = array[checkedItem].toString()

            val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)

            builder.setTitle(requireContext().getString(R.string.spinner_title))

            builder.setSingleChoiceItems(array, checkedItem) { dialog, which ->
                Log.e("NST111", "Spinner11 value is ${array[which].toString()}")
                spinnerSelectedLang = array[which].toString()
                checkedItem = which
            }

            builder.setPositiveButton(requireContext().getString(R.string.ok)) { dialog, which ->
                //selectedLanguage = which.toString()
                Log.e("NST111", "Spinner value is ${which.toString()}")
                spinner.text = spinnerSelectedLang
                selectedLanguage = spinnerSelectedLang
            }

            builder.setCancelable(false)
            builder.show()
        }

        spinnerRTL.setOnClickListener {
            var spinnerSelectedLang = array[checkedItem].toString()

            val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustomRTL)

            builder.setTitle(requireContext().getString(R.string.spinner_title))

            builder.setSingleChoiceItems(array, checkedItem, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    Log.e("NST111", "Spinner11 value is ${array[which].toString()}")
                    spinnerSelectedLang = array[which].toString()
                    checkedItem = which
                }
            })

            builder.setPositiveButton(requireContext().getString(R.string.ok)) { dialog, which ->
                //selectedLanguage = which.toString()
                Log.e("NST111", "Spinner value is ${which.toString()}")
                spinnerRTL.text = spinnerSelectedLang
                selectedLanguage = spinnerSelectedLang
            }

            builder.setCancelable(false)
            builder.show()
        }

        if (!Constants.isNetworkAvailable(requireContext())) {
            val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            builder.setTitle(requireContext().getString(R.string.no_internet_available))
            builder.setPositiveButton(requireContext().getString(R.string.retry)) { dialog, which -> requireActivity().recreate() }
            builder.setNegativeButton(requireContext().getString(R.string.exit)) { dialog, which -> requireActivity().finishAffinity() }
            builder.setCancelable(false)
            builder.show()
        } else {
            if (appDelegate.isMarketLoggedIn && appDelegate.currentUserID.isNotBlank()) {
                getSettings(appDelegate.currentUserID!!)
            } else {
                getSettings()
            }
        }

        btnSave.setOnClickListener {
            //Log.e("NST111", "Selected Lang is ${selectedLanguage}")
            progressDialog.show()
            var saveLanguage = ""
            saveLanguage = when (selectedLanguage) {
                "English" -> {
                    "en"
                }
                "کوردی" -> {
                    "ku"
                }
                "عربي" -> {
                    "ar"
                }
                else -> {
                    // Manthan 08-06-2022
                    // Set existing language by default
                    appDelegate.saveLanguageIs
                }
            }

            if (appDelegate.isMarketLoggedIn && appDelegate.currentUserID.isNotBlank()) {
                updateLanguage(saveLanguage) { flag ->
                    if (flag) {
                        progressDialog.dismiss()
                        val edit = Constants.sharedPreferences.edit()
                        edit.putString(Constants.PREF_KEY_SELECT_LANGUAGE, saveLanguage)
                        edit.apply()
                        appDelegate.saveLanguageIs = saveLanguage
                        OneSignal.sendTag(OneSignalKeys.kLanguage, appDelegate.saveLanguageIs)
                        try {
                            requireActivity().recreate()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            try {
                                mActivity?.recreate()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        progressDialog.dismiss()
                    }
                }
            } else {
                OneSignal.sendTag(OneSignalKeys.kLanguage, saveLanguage)
                progressDialog.dismiss()
                val edit = Constants.sharedPreferences.edit()
                edit.putString(Constants.PREF_KEY_SELECT_LANGUAGE, saveLanguage)
                edit.apply()
                appDelegate.saveLanguageIs = saveLanguage
                Toast.makeText(requireContext(), requireContext().getString(R.string.successfully_updated), Toast.LENGTH_SHORT).show()
                try {
                    requireActivity().recreate()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    try {
                        mActivity!!.recreate()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        btnShareApp.setOnClickListener {
            val shareBottomSheet = ShareBottomSheetFragment()
            shareBottomSheet.show(requireActivity().supportFragmentManager, ShareBottomSheetFragment.TAG)
        }
        return mView
    }

    private fun getSettings(userId: String = "") {
        progressDialog.show()
        Log.e("NST","Get Settings Called")
        SettingsApi.create().getAllSettings(
            function = "getSettings",
            userId = userId)
            .enqueue(object : Callback<BaseSettings> {
            override fun onFailure(call: Call<BaseSettings>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("NST_Settings", t.message ?: "Unknown error")
            }

            override fun onResponse(call: Call<BaseSettings>, response: Response<BaseSettings>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    if (response.body() != null) {
                        val dataList = response.body()?.data ?: emptyList()
                        if (dataList.isNotEmpty()) {
                            val settingList = dataList.mapNotNull { P0 -> Settings.create(P0) }
                            contactInfo.text = settingList[0].contact
                            Log.e("NST_Settings", "Settings DataList $dataList")
                        } else {
                            Log.e("NST_Settings", "Data List is empty")
                        }
                    } else {
                        Log.e("NST_Settings", "Response body null")
                    }
                } else {
                    progressDialog.dismiss()
                    Log.e("NST_Settings", "Settings: Un Successful ${response.errorBody()}")
                }
            }
        })
    }

    //Update Language in api
    private fun updateLanguage(language: String, mCallback: (Boolean) -> Unit) {
        val apiRequest = RetrofitClient.getInstance().create(LanguageApi::class.java)

        /**
         * Test : Add Static id
         * Change that
         */
        Log.e("LANUID", appDelegate.currentUserID)
        Log.e("LAN", language)
        Log.e("LANPRE", appDelegate.saveLanguageIs)
        apiRequest.updateLanguage("updateLanguage", appDelegate.currentUserID!!, language, appDelegate.saveLanguageIs)
            .enqueue(object :
                Callback<LanguageApi.NetworkGetLanguage> {
                override fun onFailure(call: Call<LanguageApi.NetworkGetLanguage>, t: Throwable) {
                    Log.e("NST_UP", "Get UP Value Failed ${t.message}")
                    Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                    mCallback.invoke(false)
                }

                override fun onResponse(
                    call: Call<LanguageApi.NetworkGetLanguage>,
                    response: Response<LanguageApi.NetworkGetLanguage>
                ) {
                    //Log.e("NST_UP","Get UP OnResponse Success ${response}")

                    if (response.isSuccessful) {
                        if (response.body()!!.status == "true") {
                            Toast.makeText(requireContext(), response.body()!!.message, Toast.LENGTH_SHORT).show()
                            mCallback.invoke(true)
                            Log.e("NST_UP", "Get Settings Response body ${response.body()!!.message}")
                        } else {
                            Toast.makeText(requireContext(), response.body()!!.message, Toast.LENGTH_SHORT).show()
                            mCallback.invoke(false)
                            Log.e("NST_UP", "Get Settings Response status false ${response.body()!!.message}")
                        }
                    } else {
                        mCallback.invoke(false)
                        //showMessage(response.body()!!.message)
                    }

                }

            })
    }

    override fun onStop() {
        super.onStop()
        if (progressDialog != null) {
            progressDialog.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                findNavController().navigate(R.id.profileFragment)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun changeNotificationState(isChecked: Boolean) {
        if (appDelegate.isMarketLoggedIn && appDelegate.currentUserID.isNotBlank()) {
//            OneSignal.sendTag(OneSignalKeys.kNotificationEnable, if (isChecked) "1" else "0")
            OneSignal.disablePush(!isChecked)
            progressDialog.show()
            Log.e("NST","Updateing Notification Status to: $isChecked")
            SettingsApi.create().updateNotificationStatus(
                function = "updateNotificationStatus",
                userId = appDelegate.currentUserID!!,
                language = appDelegate.saveLanguageIs,
                isNotificationEnabled = if (isChecked) "1" else "0"
            ).enqueue(object : Callback<BaseSettings> {
                    override fun onFailure(call: Call<BaseSettings>, t: Throwable) {
                        progressDialog.dismiss()
                        Log.e("NST", "Error in Notification Status Update: ${t.message}")
                    }

                    override fun onResponse(call: Call<BaseSettings>, response: Response<BaseSettings>) {
                        if (response.isSuccessful) {
                            progressDialog.dismiss()
                            if (response.body() != null) {
                                Log.e("NST", "Notification Status Update: Successful")
                                appDelegate.isNotificationEnable = isChecked
                            } else {
                                Log.e("NST", "Error in Notification Status Update: Body is null")
                            }
                        } else {
                            progressDialog.dismiss()
                            Log.e("NST", "Error in Notification Status Update: ${response.errorBody()?.string()}")
                        }
                    }
                })
        }
    }
}
