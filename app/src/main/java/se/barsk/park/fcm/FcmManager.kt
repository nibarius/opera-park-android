package se.barsk.park.fcm

import android.os.Build
import com.google.firebase.iid.FirebaseInstanceId
import se.barsk.park.ParkApp
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.text.format.Time
import se.barsk.park.R
import se.barsk.park.mainui.ParkActivity


/**
 * Created by niklas on 2017-11-25.
 */
class FcmManager(val context: Context) {
    /**
     * Get the current FCM token. This method returns null if the token has not yet been generated.
     */
    fun getToken(): String? = FirebaseInstanceId.getInstance().token

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ParkingFreeNotificationChannel(context).create()
        }
    }

    fun makeNotification(title: String, text: String) {

        val mBuilder = NotificationCompat.Builder(context, ParkingFreeNotificationChannel.ID)
                .setSmallIcon(R.mipmap.notif_icon)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setTimeoutAfter(3*60*60*1000)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, ParkActivity::class.java)

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ParkActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0, mBuilder.build())
    }
}