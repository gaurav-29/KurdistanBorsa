package net.comelite.kurdistanborsa.fragment


import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_main.view.*
import net.comelite.kurdistanborsa.MainActivity
import net.comelite.kurdistanborsa.R
//import net.comelite.kurdistanborsa.adapter.ItemBorsaMoveCallback
import net.comelite.kurdistanborsa.adapter.MarketAdapter
import net.comelite.kurdistanborsa.api.MarketApi
import net.comelite.kurdistanborsa.api.WatchlistApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.Borsa
import net.comelite.kurdistanborsa.model.Market
import net.comelite.kurdistanborsa.model.SignInState
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MarketFragment : Fragment() {

    val appDelegate = AppDelegate.applicationContext()
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: MarketAdapter
    var marketArrayList = ArrayList<Market>()
    var filterMarketArrayList = ArrayList<Market>()
    private var progressDialog: Dialog? = null
    lateinit var root: View
    lateinit var smallBTN: AppCompatRadioButton
    lateinit var mediumBTN: AppCompatRadioButton
    lateinit var largeBTN: AppCompatRadioButton
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var widthIndp: Float = 0.0f
    var fontSize: String = ""
    lateinit var watchlistTV: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e("NST", "Market on create view")
        root = if (appDelegate.saveLanguageIs == "en") {
            inflater.inflate(R.layout.fragment_market, container, false)
        } else {
            inflater.inflate(R.layout.fragment_market_mirror, container, false)
        }

        watchlistTV = activity?.toolbar?.cancelIcon!!

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar!!.show()
                it.supportActionBar!!.setTitle(R.string.title_market)
            }
        }
        sharedPref = requireContext().getSharedPreferences("Borsa", Context.MODE_PRIVATE)
        editor= sharedPref.edit()

        smallBTN = root.findViewById(R.id.smallBTN)
        mediumBTN = root.findViewById(R.id.mediumBTN)
        largeBTN = root.findViewById(R.id.largeBTN)

        val widthInPixels = resources.displayMetrics.widthPixels
        widthIndp = widthInPixels/resources.displayMetrics.density
        Log.e("SIZE", "widthInPx = $widthInPixels and widthIndp = $widthIndp")

        onClickListeners()
