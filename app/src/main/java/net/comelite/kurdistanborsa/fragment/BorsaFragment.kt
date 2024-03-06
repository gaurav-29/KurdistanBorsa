package net.comelite.kurdistanborsa.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
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
import kotlinx.android.synthetic.main.fragment_borsa.*
import kotlinx.android.synthetic.main.fragment_borsa.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_main.view.*
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.adapter.BorsaAdapter
//import net.comelite.kurdistanborsa.adapter.ItemBorsaMoveCallback
import net.comelite.kurdistanborsa.api.BorsaApi
import net.comelite.kurdistanborsa.api.WatchlistApi
import net.comelite.kurdistanborsa.custom.CustomProgressView
import net.comelite.kurdistanborsa.model.Borsa
import net.comelite.kurdistanborsa.model.SignInState
import net.comelite.kurdistanborsa.retrofit.RetrofitClient
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class BorsaFragment : Fragment() {

    private val appDelegate = AppDelegate.applicationContext()

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: BorsaAdapter
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    var borsaArrayList = ArrayList<Borsa>()
    var filterBorsaArrayList = ArrayList<Borsa>()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("NST", "Borsa on create view")
        /**
         * NST(Binjal) 26-6-20
         * Change LTR to RTL Programmatically
         * XML in some direction not work
         * So create same another layout type same as mirror
         */

        root = if (appDelegate.saveLanguageIs == "en") {
            inflater.inflate(R.layout.fragment_borsa, container, false)
        } else {
            inflater.inflate(R.layout.fragment_borsa_mirror, container, false)
        }

        watchlistTV = activity?.toolbar?.cancelIcon!!

        activity?.let {
            if (it is AppCompatActivity) {
                it.supportActionBar?.show()
                it.supportActionBar?.setTitle(R.string.title_borsa)
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

        val density = resources.displayMetrics.density
        val dpToPixel = (widthIndp * density).roundToInt()
        Log.e("SIZE", "dpToPx = $dpToPixel, density=$density")

        onClickListeners()
        //        (activity as? MainActivity)?.getSettings()

        progressDialog = CustomProgressView.initWith(requireContext())

        recyclerView = root.findViewById(R.id.recycleview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        adapter = BorsaAdapter(this, filterBorsaArrayList, 15, 150)

//        val callback: ItemTouchHelper.Callback = ItemBorsaMoveCallback(adapter)
//        val touchHelper = ItemTouchHelper(callback)
//        touchHelper.attachToRecyclerView(recyclerView)

        recyclerView.adapter = adapter

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefresh_items)

        mSwipeRefreshLayout.setOnRefreshListener {
            getBorsaValue()

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
                , ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(filterBorsaArrayList, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                storeBorsaIdInSharedPref(filterBorsaArrayList)
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
    private fun storeBorsaIdInSharedPref(filterList: java.util.ArrayList<Borsa>) {
        val listOfSortedBorsaId: MutableList<String> = java.util.ArrayList()

        for (mBorsa in filterList) {
            listOfSortedBorsaId.add(mBorsa.id)
        }
        //convert the List of Longs to a JSON string
        val gson = Gson()
        val jsonListOfSortedBorsaIds = gson.toJson(listOfSortedBorsaId)

        //save to SharedPreference
        val edit = Constants.sharedPreferences.edit()
        edit.putString(Constants.PREF_KEY_STORE_BORSALIST, jsonListOfSortedBorsaIds).apply()
    }

    private fun addToWatchlist(position: Int) {

        if (Constants.isNetworkAvailable(requireContext())) {
            val borsaId = filterBorsaArrayList[position].id
            val userId = appDelegate.currentUserID
            val lang = appDelegate.saveLanguageIs
            Log.e("ID", "UserId- $userId, BorsaId- $borsaId, Lang- $lang")

            progressDialog?.show()
            val apiRequest = RetrofitClient.getInstance().create(WatchlistApi::class.java)
            apiRequest.addToBorsaWatchlist("addWatchlistMarkets", userId, borsaId, lang)
                .enqueue(object : Callback<WatchlistApi.NetworkAddDeleteWatchlist> {
                    override fun onResponse(
                        call: Call<WatchlistApi.NetworkAddDeleteWatchlist>,
                        response: Response<WatchlistApi.NetworkAddDeleteWatchlist>
                    ) {
                        progressDialog?.dismiss()
                        if (response.isSuccessful && response.body() != null) {
                            showMessage(response.body()!!.message.toString())
                            Log.e("NST_Borsa", response.body()!!.message.toString())
                            //showMessage(resources.getString(R.string.something_went_wrong))
                        }
                    }
                    override fun onFailure(
                        call: Call<WatchlistApi.NetworkAddDeleteWatchlist>,
                        t: Throwable
                    ) {
                        progressDialog?.dismiss()
                        Log.e("NST_Borsa", "Fail to add- ${t.message}")
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
        Log.e("NST", "Borsa on resume")

        fontSize = sharedPref.getString("Size", "")!!
        Log.e("SIZE", fontSize)
        when (fontSize) {
            "Small" -> doChangesForSmall()
            "Medium" -> doChangesForMedium()
            "Large" -> doChangesForLarge()
            else -> doChangesForMedium()
        }
        getBorsaValue()

        watchlistTV.visibility = View.VISIBLE
        watchlistTV.text = resources.getString(R.string.title_watchlist)

        watchlistTV.setOnClickListener {
            findNavController().navigate(R.id.watchlistFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        watchlistTV.visibility = View.GONE
    }

    private fun getBorsaValue() {

        if (mSwipeRefreshLayout.isRefreshing) {
            mSwipeRefreshLayout.isRefreshing = false
        }

        if (borsaArrayList.isNotEmpty()) {
            borsaArrayList.clear()
        }

        if (Constants.isNetworkAvailable(requireActivity())) {
            progressDialog?.show()
            val apiRequest = RetrofitClient.getInstance().create(BorsaApi::class.java)
            apiRequest.getBorsaValue(func = "displayMarkets")
                .enqueue(object : Callback<BorsaApi.NetworkGetBorsa> {
                    override fun onFailure(call: Call<BorsaApi.NetworkGetBorsa>, t: Throwable) {
                        Log.e("NST_Borsa", "Get Borsa Value Failed ${t.message}")
                        progressDialog?.dismiss()
                        showMessage(t.message.toString())
                    }

                    override fun onResponse(
                        call: Call<BorsaApi.NetworkGetBorsa>,
                        response: Response<BorsaApi.NetworkGetBorsa>
                    ) {
                        //Log.e("NST_Borsa1","Get Borsa OnResponse Success ${response}")

                        if (response.isSuccessful) {
                            if (response.body() != null && response.body()?.status == "true") {
                                if (response.body()!!.isBorsaLoginRequired != null) {
                                    if (response.body()!!.isBorsaLoginRequired!! == "1") {
                                        AppDelegate.applicationContext().isBorsaLoginRequire =
                                            SignInState.REQUIRE
                                    } else {
                                        AppDelegate.applicationContext().isBorsaLoginRequire =
                                            SignInState.NOTREQUIRE
                                    }
                                } else {
                                    AppDelegate.applicationContext().isBorsaLoginRequire =
                                        SignInState.UNIDEFINE
                                }

                                val dataList = response.body()!!.data ?: emptyList()
                                borsaArrayList.addAll(dataList.mapNotNull { p0 -> Borsa.create(p0) })

                                when (AppDelegate.applicationContext().isBorsaLoginRequire) {
                                    SignInState.UNIDEFINE -> {
                                        checkLoginAndUpdateData()
                                    }
                                    SignInState.NOTREQUIRE -> {
                                        updateBorsaData()
                                    }
                                    SignInState.REQUIRE -> {
                                        checkLoginAndUpdateData()
                                    }
                                }
                            }
                        } else {
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

    private fun checkLoginAndUpdateData() {
        progressDialog?.dismiss()

        if (AppDelegate.applicationContext().isBorsaLoggedIn) {
            updateBorsaData()
        } else {
            moveToSignIn()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateBorsaData() {
        val filterSharedPrefList = ArrayList<Borsa>()

        val jsonListForBorsaId =
            Constants.sharedPreferences.getString(Constants.PREF_KEY_STORE_BORSALIST, "")

        if (!jsonListForBorsaId.isNullOrEmpty()) {
            val gson = Gson()

            val listOfSortedBorasId: List<String> =
                gson.fromJson(jsonListForBorsaId, object : TypeToken<List<String?>?>() {}.type)

            //build filter list
            if (!listOfSortedBorasId.isNullOrEmpty()) {
                for (id in listOfSortedBorasId) {
                    for (borsa in borsaArrayList) {
                        if (borsa.id == id) {
                            filterSharedPrefList.add(borsa)
                            borsaArrayList.remove(borsa)
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

            if (borsaArrayList.isNotEmpty()) {
                filterSharedPrefList.addAll(borsaArrayList)
            }
            filterBorsaArrayList.clear()
            filterBorsaArrayList.addAll(filterSharedPrefList)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()

        } else {
            filterBorsaArrayList.clear()
            filterBorsaArrayList.addAll(borsaArrayList)
            progressDialog?.dismiss()
            adapter.notifyDataSetChanged()
        }
    }

    private fun moveToSignIn() {
        if (findNavController().currentDestination?.id == R.id.navigation_borsa) {
            val bundle = bundleOf("typeId" to "1")
            findNavController().navigate(R.id.action_navigation_borsa_to_fragment_login, bundle)
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
            progressDialog!!.dismiss()
        }
    }
}
