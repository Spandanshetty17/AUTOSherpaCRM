package activity

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import entity.CallTriggerModel
import util.CommonSettings

import entity.CallTriggerModel

class MyFirebaseMessagingService : FirebaseMessagingService() {
    var callTriggerModel: CallTriggerModel? = null

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    var receivedphonenumber: String? = null
    var strqueuetype: String? = null
    var strpsfid: String? = null
    var strmakecallfrom: String? = null
    var strreceivedcusname: String? = null
    var strVehicleNumber: String? = null
    var vib_rate: Vibrator? = null
    private var settings: CommonSettings? = null
    var dateString: String? = null
    var timeString: String? = null
    var context: Context? = null
    var targetSdkVersion = 0

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        callTriggerModel = CallTriggerModel()
        settings = (application as WyzConnectApp).getSettings()
        super.onMessageReceived(remoteMessage)
        if (!settings.isDisabeUser && settings.getUserRole()
                .equals("CRE") || settings.getUserRole().equals("Cre") || settings.getUserRole()
                .equals("cre")
        ) {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
            vib_rate = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vib_rate!!.vibrate(2000)
            Log.d(TAG, "From: " + remoteMessage.from)
            //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Notification Message command: " + remoteMessage.data["command"])
            Log.d(TAG, "Notification Message Phone Number: " + remoteMessage.data["phonenumber"])
            receivedphonenumber = remoteMessage.data["phonenumber"]
            strqueuetype = remoteMessage.data["type"]
            strpsfid = remoteMessage.data["id"]
            strmakecallfrom = remoteMessage.data["makeCallFrom"]
            strreceivedcusname = remoteMessage.data["customername"]
            strVehicleNumber = remoteMessage.data["vehicleRegNo"]
            if (receivedphonenumber!!.isEmpty() || receivedphonenumber == "" || receivedphonenumber == null) {
                Toast.makeText(applicationContext, "Phone Number is Empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPreferences.edit().putString("queueType", strqueuetype).apply()
                sharedPreferences.edit().putString("webmakecallfrom", strmakecallfrom).apply()
                sharedPreferences.edit().putString("recordid", strpsfid).apply()
                sharedPreferences.edit().putString("customername", strreceivedcusname).apply()
                sharedPreferences.edit().putString("vehicleRegNo", strVehicleNumber).apply()
                settings.uniqueidForCallSync = strpsfid
                settings.vehicleRegNo = strVehicleNumber
                Log.d(TAG, "recordId: $strpsfid")
                if (Build.VERSION.SDK_INT < 23) {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.data = Uri.parse("tel:$receivedphonenumber")
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    startActivity(intent)
                } else {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.data = Uri.parse("tel:$receivedphonenumber")
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    } else {
                        settings.userPhoneNumber = receivedphonenumber
                        if (settings.isCheckForeground) {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.data = Uri.parse("tel:$receivedphonenumber")
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        } else {
                            val channel = NotificationChannel(
                                "channel01", "name",
                                NotificationManager.IMPORTANCE_HIGH
                            ) // for heads-up notifications
                            channel.description = "description"
                            val intent = Intent(this, CallTriggerActivity::class.java)
                            intent.putExtra("phoneNumber", receivedphonenumber)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            var pendingIntent: PendingIntent? = null
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                pendingIntent = PendingIntent.getActivity(
                                    this,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_MUTABLE
                                )
                            }
                            val notificationManager = getSystemService(
                                NotificationManager::class.java
                            )
                            notificationManager.createNotificationChannel(channel)
                            val notification1: Notification =
                                NotificationCompat.Builder(this, "channel01")
                                    .setSmallIcon(R.mipmap.new_app_icon)
                                    .setContentTitle("Click Here To Start Call")
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .build()
                            val notificationManager1 = NotificationManagerCompat.from(this)
                            notificationManager1.notify(0, notification1)
                        }
                    }
                }
            }
        } else {
            val h = Handler(Looper.getMainLooper())
            h.post {
                /*Toast.makeText(getApplicationContext(), "User is disabled. Call trigger will not work", Toast.LENGTH_SHORT).show();*/if (settings.getUserRole()
                    .equals("CRE") || settings.getUserRole().equals("Cre") || settings.getUserRole()
                    .equals("cre")
            ) {
                Toast.makeText(applicationContext, R.string.calltrigger, Toast.LENGTH_SHORT)
                    .show()
            }
            }
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e("New Token", s)
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val REQUEST_CALL = 1
    }
}