package net.comelite.kurdistanborsa.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.toolbar_main.view.*
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.api.SettingsApi
import net.comelite.kurdistanborsa.api.SignUpApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.BaseSettings
import net.comelite.kurdistanborsa.model.Settings
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationFragment : Fragment() {

    lateinit var mView: View
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var etUserName: TextInputEditText
    lateinit var etPassword: TextInputEditText
    lateinit var etMob: TextInputEditText
    lateinit var tvSignupInfo: TextView
    lateinit var btnRegistration: Button
    private lateinit var progressDialog: Dialog

    var appDelegate = AppDelegate.applicationContext()

    var showValue = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_registration, container, false)

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.hide()
            }
        }

        /**
         * NST(Binjal) 29-6-20
         * If typeId = 1 its Borsa Signup
         * If typeId = 2 its Market Signup
         */
        showValue = if (arguments == null) {
            "2"
        } else {
            arguments?.getString("typeId")!!
        }

        toolbar = mView.findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.title_registration)
        toolbar.cancelIcon.visibility = View.VISIBLE

        toolbar.cancelIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        etUserName = mView.findViewById(R.id.etUserName)
        etPassword = mView.findViewById(R.id.etUserPassword)
        etMob = mView.findViewById(R.id.etMobile)
        tvSignupInfo = mView.findViewById(R.id.tvSignuptInfo)

        btnRegistration = mView.findViewById(R.id.btnRegistration)
        progressDialog = CustomProgressView.initWith(requireContext())

        /**
         * NST(Binjal) 26-6-20
         * Change LTR to RTL Programmatically
         * XML in some inputType not work
         */
        when (appDelegate.saveLanguageIs) {
            "en" -> {
                /*etUserName.gravity = GravityCompat.START
                etPassword.gravity = GravityCompat.START
                etMob.gravity = GravityCompat.START*/
                tvSignupInfo.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            }
            "ku" -> {
                /*etUserName.gravity = GravityCompat.END
                etPassword.gravity = GravityCompat.END
                etMob.gravity = GravityCompat.END*/
                tvSignupInfo.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            }
            else -> {
                /*etUserName.gravity = GravityCompat.END
                etPassword.gravity = GravityCompat.END
                etMob.gravity = GravityCompat.END*/
                tvSignupInfo.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            }
        }

        if (!Constants.isNetworkAvailable(requireContext())) {
            val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            builder.setTitle(requireContext().getString(R.string.no_internet_available))
            builder.setPositiveButton(requireContext().getString(R.string.retry)) { dialog, which -> requireActivity().recreate() }
            builder.setNegativeButton(requireContext().getString(R.string.exit)) { dialog, which -> requireActivity().finishAffinity() }
            builder.setCancelable(false)
            builder.show()
        } else {
            getSettings()
        }

        btnRegistration.setOnClickListener {
            appDelegate.hideKeyboard(it)

            if (checkValidation()) {
                progressDialog.show()
                getRegistration()
            }
        }
        return mView
    }

    private fun getSettings() {
        progressDialog.show()
        Log.e("NST","Get Settings Called")
        SettingsApi.create().getAllSettings("getSettings", "").enqueue(object : Callback<BaseSettings> {
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
                            tvSignupInfo.text = settingList[0].info
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

    private fun getRegistration() {
        val apiRequest = RetrofitClient.getInstance().create(SignUpApi::class.java)
        /**
         *  NST(Binjal) 24-6-20
         * UUID = Android device id
         * show = 3 (static)
         * type = comp (static)
         */
        apiRequest.signUpUser(func = "userSignup", userName = etUserName.text.toString(), password = etPassword.text.toString(), mobNo = etMob.text.toString(), uuid = "uuid", deviceId = "",
            show = showValue, lang = appDelegate.saveLanguageIs, type = "").enqueue(object :
            Callback<SignUpApi.NetworkGetSignUpUser> {
            override fun onFailure(call: Call<SignUpApi.NetworkGetSignUpUser>, t: Throwable) {
                Log.e("NST_Signup","Signup Value Failed ${t.message}")
                showMessage(t.message.toString())
                progressDialog.dismiss()
            }

            override fun onResponse(call: Call<SignUpApi.NetworkGetSignUpUser>, response: Response<SignUpApi.NetworkGetSignUpUser>) {
                //Log.e("NST_Signup","Signup OnResponse Success ${response}")
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    if (response.body()!!.status == "true") {
                        Log.e("NST","Registration Successfully")
                        findNavController().popBackStack()
                        showMessage(response.body()!!.message)
                        Log.e("NST_Signup","Signup Response body ${response.body()}")
                    } else {
                        Log.e("NST_Signup","Signup Response status false ${response.body()!!.message}")
                        showMessage(response.body()!!.message)
                    }
                } else {
                    progressDialog.dismiss()
                    Log.e("NST_Signup","Signup Response Unsuccessful")
                }
            }
        })
    }

    private fun checkValidation(): Boolean {

        if (etMob.text!!.isEmpty() || etMob.text.isNullOrBlank()) {
            //etMob.error = "Field cannot be empty"
            etMob.error = getText(R.string.enter_valid_mobile)
            etMob.requestFocus()
            return false
        } else if (etMob.text!!.toString().length < 11) {
            etMob.error = getText(R.string.mobile_not_less_than)
            etMob.requestFocus()
            return false
        } else if (etPassword.text!!.isEmpty() || etPassword.text.isNullOrBlank()) {
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
