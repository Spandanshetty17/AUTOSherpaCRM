package receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import activity.WyzConnectApp
import com.google.firebase.database.*
import entity.CallInfo
import service.RecordService
import util.CallHistory
import util.CommonSettings

/**
 * Created By 1524 on 10/1/2020
 */
class CallBroadCastReceiver : BroadcastReceiver() {
    protected var callInfo: CallInfo? = null
    var start_time: Long = 0
    var end_time: Long = 0
    var settings: CommonSettings? = null
    var lm: LocationManager? = null
    var gps_enabled = false
    var strphoneNumber: String? = null
    var strRecordingstate: String? = null
    override fun onReceive(context: Context, intent: Intent) {
        settings = (context.applicationContext as WyzConnectApp).getSettings()
        callInfo = (context.applicationContext as WyzConnectApp).getCallInfoCache()
        if (settings.isAuthenticated()) {
            Log.d(
                "CallBroadCastReceiver",
                "CallBroadcastReceiver::onReceive got Intent: $intent"
            )
            if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
                Log.d(TAG, "Out going call intent received")
                lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                try {
                    gps_enabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                if (!gps_enabled) {
                    Toast.makeText(
                        context,
                        "Please turn on the Location services!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                strphoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                if (strphoneNumber!!.contains("+91")) {
                    Log.d(
                        TAG,
                        "Mobileno$strphoneNumber"
                    )
                    settings.userMobileNo = strphoneNumber
                } else {
                    strphoneNumber = "+91$strphoneNumber"
                    settings.userMobileNo = strphoneNumber
                }
                if (callInfo == null) {
                    searchPhoneNo(context)
                }
                val handler = Handler()
                handler.postDelayed({

                    // yourMethod();
                    val observer =
                        CallLogContentObserver(Handler())
                    observer.setContext(context)
                    context.contentResolver.registerContentObserver(
                        CallLog.Calls.CONTENT_URI,
                        true,
                        observer
                    )
                }, 5000) //5 seconds
                Log.d(TAG, "outgoingstate ContentObserver is Intialized")
            } else if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                Log.d(TAG, "Phone state changed intent received")
                if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
                    Log.d(TAG, "The Phone is ringing")
                    strphoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    //Toast.makeText(context, "Call from:" + strphoneNumber, Toast.LENGTH_LONG).show();
                    start_time = System.currentTimeMillis()
                    Log.d(TAG, "start_time$start_time")
                    settings.userMobileNo = strphoneNumber
                    if (callInfo == null) {
                        searchPhoneNo(context)
                    }
                    lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    try {
                        gps_enabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    if (!gps_enabled) {
                        Toast.makeText(
                            context,
                            "Please turn on the Location services!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    (context.applicationContext as WyzConnectApp).setCallInfoCache(null)
                    Log.d(TAG, "Incoming call callinfocache is null")
                    if (lastStatusReceived != TelephonyManager.EXTRA_STATE_RINGING) {
                        val handler = Handler()
                        handler.postDelayed({
                            val observer =
                                CallLogContentObserver(Handler())
                            observer.setContext(context)
                            context.contentResolver.registerContentObserver(
                                CallLog.Calls.CONTENT_URI,
                                true,
                                observer
                            )
                        }, 5000) //5 seconds
                        Log.d(TAG, "Ringing state ContentObserver is Intialized")
                    }
                    lastStatusReceived = TelephonyManager.EXTRA_STATE_RINGING
                } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE) {
                    Log.d(TAG, "The Phone is idle")
                    if (lastStatusReceived == TelephonyManager.EXTRA_STATE_RINGING && lastStatusReceived != TelephonyManager.EXTRA_STATE_OFFHOOK) {
                        ismissed = true
                        Log.d(TAG, "Become missed call ")
                    }
                    lastStatusReceived = TelephonyManager.EXTRA_STATE_IDLE
                    end_time = System.currentTimeMillis()
                    Log.d(TAG, "end_time$end_time")
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPreferences.edit().putString("idlestate", "IDLE").apply()
                    val stopped = context.stopService(
                        Intent(
                            context,
                            RecordService::class.java
                        )
                    )
                    Log.d(
                        TAG,
                        "RecordService Stop serive called. returned state:$stopped"
                    )
                    sharedPreferences.edit().putString("recording", "").apply()
                    if (RecordService.recorder != null) {
                        RecordService.recorder!!.release()
                    }
                    /*RecordService.recorder.release();*/total_time =
                        ((end_time - start_time) / 1000 % 60).toDouble()
                    Log.d(TAG, "total_time" + total_time)
                    if (callInfo == null) {
                        callInfo = CallInfo()
                        callInfo!!.ringingTime = total_time
                        Log.d(TAG, "Ring Time" + callInfo!!.ringTime)
                        end_time = 0
                        start_time = 0
                    }
                } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                    Log.d(TAG, "The Phone is offhook")
                    lastStatusReceived = TelephonyManager.EXTRA_STATE_OFFHOOK
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPreferences.edit().putString("idlestate", "IDLE").apply()
                    strRecordingstate = sharedPreferences.getString("recording", "")
                    if (strRecordingstate == "") {
                        val recordServiceIntent = Intent(context, RecordService::class.java)
                        context.startService(recordServiceIntent)
                    }
                }
            }
        }
    }

    fun searchPhoneNo(context: Context?) {
        if (settings.getUserRole().equals(CallHistory.serviceAdvisor) || settings.getUserRole()
                .equals(CallHistory.serviceAdvisors)
        ) {
            val missedcallreference: Query = FirebaseDatabase.getInstance().getReferenceFromUrl(
                settings!!.getSYNC_SOURCE() + settings.getDealerId()
                    .toString() + "/ServiceAgent/" + settings.getUserId()
                    .toString() + "/LastCallHistory"
            )
            val pushkey = arrayOfNulls<String>(1)
            missedcallreference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnapshot in dataSnapshot.children) {
                        Log.d("User key", postSnapshot.key!!)
                        pushkey[0] = postSnapshot.key
                        Log.d("User ref", postSnapshot.ref.toString())
                        Log.d("User val", postSnapshot.value.toString())
                        val str = postSnapshot.key
                        if (str != null && str == strphoneNumber) {
                            if (dataSnapshot.childrenCount == 0L || dataSnapshot.value == null) {
                                println("Sorry number is not present in schedule call")
                                Toast.makeText(context, R.string.numbernotfound, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                val callInfo = postSnapshot.getValue(
                                    CallInfo::class.java
                                )
                                println(callInfo!!.customerName + " - " + callInfo.customerPhone)
                                callInfo.customerPhone = strphoneNumber
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    companion object {
        private const val TAG = "CallBroadCastReceiver"
        private var lastStatusReceived = ""
        var total_time = 0.0
        var ismissed = false
    }
}