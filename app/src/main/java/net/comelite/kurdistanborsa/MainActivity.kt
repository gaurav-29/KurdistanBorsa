package net.comelite.kurdistanborsa

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import net.comelite.kurdistanborsa.api.*
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.fragment.ProfileFragment
import net.comelite.kurdistanborsa.fragment.RegistrationFragment
import net.comelite.kurdistanborsa.fragment.BorsaWatchlistFragment
import net.comelite.kurdistanborsa.fragment.MarketWatchlistFragment
import net.comelite.kurdistanborsa.model.*
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val appDelegate = AppDelegate.applicationContext()
    lateinit var toolbar: Toolbar
    var adsArrayList = ArrayList<String>()
    private lateinit var progressDialog: Dialog
    var navView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (appDelegate.saveLanguageIs.isNotBlank()) {
            updateAppLanguage(appDelegate.saveLanguageIs)
        }

        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.nav_view)

        // Manthan 24-12-2021
//        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navView?.setupWithNavController(navController)

        //NST(Binjal) 15-7-20
        navView?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_market -> {
                    Log.e("B_Item", "Market")
                    navController.navigate(R.id.navigation_market)
                    fetchNewNotifications()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_borsa -> {
                    Log.e("B_Item", "Borsa")
                    navController.navigate(R.id.navigation_borsa)
                    fetchNewNotifications()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_notifications -> {
                    Log.e("B_Item", "Notification")
                    navController.navigate(R.id.navigation_notifications)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_settings -> {
                    Log.e("B_Item", "Setting")
                    navController.navigate(R.id.navigation_settings)
                    fetchNewNotifications()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        progressDialog = CustomProgressView.initWith(this)

        if (!Constants.isNetworkAvailable(this)) {
            try {
                val builder = android.app.AlertDialog.Builder(this, R.style.AlertDialogCustom)
                builder.setTitle(this.getString(R.string.no_internet_available))
                builder.setPositiveButton(this.getString(R.string.retry)) { dialog, which -> this.recreate() }
                builder.setNegativeButton(this.getString(R.string.exit)) { dialog, which -> this.finishAffinity() }
                builder.setCancelable(false)
                builder.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val carouselView = findViewById<CarouselView>(R.id.cvAds)

        getAds {
            if (it) {
                carouselView.pageCount = adsArrayList.size
            }
        }
        carouselView.setImageListener(imageListener)
    }

    private var imageListener: ImageListener = ImageListener { position, imageView ->
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        Glide.with(this@MainActivity).load(adsArrayList[position]).into(imageView)
    }

    private fun getAds(mCallback: (Boolean) -> Unit) {
        val apiRequest = RetrofitClient.getInstance().create(AdApi::class.java)

        apiRequest.getAdsValue("showAdds").enqueue(object : Callback<AdApi.NetworkGetAds> {
            override fun onFailure(call: Call<AdApi.NetworkGetAds>, t: Throwable) {
                Log.e("NST_Ads", "Get Ads Value Failed ${t.message}")
                mCallback.invoke(false)
                //showMessage(t.message.toString())
            }

            override fun onResponse(
                call: Call<AdApi.NetworkGetAds>,
                response: Response<AdApi.NetworkGetAds>
            ) {
                //Log.e("NST_Ads", "Get Ads OnResponse Success ${response}")

                if (response.isSuccessful) {
                    if (response.body()?.status == "true" && response.body()?.data != null) {
                        Log.e("NST_Ads", response.body()!!.data.toString())
                        adsArrayList.clear()
                        val arrayList = response.body()!!.data ?: emptyList()
                        if (arrayList.isNotEmpty()) {
                            val dataList = arrayList.mapNotNull { p0 -> Ads.create(p0) }
                            for (ads in dataList) {
                                adsArrayList.add(ads.image)
                                Log.e("NST_Ads_list", adsArrayList.toString())
                                mCallback.invoke(true)
                            }
                        } else {
                            mCallback.invoke(false)
                        }
                        //Log.e("NST_Ads", "Get Ads Response body ${response.body()}")
                    } else {
                        mCallback.invoke(false)
                    }
                } else {
                    mCallback.invoke(false)
                }
            }
        })
    }

    fun updateAppLanguage(saveLanguage: String?) {
        val config = this.resources.configuration
        val locale = Locale(saveLanguage)
        Locale.setDefault(locale)
        config.locale = locale
        resources.updateConfiguration(config, this.resources.displayMetrics)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.childFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        )
            ?.let {

                if (it is RegistrationFragment) {
                    findNavController(R.id.nav_host_fragment).popBackStack()
                    //Log.e("NST_C", "$it Registration: True")
                } else if (it is BorsaWatchlistFragment) {
                    findNavController(R.id.nav_host_fragment).popBackStack()
                } else if (it is MarketWatchlistFragment) {
                    findNavController(R.id.nav_host_fragment).popBackStack()
                } else if (it is ProfileFragment) {
                    findNavController(R.id.nav_host_fragment).popBackStack()
                } else {
                    //Log.e("NST_C", "$it Registration: False")
                    if (doubleBackToExitPressedOnce) {
                        //finish()
                        //exitProcess(0)
                        this.finishAffinity()
                    }

                    this.doubleBackToExitPressedOnce = true
                    showMessage(this.getString(R.string.please_back_again))

                    Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 3000)
                }
            }
    }

    internal fun BottomNavigationView.checkItem(actionId: Int) {
        menu.findItem(actionId)?.isChecked = true
    }

    fun changeNotificationBadge() {
        if (appDelegate.unseenNotifications > 0 &&
            appDelegate.isMarketLoggedIn &&
            appDelegate.currentUserID.isNotBlank()) {
            val badge = navView?.getOrCreateBadge(R.id.navigation_notifications)
            badge?.isVisible = true
            badge?.number = appDelegate.unseenNotifications
        } else {
            val badge = navView?.getBadge(R.id.navigation_notifications)
            if (badge != null) {
                badge.isVisible = false
                badge.clearNumber()
            }
        }
    }

    fun fetchNewNotifications() {
        if (Constants.isNetworkAvailable(this) &&
            appDelegate.isMarketLoggedIn &&
            appDelegate.currentUserID.isNotBlank()) {
            val apiRequest = RetrofitClient.getInstance().create(NotificationApi::class.java)
            apiRequest.getNotification("showNotifications", appDelegate.currentUserID!!)
                .enqueue(object : Callback<NotificationApi.NetworkGetNotification> {
                    override fun onFailure(
                        call: Call<NotificationApi.NetworkGetNotification>,
                        t: Throwable
                    ) {
                        Log.e("NST", "E001: Error in Notification unseen count:${t.message.toString()}")
                        changeNotificationBadge()
                    }

                    override fun onResponse(
                        call: Call<NotificationApi.NetworkGetNotification>,
                        response: Response<NotificationApi.NetworkGetNotification>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()!!.status == "true" && response.body()?.count != null) {
                                appDelegate.unseenNotifications = response.body()!!.count!!.toIntOrNull() ?: 0
                                Log.e("NST", "Notification unseen count:${appDelegate.unseenNotifications}")
                            } else {
                                Log.e("NST", "E002: Error in Notification unseen count: Status or count null")
                            }
                        } else {
                            Log.e("NST", "E003: Error in Notification unseen count: ${response.errorBody()?.string()}")
                        }
                        changeNotificationBadge()
                    }
                })
        }
    }

    // Manthan 3-3-2022
    // Check what types of login is require
    fun getSettings() {
        if (Constants.isNetworkAvailable(this)) {
            SettingsApi.create().getAllSettings(
                function = "getSettings",
                userId = "")
                .enqueue(object : Callback<BaseSettings> {
                    override fun onFailure(call: Call<BaseSettings>, t: Throwable) {
                        Log.e("NST", "E01: Error is MainActivity GetSettings ${t.message}")
                    }

                    override fun onResponse(call: Call<BaseSettings>, response: Response<BaseSettings>) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val dataList = response.body()?.data ?: emptyList()
                                if (dataList.isNotEmpty()) {
                                    val settings = Settings.create(dataList.first())
                                    if (settings != null) {
                                        if (settings.isLoginRequired == "1") {
                                            AppDelegate.applicationContext().isMarketLoginRequire = SignInState.REQUIRE
                                        } else {
                                            AppDelegate.applicationContext().isMarketLoginRequire = SignInState.NOTREQUIRE
                                        }
                                        if (settings.isBorsaLoginRequired == "1") {
                                            AppDelegate.applicationContext().isBorsaLoginRequire = SignInState.REQUIRE
                                        } else {
                                            AppDelegate.applicationContext().isBorsaLoginRequire = SignInState.NOTREQUIRE
                                        }
                                        Log.e("NST", "MarketLogin state: ${AppDelegate.applicationContext().isMarketLoginRequire}")
                                        Log.e("NST", "BorsaLogin state: ${AppDelegate.applicationContext().isBorsaLoginRequire}")
                                    } else {
                                        Log.e("NST", "E02: Error is MainActivity: Settings is null")
                                    }
                                } else {
                                    Log.e("NST", "E03: Error is MainActivity: Settings is null")
                                }
                            } else {
                                Log.e("NST", "E04: Error is MainActivity: Settings is null")
                            }
                        } else {
                            Log.e("NST", "E05: Error is MainActivity: Unsuccessful response")
                        }
                    }
                })
        }
    }
}
