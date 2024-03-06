package net.comelite.kurdistanborsa.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Binjal 22-6-20
 * Firebase Messaging Service
 * check for the data/notification entry from the payload
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if (p0.notification != null) {
            val title = p0.notification!!.title
            val body = p0.notification!!.body

            NotificationHelper.displayNotification(applicationContext, title!!, body!!)
        }
    }
}


