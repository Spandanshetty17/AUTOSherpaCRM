package util

import android.content.Context
import android.widget.Toast
import com.demo.autosherpa3.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import entity.CallInfo
import java.io.File
import activity.WyzConnectApp
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CallHistory {
    private var mContext: Context? = null

    const val serviceReschedule = "3"
    const val serviceCancelled = "35"
    const val servicenotRequired = "5"
    const val ringingnoResponse = "6"
    const val wrongNumber = "9"
    const val busy = "7"
    const val notReachable = "10"
    const val switchedOff = "8"
    const val vehiclereceived = "38"
    const val serviceAdvisor = "ServiceAdvisor"
    const val serviceAdvisors = "Service Advisor"
    const val strDriver = "Driver"
    const val strDRIVER = "DRIVER"
    const val strdriver = "driver"
    const val strCRE = "CRE"
    const val strCre = "Cre"
    const val strcre = "cre"
    const val strpsf = "PSF"
    const val strvehicleNumber = "vehicleNumber"

    private val syncSource = CommonSettings.instance.SYNC_SOURCE
    private val dealerId = CommonSettings.instance.dealerId
    private val userId = CommonSettings.instance.userId

    val creUrl = "$syncSource$dealerId/CRE/$userId/CREDashBoard"
    val creHistoryUrl = "$syncSource$dealerId/CRE/$userId/WebHistory/CallInfo"
    val driverPickupUrl = "$syncSource$dealerId/Driver/$userId/DriverPickupList/CallInfo"
    val driverPickedUrl = "$syncSource$dealerId/Driver/$userId/DriverHistory"

    const val STATUS_STARTED = 1
    const val STATUS_REACHED_POINT = 2
    const val STATUS_PICKEDUP = 3
    const val STATUS_DROPPED = 4
    const val STATUS_CANCELLED = 5

    private var dateString: String? = null
    private var timeString: String? = null
    private var strDisableUserStatus: String? = null

    fun init(context: Context) {
        mContext = context
    }
    private val singleton = CallHistory()

    private const val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0

    val phoneModel: String = "${android.os.Build.MANUFACTURER} - ${android.os.Build.MODEL}"

    val androidVersion: String = android.os.Build.VERSION.RELEASE

    private var strDisableUserStatus = ""

    private lateinit var mContext: Context

    private const val CREHISTORY_URL = "https://wyzcrm-2feff.firebaseio.com/CREHistory/"

    private const val DRIVERPICKED_URL = "https://wyzcrm-2feff.firebaseio.com/DriverPicked/"

    fun getInstance(context: Context): CallHistory {
        mContext = context.applicationContext
        return singleton
    }

    fun webHistory(strMakeCallFrom: String, strrecordId: String, callInfo: CallInfo) {
        val callLogReference = FirebaseDatabase.getInstance().getReferenceFromUrl(CREHISTORY_URL)
        callInfo.makeCallFrom = strMakeCallFrom

        if (strrecordId.isNotEmpty()) {
            callInfo.uniqueidForCallSync = strrecordId.toInt()
        }

        val key = callLogReference.push().key
        key?.let { callLogReference.child(it).setValue(callInfo) }
    }

    fun insertHistory(callInfo: CallInfo) {
        val vehicleStatusUpdateRef =
            FirebaseDatabase.getInstance().getReferenceFromUrl(DRIVERPICKED_URL).push()

        currentDateTime(callInfo)

        if (callInfo.callDuration != "0") {
            vehicleStatusUpdateRef.setValue(callInfo)
        }

        callInfo = null
        (CallHistory.mContext.getApplicationContext() as WyzConnectApp).setCallInfoCache(null)
    }

        private fun currentDateTime(callInfo: CallInfo) {
        val sdf_date = SimpleDateFormat("dd/MM/yyyy")
        val sdf_time = SimpleDateFormat("HH:mm:ss")

        callInfo.interactionDate = sdf_date.format(Date())
        callInfo.interactionTime = sdf_time.format(Date())
    }

    fun getDisableUserStatus(): String {
        val settings = CommonSettings.instance

        val ref = FirebaseDatabase.getInstance().getReferenceFromUrl("${settings.syncSource}${settings.dealerId}/users/${settings.userId}")
        ref.child("DisableUser").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    //Toast.makeText(getApplicationContext(), R.string.usentavailable, Toast.LENGTH_SHORT).show();
                } else {
                    strDisableUserStatus = snapshot.value.toString()
                    if (strDisableUserStatus.equals("True", ignoreCase = true)) {
                        settings.isDisableUser = true
                        Toast.makeText(mContext, R.string.userdisabled, Toast.LENGTH_SHORT).show()
                    } else {
                        settings.isDisableUser = false
                    }
                }
            }

            override fun onCancelled(firebaseError: DatabaseError) {
                println("The read failed: " + firebaseError.message)
            }
        })
        return strDisableUserStatus
    }

    @Throws(IOException::class)
    fun deleteAudioFile(fileName: File) {
        val file = File("/sdcard/wyzcallrecorder/$fileName")
        if (file.exists()) {
            val deleted = file.delete()
            Toast.makeText(mContext, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            // Toast.makeText(mContext, String.valueOf(deleted), Toast.LENGTH_SHORT).show();
        }
    }
}