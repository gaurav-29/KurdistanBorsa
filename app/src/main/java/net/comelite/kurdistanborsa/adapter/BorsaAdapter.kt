package net.comelite.kurdistanborsa.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.fragment.BorsaFragment
import net.comelite.kurdistanborsa.model.Borsa
import net.comelite.kurdistanborsa.utils.AppDelegate
import java.util.*

/**
 * NST(Binjal) 19-6-20
 * For up-down item
 */
class BorsaAdapter(private val fragment: BorsaFragment, private val borsaList: ArrayList<Borsa>,
                   private var fontSize: Int, private var textViewWidth: Int) :
    RecyclerView.Adapter<BorsaAdapter.ViewHolder>() {
    val language = AppDelegate.applicationContext().saveLanguageIs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var v: View? = null
        v = if (language == "en") {
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_borsa, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_borsa_mirror, parent, false)
        }
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return borsaList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = borsaList[position]
        holder.let {

            when (language) {
                "en" -> {
//                    var finalName = data.title.trim()
//                    if (finalName.contains("closed")) {
//                        //it.openLock.visibility = View.GONE
//                        it.closedLock.visibility = View.VISIBLE
//                        finalName = finalName.replace("closed","")
//                    } else {
//                        it.closedLock.visibility = View.GONE
//                    }
                    if (data.isOpen == 0) {
                        // it.closedLock.visibility = View.VISIBLE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_action_closed_lock,
                            0
                        )
                    } else {
                        // it.closedLock.visibility = View.GONE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.title.trim()
                    it.name.textSize = fontSize.toFloat()
                }
                "ku" -> {
//                    var finalName = data.titleKurdish.trim()
//                    //Log.e("FN", "$finalName")
//                    if (finalName.contains("داخراوە")) {
//                        //it.openLock.visibility = View.GONE
//                       // it.closedLock.visibility = View.VISIBLE
//                        finalName = finalName.replace("داخراوە","")
//                    } else {
//                       // it.closedLock.visibility = View.GONE
//                    }
                    if (data.isOpen == 0) {
                        // it.closedLock.visibility = View.VISIBLE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_action_closed_lock,
                            0,
                            0,
                            0
                        )
                    } else {
                        // it.closedLock.visibility = View.GONE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.titleKurdish.trim()
                    it.name.textSize = fontSize.toFloat()
                }
                "ar" -> {
//                    var finalName = data.titleArabic.trim()
//                    if (finalName.contains("مغلق")) {
//                        //it.openLock.visibility = View.GONE
//                       // it.closedLock.visibility = View.VISIBLE
//                        finalName = finalName.replace("مغلق","")
//                    } else {
//                       // it.closedLock.visibility = View.GONE
//                    }
                    if (data.isOpen == 0) {
                        // it.closedLock.visibility = View.VISIBLE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_action_closed_lock,
                            0,
                            0,
                            0
                        )
                    } else {
                        // it.closedLock.visibility = View.GONE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.titleArabic.trim()
                    it.name.textSize = fontSize.toFloat()
                }
                else -> {
//                    var finalName = data.title.trim()
//                    if (finalName.contains("closed")) {
//                        //it.openLock.visibility = View.GONE
//                      //  it.closedLock.visibility = View.VISIBLE
//                        finalName = finalName.replace("closed","")
//                    } else {
//                      //  it.closedLock.visibility = View.GONE
//                    }
                    if (data.isOpen == 0) {
                        // it.closedLock.visibility = View.VISIBLE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_action_closed_lock,
                            0
                        )
                    } else {
                        // it.closedLock.visibility = View.GONE
                        it.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    it.name.text = data.title.trim()
                    it.name.textSize = fontSize.toFloat()
                }
            }

            it.btnAsk.text = data.ask
            it.btnAsk.textSize = fontSize.toFloat()
            it.btnAsk.width = textViewWidth
            //it.btnAsk.height = textViewHeight

//            it.btnAsk.text = "123456789"
//            it.btnAsk.textSize = fontSize.toFloat()
            it.btnBid.text = data.bid
            it.btnBid.textSize = fontSize.toFloat()
            it.btnBid.width = textViewWidth
            // it.btnAsk.height = textViewHeight

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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById<TextView>(R.id.name)
        var btnAsk: TextView = itemView.findViewById<TextView>(R.id.btnAsk)
        var btnBid: TextView = itemView.findViewById<TextView>(R.id.btnBid)
        //var openLock = itemView.findViewById<ImageView>(R.id.ivLockOpen)
        // var closedLock: ImageView = itemView.findViewById<ImageView>(R.id.ivLockClosed)

        init {
            btnAsk.gravity = Gravity.CENTER
            btnBid.gravity = Gravity.CENTER
        }
    }
}
