package receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Handler
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.demo.autosherpa3.R
import com.demo.autosherpa3.WyzConnectApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import entity.CallInfo
import service.RecordService
import util.CallHistory
import util.CommonSettings

class CallBroadCastReceiver : BroadcastReceiver() {
    private val TAG = "CallBroadCastReceiver"
    private var callInfo: CallInfo? = null
    private var lastStatusReceived = ""
    private var start_time: Long = 0
    private var end_time: Long = 0
    private var total_time = 0.0
    private var settings: CommonSettings? = null
    private var lm: LocationManager? = null
    private var gps_enabled = false
    private var strphoneNumber: String? = null
    private var strRecordingstate: String? = null
    private var ismissed = false

    override fun onReceive(context: Context?, intent: Intent?) {
        settings = (context?.applicationContext as WyzConnectApp).settings
        callInfo = (context.applicationContext as WyzConnectApp).callInfoCache

        if (settings!!.isAuthenticated) {
            Log.d("CallBroadCastReceiver", "CallBroadcastReceiver::onReceive got Intent: ${intent.toString()}")
            if (intent?.action == Intent.ACTION_NEW_OUTGOING_CALL) {
                Log.d(TAG, "Out going call intent received")
                lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                try {
                    gps_enabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                if (!gps_enabled) {
                    Toast.makeText(context, "Please turn on the Location services!", Toast.LENGTH_LONG).show()
                }

                strphoneNumber = intent?.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                if (strphoneNumber!!.contains("+91")) {
                    Log.d(TAG, "Mobileno$strphoneNumber")
                    settings?.userMobileNo = strphoneNumber
                } else {
                    strphoneNumber = "+91$strphoneNumber"
                    settings?.userMobileNo = strphoneNumber
                }

                if (callInfo == null) {
                    searchPhoneNo(context)
                }

                Handler().postDelayed({
                    val observer = CallLogContentObserver(Handler())
                    observer.context = context
                    context?.contentResolver?.registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, observer)
                }, 5000) // 5 seconds
            }
        }
    }

    class CallStateReceiver : BroadcastReceiver() {

        private val TAG = "CallStateReceiver"
        private var strphoneNumber: String? = null
        private var start_time: Long = 0
        private var end_time: Long = 0
        private var total_time: Long = 0
        private var ismissed = false
        private var gps_enabled = false
        private var lm: LocationManager? = null
        private var callInfo: CallInfo? = null
        private var lastStatusReceived = TelephonyManager.EXTRA_STATE_IDLE
        private var strRecordingstate: String? = null

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == Intent.ACTION_BOOT_COMPLETED) {
                // Do something on boot completed
            } else if (action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                Log.d(TAG, "Phone state changed intent received")
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        Log.d(TAG, "The Phone is ringing")
                        strphoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        start_time = System.currentTimeMillis()
                        Log.d(TAG, "start_time$start_time")
                        settings.setUserMobileNo(strphoneNumber)
                        if (callInfo == null) {
                            searchPhoneNo(context)
                        }
                        lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        try {
                            gps_enabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                        if (!gps_enabled) {
                            Toast.makeText(context, "Please turn on the Location services!", Toast.LENGTH_LONG).show()
                        }
                        (context.applicationContext as WyzConnectApp).setCallInfoCache(null)
                        Log.d(TAG, "Incoming call callinfocache is null")
                        if (lastStatusReceived != TelephonyManager.EXTRA_STATE_RINGING) {
                            Handler().postDelayed({
                                val observer = CallLogContentObserver(Handler())
                                observer.setContext(context)
                                context.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
                            }, 5000) // 5 seconds
                            Log.d(TAG, "Ringing state ContentObserver is Initialized")
                        }
                        lastStatusReceived = TelephonyManager.EXTRA_STATE_RINGING
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        Log.d(TAG, "The Phone is idle")
                        if (lastStatusReceived == TelephonyManager.EXTRA_STATE_RINGING && lastStatusReceived != TelephonyManager.EXTRA_STATE_OFFHOOK) {
                            ismissed = true
                            Log.d(TAG, "Become missed call")
                        }
                        lastStatusReceived = TelephonyManager.EXTRA_STATE_IDLE
                        end_time = System.currentTimeMillis()
                        Log.d(TAG, "end_time$end_time")
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                        sharedPreferences.edit().putString("idlestate", "IDLE").apply()
                        val stopped = context.stopService(Intent(context, RecordService::class.java))
                        Log.d(TAG, "RecordService Stop service called. returned state:$stopped")
                        sharedPreferences.edit().putString("recording", "").apply()
                        if (RecordService.recorder != null) {
                            RecordService.recorder?.release()
                        }

                        var total_time = ((end_time - start_time) / 1000) % 60
                        Log.d(TAG, "total_time $total_time")

                        if (callInfo == null) {
                            callInfo = CallInfo()
                            callInfo.ringingTime = total_time
                            Log.d(TAG, "Ring Time ${callInfo.ringTime}")
                            end_time = 0
                            start_time = 0
                        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                            Log.d(TAG, "The Phone is offhook")
                            lastStatusReceived = TelephonyManager.EXTRA_STATE_OFFHOOK

                            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                            sharedPreferences.edit().putString("idlestate", "IDLE").apply()

                            val strRecordingstate = sharedPreferences.getString("recording", "")

                            if (strRecordingstate == "") {
                                val recordServiceIntent = Intent(context, RecordService::class.java)
                                context.startService(recordServiceIntent)
                            }
                        }

                        fun searchPhoneNo(context: Context) {
                            if (settings.userRole == CallHistory.serviceAdvisor || settings.userRole == CallHistory.serviceAdvisors) {

                                val missedcallreference = FirebaseDatabase.getInstance().getReferenceFromUrl(settings.SYNC_SOURCE + settings.dealerId + "/ServiceAgent/" + settings.userId + "/LastCallHistory")
                                val pushkey = arrayOf<String?>(null)

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
                                                    System.out.println("Sorry number is not present in schedule call")
                                                    Toast.makeText(context, R.string.numbernotfound, Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val callInfo = postSnapshot.getValue(CallInfo::class.java)
                                                    System.out.println(callInfo!!.customerName + " - " + callInfo.customerPhone)
                                                    callInfo.customerPhone = strphoneNumber
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {

                                    }
                                })
                            }
                        }

}