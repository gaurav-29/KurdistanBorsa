package net.comelite.kurdistanborsa.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import net.comelite.kurdistanborsa.MainActivity
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.utils.Constants


/**
 * Binjal 22-6-20
 * For Display Notification
 */
object NotificationHelper {

    fun displayNotification(context: Context, title: String, body: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //setUpChannelNotification(mNotificationMgr)
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, importance)
            mChannel.description = Constants.CHANNEL_DESC
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mNotificationManager!!.createNotificationChannel(mChannel)
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap

        //val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round)

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val mBuilder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            //.setLargeIcon(largeIcon)
            .setSmallIcon(R.drawable.ic_notification_icon)

            .setContentTitle(title)
            .setContentText(body)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val mNotificationMgr = NotificationManagerCompat.from(context)
        mNotificationMgr.notify(1, mBuilder.build())

    }

    private fun setUpChannelNotification(manager: NotificationManager) {
        val adminName = "New Notification"
        val adminDescription = "Device to device notification"

        val adminchannel : NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            adminchannel = NotificationChannel(Constants.CHANNEL_ID, adminName, NotificationManager.IMPORTANCE_HIGH)

            adminchannel.description = adminDescription

            adminchannel.enableLights(true)
            adminchannel.lightColor = Color.RED
            adminchannel.enableVibration(true)


            manager.createNotificationChannel(adminchannel)
        }
    }

}

//Notification
//https://www.simplifiedcoding.net/firebase-cloud-messaging-tutorial-android/