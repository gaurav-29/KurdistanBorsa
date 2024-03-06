package net.comelite.kurdistanborsa.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_main.view.*
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.adapter.NotificationAdapter
import net.comelite.kurdistanborsa.api.NotificationApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.BaseSettings
import net.comelite.kurdistanborsa.model.Notification
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsFragment : Fragment(), ImageInterface {

    private var progressDialog: Dialog? = null
    val appDelegate = AppDelegate.applicationContext()

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: NotificationAdapter
    var notificationList = ArrayList<Notification>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.show()
                it.supportActionBar!!.setTitle(R.string.title_notifications)
            }
        }

        progressDialog = CustomProgressView.initWith(requireContext())

        recyclerView = root.findViewById(R.id.recycleview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        adapter = NotificationAdapter(this, notificationList)
        recyclerView.adapter = adapter

        return root
    }

    override fun onResume() {
        super.onResume()
        if (notificationList.isNotEmpty()) {
            notificationList.clear()
        }
        getNotifications()
    }

    private fun getNotifications() {
        if (Constants.isNetworkAvailable(requireActivity())) {
            if (appDelegate.isMarketLoggedIn && appDelegate.currentUserID.isNotBlank()) {
                progressDialog!!.show()
                val apiRequest = RetrofitClient.getInstance().create(NotificationApi::class.java)
                apiRequest.getNotification("showNotifications", appDelegate.currentUserID)
                    .enqueue(object : Callback<NotificationApi.NetworkGetNotification> {
                        override fun onFailure(
                            call: Call<NotificationApi.NetworkGetNotification>,
                            t: Throwable
                        ) {
                            Log.e("NST_Notification", "Get Notificatino Value Failed ${t.message}")
                            progressDialog!!.dismiss()
                            showMessage(t.message.toString())
                        }

                        override fun onResponse(
                            call: Call<NotificationApi.NetworkGetNotification>,
                            response: Response<NotificationApi.NetworkGetNotification>
                        ) {
                            //Log.e("NST_Notification","Get Notification OnResponse Success ${response}")
                            readAllNotifications()
                            if (response.isSuccessful) {
                                if (response.body()!!.status == "true" && response.body()?.data != null) {
                                    val dataList = response.body()!!.data ?: emptyList()
                                    if (dataList.isNotEmpty()) {
                                        for (data in dataList) {
                                            val mDataList = data.notificationInfo
                                            if (mDataList.isNotEmpty()) {
                                                val notifications = mDataList.mapNotNull { p0 -> Notification.create(p0) }
                                                notificationList.addAll(notifications)
                                            }
                                        }
                                        updateAdapter()
                                    }
                                    //Log.e("NST_Notification","Get Notification Response body ${response.body()}")
                                } else {
                                    //showMessage(response.errorBody().toString())
                                    progressDialog!!.dismiss()
                                    Log.e(
                                        "NST_Notification",
                                        "Get Notification data null ${response.errorBody()
                                            .toString()}"
                                    )
                                }
                            } else {
                                //showMessage(response.errorBody().toString())
                                Log.e(
                                    "NST_Notification",
                                    "Get Notification response false ${response.errorBody()
                                        .toString()}"
                                )
                                progressDialog!!.dismiss()
                            }

                        }

                    })
            }

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

    private fun readAllNotifications() {
        if (Constants.isNetworkAvailable(requireActivity())) {
            if (appDelegate.isMarketLoggedIn && appDelegate.currentUserID.isNotBlank()) {
                val apiRequest = RetrofitClient.getInstance().create(NotificationApi::class.java)
                apiRequest.readAllNotifications(
                    func = "seenNotification",
                    userId = appDelegate.currentUserID!!,
                    language = appDelegate.saveLanguageIs
                ).enqueue(object : Callback<BaseSettings> {
                    override fun onFailure(call: Call<BaseSettings>, t: Throwable) {
                        Log.e("NST", "E001: Error in Notification read:${t.message.toString()}")
                    }

                    override fun onResponse(call: Call<BaseSettings>, response: Response<BaseSettings>) {
                        if (response.isSuccessful) {
                            if (response.body()!!.status == "true") {
                                Log.e("NST", "All Notification read")
                            } else {
                                Log.e("NST", "E002: Error in Notification read: Status or count null")
                            }
                        } else {
                            Log.e("NST", "E003: Error in Notification read: ${response.errorBody()?.string()}")
                        }
                    }
                })
            }
        }
    }

    private fun updateAdapter() {
        progressDialog!!.dismiss()
        adapter.notifyDataSetChanged()
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

    override fun onStop() {
        super.onStop()

        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun popUpImage(position: Int) {
        val b = Bundle()
        b.putString("imageUrl", notificationList[position].notificationImg)
        findNavController().navigate(R.id.image_popup_dialog, b)
    }
}

interface ImageInterface {
    fun popUpImage(position: Int)
}