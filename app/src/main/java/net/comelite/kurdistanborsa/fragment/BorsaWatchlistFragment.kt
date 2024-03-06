package net.comelite.kurdistanborsa.fragment

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.toolbar_main.*
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.adapter.BorsaWatchlistAdapter
import net.comelite.kurdistanborsa.api.BorsaApi
import net.comelite.kurdistanborsa.api.WatchlistApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.Borsa
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class BorsaWatchlistFragment : Fragment() {

    private val appDelegate = AppDelegate.applicationContext()
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: BorsaWatchlistAdapter
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    var watchList = ArrayList<Borsa>()
    var filteredWatchlist = ArrayList<Borsa>()
    private var progressDialog: Dialog? = null
    lateinit var root: View
    lateinit var smallBTN: AppCompatRadioButton
    lateinit var mediumBTN: AppCompatRadioButton
    lateinit var largeBTN: AppCompatRadioButton
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var widthIndp: Float = 0.0f
    var fontSize: String = ""
    lateinit var infoLL: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = if (appDelegate.saveLanguageIs == "en") {
            inflater.inflate(R.layout.fragment_borsa_watchlist, container, false)
        } else {
            inflater.inflate(R.layout.fragment_borsa_watchlist_mirror, container, false)
        }

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.show()
                it.supportActionBar!!.setTitle(R.string.title_watchlist)
                it.toolbar.setNavigationOnClickListener {
                    findNavController().navigate(R.id.navigation_borsa)
                }
            }
        }

        sharedPref = requireContext().getSharedPreferences("Borsa", Context.MODE_PRIVATE)
        editor= sharedPref.edit()

        smallBTN = root.findViewById(R.id.smallBTN)
        mediumBTN = root.findViewById(R.id.mediumBTN)
        largeBTN = root.findViewById(R.id.largeBTN)
        infoLL = root.findViewById(R.id.infoLL)

        val widthInPixels = resources.displayMetrics.widthPixels
        widthIndp = widthInPixels/resources.displayMetrics.density
        Log.e("SIZE", "widthInPx = $widthInPixels and widthIndp = $widthIndp")

        val density = resources.displayMetrics.density
        val dpToPixel = (widthIndp * density).roundToInt()
        Log.e("SIZE", "dpToPx = $dpToPixel, density=$density")

        onClickListeners()

        progressDialog = CustomProgressView.initWith(requireContext())

        recyclerView = root.findViewById(R.id.recycleview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        adapter = BorsaWatchlistAdapter(this, filteredWatchlist, 15, 150)

        recyclerView.adapter = adapter

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefresh_items)

        mSwipeRefreshLayout.setOnRefreshListener {
            getWatchlist()

            if (appDelegate.userExpiredDate.isNotBlank() && appDelegate.userExpiredDate.isNotEmpty()) {
                if (appDelegate.after3DaysDate >= appDelegate.userExpiredDate) {
                    Log.e("Date", "True")
                    showMessage(getString(R.string.your_account_expiry) + " " + appDelegate.userExpiredDate)
                } else {
                    Log.e("Date", "False")
                }
            }
        }

        // For detecting drag & drop and swipe gesture.
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN
            , ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(filteredWatchlist, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                storeFilteredBorsaIdInSharedPref(filteredWatchlist)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                deleteFromWatchlist(position)
            }
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                mSwipeRefreshLayout.isEnabled = false
                viewHolder?.itemView?.setBackgroundColor(Color.GRAY)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                mSwipeRefreshLayout.isEnabled = true
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }).attachToRecyclerView(recyclerView)

        return root
    }

    private fun deleteFromWatchlist(position: Int) {
        if (Constants.isNetworkAvailable(requireContext())) {
            val borsaId = filteredWatchlist[position].id
            val userId = appDelegate.currentUserID
            val lang = appDelegate.saveLanguageIs
            Log.e("ID", "UserId- $userId, BorsaId- $borsaId, Lang- $lang")

            progressDialog?.show()
            val apiRequest = RetrofitClient.getInstance().create(WatchlistApi::class.java)
            apiRequest.deleteFromBorsaWatchlist("deleteWatchlistMarkets", userId, borsaId, lang)
                .enqueue(object : Callback<WatchlistApi.NetworkAddDeleteWatchlist> {
                    override fun onResponse(
                        call: Call<WatchlistApi.NetworkAddDeleteWatchlist>,
                        response: Response<WatchlistApi.NetworkAddDeleteWatchlist>
                    ) {
                        progressDialog?.dismiss()
                        if (response.isSuccessful && response.body() != null) {
                            if (response.body()?.status == "true") {
                                getWatchlist()
                                showMessage(response.body()!!.message.toString())
                                Log.e("NST_Watchlist_borsa", response.body()!!.message.toString())
                            } else {
                                showMessage(resources.getString(R.string.something_went_wrong))
                                Log.e("NST_Watchlist_borsa", response.body()!!.message.toString())
                            }
                        }
                    }
                    override fun onFailure(
                        call: Call<WatchlistApi.NetworkAddDeleteWatchlist>,
                        t: Throwable
                    ) {
                        progressDialog?.dismiss()
                        Log.e("NST_Watchlist_borsa", "Fail- ${t.message}")
                        showMessage(t.message.toString())
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

    private fun storeFilteredBorsaIdInSharedPref(filteredList: ArrayList<Borsa>) {
        val listOfSortedBorsaId: MutableList<String> = java.util.ArrayList()

        for (mBorsa in filteredList) {
            listOfSortedBorsaId.add(mBorsa.id)
        }
        //convert the List of Longs to a JSON string
        val gson = Gson()
        val jsonListOfSortedBorsaIds = gson.toJson(listOfSortedBorsaId)

        //save to SharedPreference
        val edit = Constants.sharedPreferences.edit()
        edit.putString(Constants.PREF_KEY_FILTERED_WATCHLIST_IDS, jsonListOfSortedBorsaIds).apply()
    }
    private fun updateWatchlistData() {
        val filteredSharedPrefWatchlist = ArrayList<Borsa>()

        val jsonListForBorsaId =
            Constants.sharedPreferences.getString(Constants.PREF_KEY_FILTERED_WATCHLIST_IDS, "")

        if (!jsonListForBorsaId.isNullOrEmpty()) {
            val gson = Gson()

            val listOfSortedBorasId: List<String> =
                gson.fromJson(jsonListForBorsaId, object : TypeToken<List<String?>?>() {}.type)

            //build filter list
            if (!listOfSortedBorasId.isNullOrEmpty()) {
                for (id in listOfSortedBorasId) {
                    for (borsa in watchList) {
                        if (borsa.id == id) {
                            filteredSharedPrefWatchlist.add(borsa)
                            watchList.remove(borsa)
                            break
                        }
                    }
                }
            }

            /**
             * NST(Binjal)
             * if there are still borsa that were not in the filter list
             * maybe they were added after the last drag and drop
             * add them to the filter list
             */

            if (watchList.isNotEmpty()) {
                filteredSharedPrefWatchlist.addAll(watchList)
            }
            filteredWatchlist.clear()
            filteredWatchlist.addAll(filteredSharedPrefWatchlist)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()

        } else {
            filteredWatchlist.clear()
            filteredWatchlist.addAll(watchList)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()
        }
    }
    private fun getWatchlist() {
        if (mSwipeRefreshLayout.isRefreshing) {
            mSwipeRefreshLayout.isRefreshing = false
        }
        if (watchList.isNotEmpty()) {
            watchList.clear()
        }
        if (Constants.isNetworkAvailable(requireActivity())) {

            val userId = appDelegate.currentUserID
            val lang = appDelegate.saveLanguageIs

            progressDialog?.show()
            val apiRequest = RetrofitClient.getInstance().create(WatchlistApi::class.java)
            apiRequest.showBorsaWatchlist("showWatchlistMarkets", userId, lang)
                .enqueue(object : Callback<BorsaApi.NetworkGetBorsa> {
                    override fun onFailure(call: Call<BorsaApi.NetworkGetBorsa>, t: Throwable) {
                        Log.e("NST_borsa_watchlist", "Failed to get watchlist ${t.message}")
                        showMessage(t.message.toString())
                    }

                    override fun onResponse(
                        call: Call<BorsaApi.NetworkGetBorsa>,
                        response: Response<BorsaApi.NetworkGetBorsa>
                    ) {
                        //Log.e("NST_Borsa1","Get Borsa OnResponse Success ${response}")

                        if (response.isSuccessful) {
                            progressDialog?.dismiss()
                            if (response.body() != null && response.body()?.status == "true") {
                                Log.e("WATCH", "Response successful")
                                val dataList = response.body()!!.data ?: emptyList()
                                watchList.addAll(dataList.mapNotNull { p0 -> Borsa.create(p0) })
                                updateWatchlistData()
                                //recyclerView.adapter = adapter
                            } else {
                                infoLL.visibility = View.VISIBLE
                            }
                        } else {
                            Log.e("WATCH", "Response not successful")
                            showMessage(response.errorBody().toString())
                            progressDialog?.dismiss()
                        }
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
    private fun onClickListeners() {
        smallBTN.setOnClickListener {
            doChangesForSmall()
            editor.putString("Size", "Small")
            editor.apply()
        }
        mediumBTN.setOnClickListener {
            doChangesForMedium()
            editor.putString("Size", "Medium")
            editor.apply()
        }
        largeBTN.setOnClickListener {
            doChangesForLarge()
            editor.putString("Size", "Large")
            editor.apply()
        }
    }

    private fun doChangesForSmall() {
        smallBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        mediumBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        largeBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        if (widthIndp < 400) {
            adapter.setFontSize(12)
            // Link for below line- https://developer.android.com/training/multiscreen/screendensities#TaskUseDP
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else if (widthIndp >= 400 && widthIndp <= 600) {
            adapter.setFontSize(18)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else {
            adapter.setFontSize(20)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
    }

    private fun doChangesForMedium() {
        smallBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        mediumBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        largeBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        if (widthIndp < 400) {
            adapter.setFontSize(15)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else if (widthIndp >= 400 && widthIndp <= 600) {
            adapter.setFontSize(20)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else {
            adapter.setFontSize(25)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
    }
    private fun doChangesForLarge() {
        smallBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        mediumBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        largeBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        if (widthIndp < 400) {
            adapter.setFontSize(18)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else if (widthIndp >= 400 && widthIndp <= 600) {
            adapter.setFontSize(22)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 95.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else {
            adapter.setFontSize(30)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
    }
    override fun onResume() {
        super.onResume()
        Log.e("NST", "BorsaWatchlist on resume")

        activity?.let {
            it.toolbar.navigationIcon = ContextCompat.getDrawable(it, R.drawable.ic_back)
        }

        fontSize = sharedPref.getString("Size", "")!!
        Log.e("SIZE", fontSize)
        when (fontSize) {
            "Small" -> doChangesForSmall()
            "Medium" -> doChangesForMedium()
            "Large" -> doChangesForLarge()
            else -> doChangesForMedium()
        }
        getWatchlist()
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

        activity?.let {
            it.toolbar.navigationIcon = ContextCompat.getDrawable(it, R.mipmap.ic_action_logo)
        }

        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }
}