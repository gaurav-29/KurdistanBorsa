package net.comelite.kurdistanborsa.adapter

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.fragment.BorsaFragment
import net.comelite.kurdistanborsa.fragment.MarketFragment
import net.comelite.kurdistanborsa.model.Borsa
import net.comelite.kurdistanborsa.model.Market
import net.comelite.kurdistanborsa.utils.AppDelegate
import net.comelite.kurdistanborsa.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

class MarketAdapter(private val fragment: MarketFragment, val mContext: Context, private val arrayList: ArrayList<Market>,
                    private var fontSize: Int, private var textViewWidth: Int, private var ivWidth: Int, private var ivHeight: Int) :
    RecyclerView.Adapter<MarketAdapter.ViewHolder>(){

    val language = AppDelegate.applicationContext().saveLanguageIs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var v: View? = null
        v = if (language == "en") {
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_market, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_market_mirror, parent, false)
        }

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = arrayList[position]
        holder.let {
            val fromFlag = data.fromFlag
            val toFlag = data.toFlag

            val assetManager: AssetManager = mContext.assets

            try {
                val inputStream = assetManager.open(fromFlag)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                it.fromFlag.setImageBitmap(bitmap)

            } catch (e: Exception) {
                Log.e("NST_Excep_From", e.localizedMessage ?: "Unknown error in MarketAdapter - 1")
            }

            try {
                val inputStream = assetManager.open(toFlag)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                it.toFlag.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("NST_Excep_To", e.localizedMessage ?: "Unknown error in MarketAdapter - 2")
            }

            it.fromCode.text = data.fromCurrencyCode
            it.toCode.text = data.toCurrencyCode
            it.btnAsk.text = data.ask
            it.btnBid.text = data.bid

            it.btnAsk.textSize = fontSize.toFloat()
            it.btnAsk.width = textViewWidth

            it.btnBid.textSize = fontSize.toFloat()
            it.btnBid.width = textViewWidth

            it.fromFlag.layoutParams.height = ivHeight
            it.toFlag.layoutParams.height = ivHeight
            it.fromFlag.layoutParams.width = ivWidth
            it.toFlag.layoutParams.width = ivWidth

            it.fromCode.textSize = fontSize.toFloat()
            it.toCode.textSize = fontSize.toFloat()

            val askArrow = data.askArrow
            val bidArrow = data.bidArrow

            if (askArrow == 0) {
                it.btnAsk.setBackgroundResource(R.drawable.button_background_red)
            } else {
                it.btnAsk.setBackgroundResource(R.drawable.button_background_green)
            }

            if (bidArrow == 0) {
                it.btnBid.setBackgroundResource(R.drawable.button_background_red)
            } else {
                it.btnBid.setBackgroundResource(R.drawable.button_background_green)
            }
        }
    }

    fun setFontSize(fontSize: Int) {
        this.fontSize = fontSize
        notifyDataSetChanged()
    }

    fun setTextViewWidth(textViewWidth: Int) {
        this.textViewWidth = textViewWidth
        notifyDataSetChanged()
    }

    fun setImageViewWidthAndHeight(width: Int, height: Int) {
        this.ivWidth = width
        this.ivHeight = height
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fromFlag: ImageView = itemView.findViewById<ImageView>(R.id.ivFromFlag)
        var toFlag: ImageView = itemView.findViewById<ImageView>(R.id.ivToFlag)
        var fromCode: TextView = itemView.findViewById<TextView>(R.id.tvFromCode)
        var toCode: TextView = itemView.findViewById<TextView>(R.id.tvToCode)
        var btnAsk: TextView = itemView.findViewById<TextView>(R.id.btnAsk)
        var btnBid: TextView = itemView.findViewById<TextView>(R.id.btnBid)

        init {
            btnAsk.gravity = Gravity.CENTER
            btnBid.gravity = Gravity.CENTER
        }
    }
}

//load Image in asset Foldeer
//https://stackoverflow.com/questions/11734803/load-an-image-from-assets-folder

