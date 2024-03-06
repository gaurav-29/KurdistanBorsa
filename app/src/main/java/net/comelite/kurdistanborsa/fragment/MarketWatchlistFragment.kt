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
import net.comelite.kurdistanborsa.adapter.MarketAdapter
import net.comelite.kurdistanborsa.adapter.MarketWatchlistAdapter
import net.comelite.kurdistanborsa.api.BorsaApi
import net.comelite.kurdistanborsa.api.MarketApi
import net.comelite.kurdistanborsa.api.WatchlistApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.Borsa
import net.comelite.kurdistanborsa.model.Market
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MarketWatchlistFragment : Fragment() {

    private val appDelegate = AppDelegate.applicationContext()
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: MarketWatchlistAdapter
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    var watchList = ArrayList<Market>()
    var filteredWatchlist = ArrayList<Market>()
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
            inflater.inflate(R.layout.fragment_market_watchlist, container, false)
        } else {
            inflater.inflate(R.layout.fragment_market_watchlist_mirror, container, false)
        }

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.show()
                it.supportActionBar!!.setTitle(R.string.title_watchlist)
                it.toolbar.setNavigationOnClickListener {
                    findNavController().navigate(R.id.navigation_market)
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

        adapter = MarketWatchlistAdapter(this, requireContext(), filteredWatchlist, 16, 150, 42,42)

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
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN
            , ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(filteredWatchlist, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                storeFilteredMarketIdInSharedPref(filteredWatchlist)
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
            val marketId = filteredWatchlist[position].id
            val userId = appDelegate.currentUserID
            val lang = appDelegate.saveLanguageIs
            Log.e("ID", "UserId- $userId, MarketId- $marketId, Lang- $lang")

            progressDialog?.show()
            val apiRequest = RetrofitClient.getInstance().create(WatchlistApi::class.java)
            apiRequest.deleteFromMarketWatchlist("deleteWatchlistCurrencies", userId, marketId, lang)
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
                                Log.e("NST_Watchlist_market", response.body()!!.message.toString())
                            } else {
                                showMessage(resources.getString(R.string.something_went_wrong))
                                Log.e("NST_Watchlist_market", response.body()!!.message.toString())
                            }
                        }
                    }
                    override fun onFailure(
                        call: Call<WatchlistApi.NetworkAddDeleteWatchlist>,
                        t: Throwable
                    ) {
                        progressDialog?.dismiss()
                        Log.e("NST_Watchlist_market", "Fail ${t.message}")
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
    private fun storeFilteredMarketIdInSharedPref(filterList: java.util.ArrayList<Market>) {
        val listOfSortedMarketId: MutableList<String> = java.util.ArrayList()

        for (mMarket in filterList) {
            listOfSortedMarketId.add(mMarket.id)
        }
        //convert the List of Longs to a JSON string
        val gson = Gson()
        val jsonListOfSortedMarketIds = gson.toJson(listOfSortedMarketId)

        //save to SharedPreference
        val edit = Constants.sharedPreferences.edit()
        edit.putString(Constants.PREF_KEY_FILTERED_WATCHLIST_MARKET_IDS, jsonListOfSortedMarketIds).apply()
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
            apiRequest.showMarketWatchlist("showWatchCurrencies", userId, lang)
                .enqueue(object : Callback<MarketApi.NetworkGetMarket> {
                    override fun onFailure(call: Call<MarketApi.NetworkGetMarket>, t: Throwable) {
                        Log.e("NST_market_watchlist", "Failed to get watchlist ${t.message}")
                        showMessage(t.message.toString())
                    }

                    override fun onResponse(
                        call: Call<MarketApi.NetworkGetMarket>,
                        response: Response<MarketApi.NetworkGetMarket>
                    ) {
                        //Log.e("NST_Borsa1","Get Borsa OnResponse Success ${response}")

                        if (response.isSuccessful) {
                            progressDialog?.dismiss()
                            if (response.body() != null && response.body()?.status == "true") {
                                Log.e("NST_market_watchlist", "Response successful")
                                val dataList = response.body()!!.data ?: emptyList()
                                watchList.addAll(dataList.mapNotNull { p0 -> Market.create(p0) })
                                updateWatchlistData()
                                //recyclerView.adapter = adapter
                            } else {
                                infoLL.visibility = View.VISIBLE
                            }
                        } else {
                            Log.e("NST_market_watchlist", "Response not successful")
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
    private fun updateWatchlistData() {
        val filterSharedPrefList = ArrayList<Market>()

        val jsonListForMarketId =
            Constants.sharedPreferences.getString(Constants.PREF_KEY_FILTERED_WATCHLIST_MARKET_IDS, "")

        if (!jsonListForMarketId.isNullOrEmpty()) {
            val gson = Gson()

            val listOfSortedMarketId: List<String> =
                gson.fromJson(jsonListForMarketId, object : TypeToken<List<String?>?>() {}.type)

            //build filter list
            if (!listOfSortedMarketId.isNullOrEmpty()) {
                for (id in listOfSortedMarketId) {
                    for (market in watchList) {
                        if (market.id == id) {
                            filterSharedPrefList.add(market)
                            watchList.remove(market)
                            break
                        }
                    }
                }
            }

            /**
             * NST(Gaurav)
             * if there are still market that were not in the filter list
             * maybe they were added after the last drag and drop
             * add them to the filter list
             */

            if (watchList.isNotEmpty()) {
                filterSharedPrefList.addAll(watchList)
            }
            filteredWatchlist.clear()
            filteredWatchlist.addAll(filterSharedPrefList)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()
        }
        else {
            filteredWatchlist.clear()
            filteredWatchlist.addAll(watchList)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()
        }
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

    private fun onClickListeners() {
        smallBTN.setOnClickListener {
            doChangesForSmall()
            editor.putString("MSize", "Small")
            editor.apply()
        }
        mediumBTN.setOnClickListener {
            doChangesForMedium()
            editor.putString("MSize", "Medium")
            editor.apply()
        }
        largeBTN.setOnClickListener {
            doChangesForLarge()
            editor.putString("MSize", "Large")
            editor.apply()
        }
    }

    private fun doChangesForSmall() {
        smallBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        mediumBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        largeBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        if (widthIndp < 400) {
            adapter.setFontSize(10)
            //adapter.setTextViewWidth(130)
            adapter.setImageViewWidthAndHeight(36, 36)
            // Link for below line- https://developer.android.com/training/multiscreen/screendensities#TaskUseDP
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else if (widthIndp >= 400 && widthIndp <= 600) {
            adapter.setFontSize(14)
            //adapter.setTextViewWidth(220)
            adapter.setImageViewWidthAndHeight(50, 50)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else {
            adapter.setFontSize(20)
            adapter.setImageViewWidthAndHeight(68, 68)
        }
    }
    private fun doChangesForMedium() {
        smallBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        mediumBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        largeBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        if (widthIndp < 400) {
            adapter.setFontSize(12)
            //adapter.setTextViewWidth(160)
            adapter.setImageViewWidthAndHeight(42, 42)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)

        }
        else if (widthIndp >= 400 && widthIndp <= 600) {
            adapter.setFontSize(16)
            // adapter.setTextViewWidth(260)
            adapter.setImageViewWidthAndHeight(60, 60)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 95.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else {
            adapter.setFontSize(25)
            adapter.setImageViewWidthAndHeight(78, 78)
        }
    }
    private fun doChangesForLarge() {
        smallBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        mediumBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_background_color)
        largeBTN.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        if (widthIndp < 400) {
            adapter.setFontSize(14)
            //adapter.setTextViewWidth(180)
            adapter.setImageViewWidthAndHeight(48, 48)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else if (widthIndp >= 400 && widthIndp <= 600) {
            adapter.setFontSize(17)
            //adapter.setTextViewWidth(280)
            adapter.setImageViewWidthAndHeight(76, 76)
            val textViewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 105.0f, resources.displayMetrics).toInt()
            adapter.setTextViewWidth(textViewWidth)
        }
        else {
            adapter.setFontSize(30)
            adapter.setImageViewWidthAndHeight(94, 94)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("NST", "MarketWatchlist on resume")

        activity?.let {
            it.toolbar.navigationIcon = ContextCompat.getDrawable(it, R.drawable.ic_back)
        }

        fontSize = sharedPref.getString("MSize", "")!!
        Log.e("SIZE", fontSize)
        when (fontSize) {
            "Small" -> doChangesForSmall()
            "Medium" -> doChangesForMedium()
            "Large" -> doChangesForLarge()
            else -> doChangesForMedium()
        }
        getWatchlist()
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

    //NST 15-7-20 For transactiontoolargeexception solve
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }
}