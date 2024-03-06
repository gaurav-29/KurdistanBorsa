package net.comelite.kurdistanborsa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.fragment.ImageInterface
import net.comelite.kurdistanborsa.fragment.NotificationsFragment
import net.comelite.kurdistanborsa.model.Notification
import net.comelite.kurdistanborsa.utils.AppDelegate
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(val listner: ImageInterface,
                          val arrayList: ArrayList<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    val language = AppDelegate.applicationContext().saveLanguageIs
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val longDateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    private val toTimeFormatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        longDateTimeFormatter.timeZone = TimeZone.getTimeZone("Asia/Kuwait")
        dateFormatter.timeZone = TimeZone.getTimeZone("Asia/Kuwait")
        toTimeFormatter.timeZone = TimeZone.getTimeZone("Asia/Kuwait")
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_notification, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = arrayList[position]

        with(holder) {
            var finalDate = ""
            val dateTimeComponents = data.date.split(" ")
            if (dateTimeComponents.size == 2) {
                val dateStr = dateTimeComponents[0]
                val timeStr = dateTimeComponents[1]

                val longDateTimeString = "${dateStr}T$timeStr"
                val networkDateTime = longDateTimeFormatter.parse(longDateTimeString)

                if (networkDateTime != null) {
                    val nDateStr = dateFormatter.format(networkDateTime)
                    val nTimeStr = toTimeFormatter.format(networkDateTime)

                    finalDate = "$nDateStr $nTimeStr"
                }
            }

            when (language) {
                "en" -> {
                    title.text = data.engTitle
                    message.text = data.engMessage
                    date.text = finalDate
                    val option: RequestOptions = RequestOptions()
                        .placeholder(R.mipmap.ic_action_logo)
                        .error(R.mipmap.ic_action_logo)
                    Glide.with(listner as NotificationsFragment).load(data.notificationImg).apply(option).fitCenter().into(image)
                }
                "ku" -> {
                    titleRTL.text = data.kurTitle
                    messageRTL.text = data.kurMessage
                    dateRTL.text = finalDate
                    val option: RequestOptions = RequestOptions()
                        .placeholder(R.mipmap.ic_action_logo)
                        .error(R.mipmap.ic_action_logo)
                    Glide.with(listner as NotificationsFragment).load(data.notificationImg).apply(option).fitCenter().into(imageRTL)
                }
                "ar" -> {
                    titleRTL.text = data.arTitle
                    messageRTL.text = data.arMessage
                    dateRTL.text = finalDate
                    val option: RequestOptions = RequestOptions()
                        .placeholder(R.mipmap.ic_action_logo)
                        .error(R.mipmap.ic_action_logo)
                    Glide.with(listner as NotificationsFragment).load(data.notificationImg).apply(option).fitCenter().into(imageRTL)
                }
                else -> {
                    title.text = data.engTitle
                    message.text = data.engMessage
                    date.text = finalDate
                    val option: RequestOptions = RequestOptions()
                        .placeholder(R.mipmap.ic_action_logo)
                        .error(R.mipmap.ic_action_logo)
                    Glide.with(listner as NotificationsFragment).load(data.notificationImg).apply(option).fitCenter().into(image)
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.tvTitle)
        var message: TextView = itemView.findViewById(R.id.tvMessage)
        var date: TextView = itemView.findViewById(R.id.tvDate)
        var image: ImageView = itemView.findViewById(R.id.ivImg)

        var titleRTL: TextView = itemView.findViewById(R.id.tvTitleRTL)
        var messageRTL: TextView = itemView.findViewById(R.id.tvMessageRTL)
        var dateRTL: TextView = itemView.findViewById(R.id.tvDateRTL)
        var imageRTL: ImageView = itemView.findViewById(R.id.ivImgRTL)

        init {
            if (language == "en") {
                title.visibility = View.VISIBLE
                message.visibility = View.VISIBLE
                date.visibility = View.VISIBLE
                image.visibility = View.VISIBLE

                titleRTL.visibility = View.GONE
                messageRTL.visibility = View.GONE
                dateRTL.visibility = View.GONE
                imageRTL.visibility = View.GONE
            } else {
                title.visibility = View.GONE
                message.visibility = View.GONE
                date.visibility = View.GONE
                image.visibility = View.GONE

                titleRTL.visibility = View.VISIBLE
                messageRTL.visibility = View.VISIBLE
                dateRTL.visibility = View.VISIBLE
                imageRTL.visibility = View.VISIBLE
            }

            image.setOnClickListener {
                listner.popUpImage(adapterPosition)
            }

            imageRTL.setOnClickListener {
                listner.popUpImage(adapterPosition)
            }

        }
    }
}