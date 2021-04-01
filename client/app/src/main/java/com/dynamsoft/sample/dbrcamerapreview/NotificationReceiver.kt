package com.dynamsoft.sample.dbrcamerapreview

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.*

class NotificationReceiver : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        println("NEW TOKEN: $token")
        val url = "http://173.34.40.62:5000/subscribe"
        val body = JSONObject(mapOf("id" to token))
        Volley.newRequestQueue(this).add(
                JsonObjectRequest(Request.Method.POST, url, body, {}, null))
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        remoteMessage.notification?.let {
            val builder = NotificationCompat.Builder(this, "CIS3760_1337")
                    .setContentTitle(it.title)
                    .setContentText(it.body)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(UUID.randomUUID().leastSignificantBits.toInt(), builder.build())
            }
        }
    }
}