//        (activity as? MainActivity)?.getSettings()

        progressDialog = CustomProgressView.initWith(requireContext())

        recyclerView = root.findViewById(R.id.recycleview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        adapter = MarketAdapter(this, requireContext(), filterMarketArrayList, 16, 150, 42,42)

//        val callback: ItemTouchHelper.Callback = ItemMarketMoveCallback(adapter)
//        val touchHelper = ItemTouchHelper(callback)
//        touchHelper.attachToRecyclerView(recyclerView)

        recyclerView.adapter = adapter

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefresh_items)

        mSwipeRefreshLayout.setOnRefreshListener {
            getMarketValues()

            if (appDelegate.userExpiredDate.isNotBlank() && appDelegate.userExpiredDate.isNotEmpty()) {
                if (appDelegate.after3DaysDate >= appDelegate.userExpiredDate) {
                    Log.e("Date","True")
                    try {
                        showMessage(getString(R.string.your_account_expiry) + " " + appDelegate.userExpiredDate)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Log.e("Date","False")
                }
            }
        }
        // For detecting drag & drop and swipe gesture.
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN
            , ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(filterMarketArrayList, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                storeMarketIdInSharedPref(filterMarketArrayList)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                adapter.notifyDataSetChanged()
                addToWatchlist(position)
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

    private fun addToWatchlist(position: Int) {
        if (Constants.isNetworkAvailable(requireContext())) {
            val marketId = filterMarketArrayList[position].id
            val userId = appDelegate.currentUserID
            val lang = appDelegate.saveLanguageIs
            Log.e("ID", "UserId- $userId, marketId- $marketId, Lang- $lang")

            progressDialog?.show()
            val apiRequest = RetrofitClient.getInstance().create(WatchlistApi::class.java)
            apiRequest.addToMarketWatchlist("addWatchlistCurrencies", userId, marketId, lang)
                .enqueue(object : Callback<WatchlistApi.NetworkAddDeleteWatchlist> {
                    override fun onResponse(
                        call: Call<WatchlistApi.NetworkAddDeleteWatchlist>,
                        response: Response<WatchlistApi.NetworkAddDeleteWatchlist>
                    ) {
                        progressDialog?.dismiss()
                        if (response.isSuccessful && response.body() != null) {
                            showMessage(response.body()!!.message.toString())
                            Log.e("NST_market", response.body()!!.message.toString())
                            //showMessage(resources.getString(R.string.something_went_wrong))
                        }
                    }
                    override fun onFailure(
                        call: Call<WatchlistApi.NetworkAddDeleteWatchlist>,
                        t: Throwable
                    ) {
                        progressDialog?.dismiss()
                        Log.e("NST_market", "Fail to add- ${t.message}")
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

    private fun storeMarketIdInSharedPref(filterList: java.util.ArrayList<Market>) {
        val listOfSortedMarketId: MutableList<String> = java.util.ArrayList()

        for (mMarket in filterList) {
            listOfSortedMarketId.add(mMarket.id)
        }
        //convert the List of Longs to a JSON string
        val gson = Gson()
        val jsonListOfSortedMarketIds = gson.toJson(listOfSortedMarketId)

        //save to SharedPreference
        val edit = Constants.sharedPreferences.edit()
        edit.putString(Constants.PREF_KEY_STORE_MARKETLIST, jsonListOfSortedMarketIds).apply()
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
        Log.e("NST", "Market on resume")

        fontSize = sharedPref.getString("MSize", "")!!
        Log.e("SIZE", fontSize)
        when (fontSize) {
            "Small" -> doChangesForSmall()
            "Medium" -> doChangesForMedium()
            "Large" -> doChangesForLarge()
            else -> doChangesForMedium()
        }
        getMarketValues()

        watchlistTV.visibility = View.VISIBLE
        watchlistTV.text = resources.getString(R.string.title_watchlist)

        watchlistTV.setOnClickListener {
            findNavController().navigate(R.id.marketWatchlistFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        watchlistTV.visibility = View.GONE
    }

    private fun getMarketValues() {
        if (mSwipeRefreshLayout.isRefreshing) {
            mSwipeRefreshLayout.isRefreshing = false
        }

        if (marketArrayList.isNotEmpty()) {
            marketArrayList.clear()
        }

        if (Constants.isNetworkAvailable(requireActivity())) {
            progressDialog?.show()

            val apiRequest = RetrofitClient.getInstance().create(MarketApi::class.java)
            apiRequest.getMarketValue("showCurrencies").enqueue(object : Callback<MarketApi.NetworkGetMarket> {
                override fun onFailure(call: Call<MarketApi.NetworkGetMarket>, t: Throwable) {
                    showMessage(t.message.toString())
                    progressDialog?.dismiss()
                }

                override fun onResponse(
                    call: Call<MarketApi.NetworkGetMarket>,
                    response: Response<MarketApi.NetworkGetMarket>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null && response.body()?.status == "true") {
                            if (response.body()!!.isLoginRequired != null) {
                                if (response.body()!!.isLoginRequired!! == "1") {
                                    AppDelegate.applicationContext().isMarketLoginRequire = SignInState.REQUIRE
                                } else {
                                    AppDelegate.applicationContext().isMarketLoginRequire = SignInState.NOTREQUIRE
                                }
                            } else {
                                AppDelegate.applicationContext().isMarketLoginRequire = SignInState.UNIDEFINE
                            }

                            val dataList = response.body()?.data ?: emptyList()
                            marketArrayList.addAll(dataList.mapNotNull { p0 -> Market.create(p0) })

                            when (AppDelegate.applicationContext().isMarketLoginRequire) {
                                SignInState.UNIDEFINE -> {
                                    checkLoginAndUpdateData()
                                }
                                SignInState.NOTREQUIRE -> {
                                    updateMarketData()
                                }
                                SignInState.REQUIRE -> {
                                    checkLoginAndUpdateData()
                                }
                            }
                        } else {
                            progressDialog?.dismiss()
                            //showMessage(response.errorBody().toString())
                        }
                    } else {
                        progressDialog?.dismiss()
                        showMessage(response.errorBody().toString())
                    }
                }
            })
        } else {
            try {
                val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                builder.setTitle(requireContext().getString(R.string.no_internet_available))
                builder.setPositiveButton(requireContext().getString(R.string.retry)) { dialog, which -> requireActivity().recreate() }
                builder.setNegativeButton(requireContext().getString(R.string.exit)) { dialog, which -> requireActivity().finishAffinity() }
                builder.setCancelable(false)
                builder.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun checkLoginAndUpdateData() {
        progressDialog?.dismiss()

        if (AppDelegate.applicationContext().isMarketLoggedIn) {
            updateMarketData()
        } else {
            moveToSignIn()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateMarketData() {
        val filterSharedPrefList = ArrayList<Market>()

        val jsonListForMarketId =
            Constants.sharedPreferences.getString(Constants.PREF_KEY_STORE_MARKETLIST, "")

        if (!jsonListForMarketId.isNullOrEmpty()) {
            val gson = Gson()

            val listOfSortedMarketId: List<String> =
                gson.fromJson(jsonListForMarketId, object : TypeToken<List<String?>?>() {}.type)

            //build filter list
            if (!listOfSortedMarketId.isNullOrEmpty()) {
                for (id in listOfSortedMarketId) {
                    for (market in marketArrayList) {
                        if (market.id == id) {
                            filterSharedPrefList.add(market)
                            marketArrayList.remove(market)
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

            if (marketArrayList.isNotEmpty()) {
                filterSharedPrefList.addAll(marketArrayList)
            }
            filterMarketArrayList.clear()
            filterMarketArrayList.addAll(filterSharedPrefList)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()
        }
        else {
            filterMarketArrayList.clear()
            filterMarketArrayList.addAll(marketArrayList)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()
        }
    }

    private fun moveToSignIn() {
        if (findNavController().currentDestination?.id == R.id.navigation_market) {
            val bundle = bundleOf("typeId" to "2")
            findNavController().navigate(R.id.action_navigation_market_to_fragment_login, bundle)
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


    override fun onStop() {
        super.onStop()

        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }


    //NST(Binjal) 15-7-20 For transactiontoolargeexception solve
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }
}