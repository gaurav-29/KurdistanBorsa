package net.comelite.kurdistanborsa.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.fragment.BorsaWatchlistFragment
import net.comelite.kurdistanborsa.model.Borsa
import net.comelite.kurdistanborsa.utils.AppDelegate
import java.util.ArrayList

class BorsaWatchlistAdapter(private val fragment: BorsaWatchlistFragment, private val watchList: ArrayList<Borsa>,
                            private var fontSize: Int, private var textViewWidth: Int) :
    RecyclerView.Adapter<BorsaWatchlistAdapter.ViewHolder>() {

    val language = AppDelegate.applicationContext().saveLanguageIs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var v: View? = null
        v = if (language == "en") {
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_borsa_watchlist, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_borsa_watchlist_mirror, parent, false)
        }
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = watchList[position]
        holder.let {

            when (language) {
                "en" -> {
                    if (data.isOpen == 0) {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_closed_lock, 0)
                    }
                    else {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.title.trim()
                    it.name.textSize = fontSize.toFloat()
                }
                "ku" -> {
                    if (data.isOpen == 0) {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_closed_lock, 0, 0, 0)
                    }
                    else {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.titleKurdish.trim()
                    it.name.textSize = fontSize.toFloat()
                }
                "ar" -> {
                    if (data.isOpen == 0) {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_closed_lock, 0, 0, 0)
                    }
                    else {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.titleArabic.trim()
                    it.name.textSize = fontSize.toFloat()
                }
                else -> {
                    if (data.isOpen == 0) {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_closed_lock, 0)
                    }
                    else {
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.title.trim()
                    it.name.textSize = fontSize.toFloat()
                }
            }

            it.btnAsk.text = data.ask
            it.btnAsk.textSize = fontSize.toFloat()
            it.btnAsk.width = textViewWidth

            it.btnBid.text = data.bid
            it.btnBid.textSize = fontSize.toFloat()
            it.btnBid.width = textViewWidth

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

    override fun getItemCount(): Int {
        return watchList.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var btnAsk: TextView = itemView.findViewById(R.id.btnAsk)
        var btnBid: TextView = itemView.findViewById(R.id.btnBid)

        init {
            btnAsk.gravity = Gravity.CENTER
            btnBid.gravity = Gravity.CENTER
        }
    }
}