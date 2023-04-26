package com.demo.autosherpa3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import activity.CallTriggerActivity
import entity.CallTriggerModel
import util.CommonSettings


class MyFirebaseMessagingService : FirebaseMessagingService() {



    private val TAG = "MyFirebaseMsgService"
    private lateinit var callTriggerModel: CallTriggerModel
    private lateinit var receivedphonenumber: String
    private lateinit var strqueuetype: String
    private lateinit var strpsfid: String
    private lateinit var strmakecallfrom: String
    private lateinit var strreceivedcusname: String
    private lateinit var strVehicleNumber: String
    private lateinit var vib_rate: Vibrator
    private lateinit var settings: CommonSettings
    private lateinit var dateString: String
    private lateinit var timeString: String
    private lateinit var context: Context
    private var targetSdkVersion: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        callTriggerModel = CallTriggerModel()

        settings = (application as WyzConnectApp).settings
        super.onMessageReceived(remoteMessage)

        if (!settings.isDisabeUser() && (settings.userRole.equals("CRE", ignoreCase = true) || settings.userRole.equals("Cre", ignoreCase = true) || settings.userRole.equals("cre", ignoreCase = true))) {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()

            vib_rate = getSystemService(VIBRATOR_SERVICE) as Vibrator

            vib_rate.vibrate(2000)

            Log.d(TAG, "From: " + remoteMessage.from!!)
            //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Notification Message command: " + remoteMessage.data["command"])

            Log.d(TAG, "Notification Message Phone Number: " + remoteMessage.data["phonenumber"]!!)

            receivedphonenumber = remoteMessage.data["phonenumber"]!!

            strqueuetype = remoteMessage.data["type"]!!

            strpsfid = remoteMessage.data["id"]!!

            strmakecallfrom = remoteMessage.data["makeCallFrom"]!!

            strreceivedcusname = remoteMessage.data["customername"]!!

            strVehicleNumber = remoteMessage.data["vehicleRegNo"]!!

            if (receivedPhoneNumber.isNullOrEmpty()) {
                Toast.makeText(applicationContext, "Phone Number is Empty", Toast.LENGTH_SHORT).show()
            } else {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                with(sharedPreferences.edit()) {
                    putString("queueType", strqueuetype)
                    putString("webmakecallfrom", strmakecallfrom)
                    putString("recordid", strpsfid)
                    putString("customername", strreceivedcusname)
                    putString("vehicleRegNo", strVehicleNumber)
                    apply()
                }
                settings.setUniqueidForCallSync(strpsfid)
                settings.setVehicleRegNo(strVehicleNumber)

                Log.d(TAG, "recordId: $strpsfid")

                if (Build.VERSION.SDK_INT < 23) {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.data = Uri.parse("tel:$receivedPhoneNumber")
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(intent)
                    }
                } else {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.data = Uri.parse("tel:$receivedPhoneNumber")
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    } else {
                        settings.setUserPhoneNumber(receivedPhoneNumber)
                        if (settings.isCheckForeground()) {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.data = Uri.parse("tel:$receivedPhoneNumber")
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        } else {
                            val channel = NotificationChannel("channel01", "name", NotificationManager.IMPORTANCE_HIGH)
                            channel.description = "description"
                            val intent = Intent(this, CallTriggerActivity::class.java).apply {
                                putExtra("phoneNumber", receivedPhoneNumber)
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
                            val notificationManager = getSystemService(NotificationManager::class.java)
                            notificationManager.createNotificationChannel(channel)
                            val notification = NotificationCompat.Builder(this, "channel01")
                                .setSmallIcon(R.mipmap.new_app_icon)
                                .setContentTitle("Click Here To Start Call")
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .build()
                            NotificationManagerCompat.from(this).notify(0, notification)
                        }
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    if (settings.getUserRole().equals("CRE", ignoreCase = true)) {
                        Toast.makeText(applicationContext, R.string.calltrigger, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onNewToken(token: String) {
                super.onNewToken(token)
                Log.e("New Token", token)
            }
        }
    }
}



















            }