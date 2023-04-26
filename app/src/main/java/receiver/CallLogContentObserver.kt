package receiver

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.CallLog
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.demo.autosherpa3.R
import com.demo.autosherpa3.SplashActivity.MY_PREFS_NAME
import com.demo.autosherpa3.WyzConnectApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import database.DBHelper
import entity.CallInfo
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrointerface.APIClient
import retrointerface.AudioDataResponse
import service.UploadService
import util.CallHistory
import util.CallStatusSemaphoreLock
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

mport static android.content.Context.MODE_PRIVATE;
import com.demo.autosherpa3.SplashActivity.MY_PREFS_NAME


import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.CallLog
import android.util.Log
import android.widget.Toast

import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat

import com.demo.autosherpa3.R

import com.demo.autosherpa3.WyzConnectApp

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

import database.DBHelper
import entity.CallInfo
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrointerface.APIClient

import retrointerface.AudioDataResponse

import service.UploadService

import util.CallHistory

import util.CallStatusSemaphoreLock

import util.CommonSettings

class CallLogContentObserver(handler: Handler?) : ContentObserver(handler), ActivityCompat.OnRequestPermissionsResultCallback {

    private var callerName: String? = null
    private var callInfo: CallInfo? = null
    private lateinit var context: Context

    private var strQueueType: String? = null
    private var callIdleState: String? = null
    private var strMakeCallFrom: String? = null
    private var strRecordId: String? = null
    private var strRecusName: String? = null

    private var strModuleType: String? = null
    private var strFromNotification: String? = null
    private var strForeOrBackground: String? = null

    private var phNumber: String? = null

    companion object {
        private const val DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
        private var dbhelper: DBHelper? = null
        private var service: UploadService? = null
        private var uploadFirebaseFlag = false

        private const val PERMISSION_REQUEST_CALL_LOG = 1

        private var dateString: String? = null
        private var timeString: String? = null

        private const val HOSTNAME = "137.59.201.20"
        private const val USERNAME = "wyzaudio"
        private const val PASSWORD = "audio@123"
        private const val PORT = "2121"
        private var UPLOAD_PATH: String? = null

        private val audioStatusList = ArrayList<AudioDataResponse>()

        var prefs: SharedPreferences? = null

        override fun onChange(selfChange: Boolean, uri: Uri) {
            settings = (context?.applicationContext as WyzConnectApp).settings
            UPLOAD_PATH = "/" + settings?.dealerId + "/"
            Log.d(TAG, "Received onChange message")
            context?.contentResolver?.unregisterContentObserver(this)

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            strqueuetype = sharedPreferences.getString("queueType", "")
            callidlestate = sharedPreferences.getString("idlestate", "")
            strMakeCallFrom = sharedPreferences.getString("webmakecallfrom", "")
            strrecordId = sharedPreferences.getString("recordid", "")
            strrecusname = sharedPreferences.getString("customername", "")

            strfromnotification = sharedPreferences.getString("notificationmodule", "")
            strforeorbackground = sharedPreferences.getString("foreorback", "")
            println(strqueuetype)
            println(strMakeCallFrom)
            println(strrecordId)
            println(strrecusname)

            try {
                val uuid = CallStatusSemaphoreLock.getInstance().uniquieId
                callInfo = (context?.applicationContext as WyzConnectApp).callInfoCache
                Log.d(TAG, "getcallinfocache $callInfo")
                insertCallDetails(uuid)
            } catch (e: InterruptedException) {
                Log.d(TAG, "Could not obtain the lock")
                e.printStackTrace()
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
        }

        private fun insertCallDetails(uid: String) {
            val uniqueId = settings?.uniqueidForCallSync
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        fun setContext(cn: Context) {
            context = cn
        }
    }


    val queryArgs = Bundle().apply {
        putInt(ContentResolver.QUERY_ARG_OFFSET, 30)
        putInt(ContentResolver.QUERY_ARG_LIMIT, 20)
    }

    var managedCursor: Cursor? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
        managedCursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            queryArgs,
            null
        )
    } else
    {
        managedCursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            "${CallLog.Calls.DEFAULT_SORT_ORDER} LIMIT 1"
        )
    }

    val name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
    val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
    val duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
    val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
    val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)

    if (managedCursor.moveToFirst())
    {
        var callerName = managedCursor.getString(name)

        if (callInfo != null) {
            callerName = callInfo.customerName
            strmoduletype = callInfo.makeCallFrom
            Log.d(TAG, "callinfo is not null")
        } else {
            callInfo = CallInfo()
            Log.d(TAG, "new callinfo has created $callInfo")
            callerName = "UNKNOWN"
            callInfo.customerName = callerName
            strmoduletype = "DialPad"

            /* DANGER ZONE SHOULD BE REMOVED ONCE USER AUTHENTICATION IS IMPLEMENTED */
        }

        val phoneNumber = managedCursor.getString(number)
        val callDuration = managedCursor.getString(duration)
        val callType: String?
        val directionCode = Integer.parseInt(managedCursor.getString(type))

        Log.d(TAG, "Obtained from call logs caller name: $callerName")
        Log.d(TAG, "Obtained from call logs PhoneNumber: $phoneNumber")
        Log.d(TAG, "Obtained from call logs callDuration: $callDuration")

        callType = when (directionCode) {
            CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
            CallLog.Calls.INCOMING_TYPE -> "INCOMING"
            else -> "MISSED"
        }
    }


    val sdf_date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val sdf_time = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    val dateString = sdf_date.format(Date(date.toLong()))
    val timeString = sdf_time.format(Date(date.toLong()))
// val duration_new=sdf_dur.format(Date(callDuration.toLong()))

    Log.d(TAG, "Obtained from call logs call type: $calltype")
    Log.d(TAG, "Obtained from call logs call date: $dateString")
    Log.d(TAG, "Obtained from call logs call time: $timeString")

    if ("MISSED" == calltype) {
        uniqueId = ""
    }

    if ("OUTGOING" != calltype) {
        if ("INCOMING" == calltype && callDuration == "0") {
            CallBroadCastReceiver.ismissed = false
            Log.d(TAG, "incoming callduration is 0")
            calltype = "MISSED"
            callInfo.setCallTypePicId(R.mipmap.ic_call_missed_outgoing_color_24dp)
        } else if (CallBroadCastReceiver.ismissed) {
            Log.d(TAG, "ismissed is true")
            calltype = "MISSED"
            callInfo.setCallTypePicId(R.mipmap.ic_call_missed_outgoing_color_24dp)
            CallBroadCastReceiver.ismissed = false
        }
    }

    callInfo.setCustomerName(caller_name)
    callInfo.setAgentName(settings.getUserId())
    callInfo.setCustomerPhone(phNumber)
    callInfo.setCallDate(dateString)
    callInfo.setCallTime(timeString)
    callInfo.setCallDuration(callDuration)
    callInfo.setCallType(calltype)
    callInfo.setInteractionDate(dateString)
    callInfo.setInteractionTime(timeString)

    if (callDuration.toDouble() <= 0) {
        val totalRingTime = CallBroadCastReceiver.total_time
        Log.d("CallLogContentObserver", "TotalRingTime $totalRingTime")
        callInfo.setRingTime(totalRingTime.toString())
        CallBroadCastReceiver.total_time = 0.0
    } else if (CallBroadCastReceiver.total_time < callDuration.toDouble()) {
        val totalRingTime = CallBroadCastReceiver.total_time
        Log.d("CallLogContentObserver", "TotalRingTime $totalRingTime")
        callInfo.setRingTime(totalRingTime.toString())
        CallBroadCastReceiver.total_time = 0.0
    } else {
        val totalRingTime = CallBroadCastReceiver.total_time
//val totalRingTime = CallBroadCastReceiver.total_time - callDuration.toDouble()
        Log.d("CallLogContentObserver", "TotalRingTime $totalRingTime")
        callInfo.setRingTime(totalRingTime.toString())
        CallBroadCastReceiver.total_time = 0.0
    }

    Log.d("Duration", callDuration)
    callInfo.setLongitude(settings.getLongitude())
    callInfo.setLatitude(settings.getLatitude())

    callInfo.setFilePath("${settings.getDealerId()}/${settings.getFileNameWithUniqueId()}")
    callInfo.setDealerCode(settings.getDealerId())

    if (callInfo.getCallType() != "MISSED") {
        if (uniqueId != null) {
            var bytearray: ByteArray? = null
            var fileInputStream: FileInputStream? = null
            val file = File("$DEFAULT_STORAGE_LOCATION/${settings.getFileNameWithUniqueId()}")
            // New Code
            var inputStream: InputStream? = null
            try {
                inputStream = FileInputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            println("File Size:" + file.getTotalSpace())

            val fileSize = file.length()

            println("File Length:" + fileSize)
        }

        if (callInfo.callType == "MISSED") {
            Log.d(TAG, "Missedcall firebase storing")
            callInfo.filePath = ""
            callInfo.fileSize = "0.00 kb"
            Handler().postDelayed({
                CallHistory.getInstance().WebHistory(strMakeCallFrom, strrecordId, callInfo)
                settings.vehicleRegNo = null
                clearCallinfoData()
            }, 2000) //2 seconds
        } else {
            when {
                callInfo.callType == "INCOMING" -> when {
                    strmoduletype == CallHistory.serviceAdvisor || strmoduletype == CallHistory.serviceAdvisors -> {
                    }
                    settings.userRole == CallHistory.strCRE || settings.userRole == CallHistory.strCre || settings.userRole == CallHistory.strcre -> {
                        callInfo.fileSize = ""
                        val file = File(DEFAULT_STORAGE_LOCATION + "/" + settings.fileNameWithUniqueId)
                        try {
                            if (file.exists() && file.isFile) {
                                val fileSize = getFileSizeKiloBytes(file)
                                callInfo.fileSize = fileSize
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Handler().postDelayed({
                            CallHistory.getInstance().WebHistory(strMakeCallFrom, strrecordId, callInfo)
                            settings.vehicleRegNo = null
                            clearCallinfoData()
                        }, 2000) //2 seconds
                    }
                    settings.userRole == CallHistory.strDRIVER || settings.userRole == CallHistory.strDriver || settings.userRole == CallHistory.strdriver -> {
                        //deleteCallRecording()
                    }
                    strmoduletype == CallHistory.strpsf -> {
                        // psfDisposition();
                    }
                }
                else -> when {
                    (strqueuetype == "WEB" || strqueuetype == "Web" || strqueuetype == "web") && callidlestate == "IDLE" -> {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                        sharedPreferences.edit().putString("queueType", "").apply()
                        sharedPreferences.edit().putString("idlestate", "").apply()
                        sharedPreferences.edit().putString("webmakecallfrom", "").apply()
                        sharedPreferences.edit().putString("recordid", "").apply()
                        if (strrecusname == "" || strrecusname.isEmpty()) {
                        } else {
                            callInfo.customerName = strrecusname
                        }
                        callInfo.fileSize = ""
                        val file =
                            File(DEFAULT_STORAGE_LOCATION + "/" + settings.fileNameWithUniqueId)
                        try {
                            if (file.exists() && file.isFile) {
                                val fileSize = getFileSizeKiloBytes(file)
                                callInfo.fileSize = fileSize
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Handler().postDelayed({
                            CallHistory.getInstance()
                                .WebHistory(strMakeCallFrom, strrecordId, callInfo)
                            settings.vehicleRegNo = null
                            clearCallinfoData()
                        }, 2000) //2 seconds
                    }
                    strqueuetype == CallHistory.strDriver && callidlestate == "IDLE" -> {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                        sharedPreferences.edit().putString("queueType", "").apply()
                        sharedPreferences.edit().putString("idlestate", "").apply()
                        //sharedPreferences.edit().putString("filepath", callInfo.filePath).apply()
                        updatecallStatus()
                    }

                    else if (strmoduletype == "DialPad" || settings.userRole == CallHistory.strCRE || settings.userRole == CallHistory.strCre || settings.userRole == CallHistory.strcre && callInfo.callType == "OUTGOING") {
                        callInfo.filePath = "${settings.dealerId}/${settings.fileNameWithUniqueId}"
                        callInfo.fileSize = ""

                        val file =
                            File("$DEFAULT_STORAGE_LOCATION/${settings.fileNameWithUniqueId}")
                        try {
                            if (file.exists() && file.isFile) {
                                val fileSize = getFileSizeKiloBytes(file)
                                callInfo.fileSize = fileSize
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        val handler = Handler()
                        handler.postDelayed({
                            CallHistory.getInstance()
                                .WebHistory(strMakeCallFrom, strrecordId, callInfo)
                            settings.vehicleRegNo = null
                            clearCallinfoData()
                        }, 2000)   //2 seconds


                    }
                    else {
                        if (strmoduletype == CallHistory.serviceAdvisor || strmoduletype == CallHistory.serviceAdvisors && callInfo.callType == "OUTGOING") {
                        } else if (strmoduletype == CallHistory.strpsf && callInfo.callType == "OUTGOING") {
                        } else if (settings.userRole == CallHistory.strCRE || settings.userRole == CallHistory.strCre || settings.userRole == CallHistory.strcre && callInfo.callType == "OUTGOING") {
                        } else if (settings.userRole == CallHistory.strDRIVER || settings.userRole == CallHistory.strDriver || settings.userRole == CallHistory.strdriver && callInfo.callType == "OUTGOING") {
                        } else if (strmoduletype == "DialPad") {
                        }

                    }

                    Log.d(TAG, "calldispositionactivity has started!")
                            AsyncSendFile ().execute()
                            Log . d (TAG, "callinfo before set cache $callInfo")
                        (applicationContext as WyzConnectApp).setCallInfoCache(callInfo)
                    setCallInfoCache()
                            managedCursor . close ()


                }


                private fun getFileSizeKiloBytes(file: File): String {
                val f = file.length() / 1024f
                return String.format("%.2f", f) + " kb"
            }

                override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
                if (requestCode == PERMISSION_REQUEST_CALL_LOG) {
                    if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Camera permission has been granted, preview can be displayed
                        Log.i(TAG, "Call Log permission has now been granted.")
                    } else {
                        Log.i(TAG, "Call Log permission was NOT granted.")
                    }
                }
            }

                fun updateCallStatus() {
                    val isCallMadeReference = FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.DRIVERPICKUP_URL)
                    var pushkey: String? = null
                    isCallMadeReference.orderByChild(CallHistory.strvehicleNumber).equalTo(callInfo.vehicleNumber)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (child in dataSnapshot.children) {
                                    Log.d("children count", dataSnapshot.children.toString())
                                    Log.d("User key", child.key!!)
                                    pushkey = child.key
                                    Log.d("User ref", child.ref.toString())
                                    Log.d("User val", child.value.toString())
                                }
                                if (dataSnapshot.childrenCount == 0L || dataSnapshot.value == null) {
                                    System.out.println("Sorry number is not present in schedule call")
                                } else {
                                    if (callInfo.callDuration != "0") {
                                        Toast.makeText(context, "CallDuration is " + callInfo.callDuration, Toast.LENGTH_SHORT).show()
                                        isCallMadeReference.child(pushkey!!).child("isCallMade").setValue("yes")
                                        isCallMadeReference.child(pushkey!!).child("mediaFile").setValue(callInfo.mediaFile)
                                        insertHistory()
                                    } else {
                                        Toast.makeText(context, R.string.durationzero, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                }

                    fun insertHistory() {
                    val vehicleStatusUpdateRef = FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.DRIVERPICKED_URL)
                    val lastRef = vehicleStatusUpdateRef.push()
                    currentDateTime()
                    changeStatus(callInfo)
                    callInfo.interactionDate = dateString
                    callInfo.interactionTime = timeString
                    callInfo.isCallMade = "yes"
                    if (callInfo.callDuration != "0") {
                        lastRef.setValue(callInfo)
                    }
                    callInfo = null
                    (context.applicationContext as WyzConnectApp).setCallInfoCache(null)
                }

                fun currentDateTime() {
                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    dateString = sdfDate.format(Date())
                    timeString = sdfTime.format(Date())
                }

                    fun changeStatus(callInfo: CallInfo) {
                    callInfo.trackStatus = when (callInfo.trackStatus) {
                        "Cancelled" -> CallHistory.STATUS_CANCELLED.toString()
                        "PickupStarted" -> CallHistory.STATUS_STARTED.toString()
                        "ReachedPoint" -> CallHistory.STATUS_REACHED_POINT.toString()
                        "VehiclePicked" -> CallHistory.STATUS_PICKEDUP.toString()
                        "Completed" -> CallHistory.STATUS_DROPPED.toString()
                        else -> callInfo.trackStatus
                    }
                }
                fun setCallInfoCache() {
                    Log.d(TAG, "Moduletype is $strmoduletype")
                    if (strmoduletype.equals("DialPad", ignoreCase = true) || strqueuetype.equals("WEB", ignoreCase = true)) {
                        (context.applicationContext as WyzConnectApp).setCallInfoCache(null)
                    }
                }

                    @Throws(IOException::class)
                fun deleteCallRecording(filename: String) {
                    val file = File(DEFAULT_STORAGE_LOCATION, filename)
                    if (file.exists()) {
                        val deleted = file.delete()
                        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                        // Toast.makeText(mContext, String.valueOf(deleted), Toast.LENGTH_SHORT).show()
                    }
                }

                        inner class AsyncSendFile : AsyncTask<Void, Void, Void>() {
                    var flag = false

                    override fun doInBackground(vararg voids: Void): Void? {
                        flag = uploadFileUsingRetrofit(settings.fileNameWithUniqueId)
                        /*if(flag)
                        {
                            try {
                                deleteCallRecording(settings.getFileNameWithUniqueId());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }*/
                        return null
                    }

                    override fun onPostExecute(result: Void?) {
                        super.onPostExecute(result)
                        if (flag) {
                            /*
                            Toast.makeText( context, "File transferred", Toast.LENGTH_SHORT ).show();
                            */
                        } else {
                            /*
                            Toast.makeText( context, "File couldn't transferred", Toast.LENGTH_SHORT ).show();
                            */
                        }
                    }
                }

                    fun uploadFileUsingRetrofit(uniqueId: String): Boolean {
                prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
                val dealerUrl = prefs.getString("DealerUrl", "")
                service = APIClient.getClient(dealerUrl).create(UploadService::class.java)
                val file = File(DEFAULT_STORAGE_LOCATION, uniqueId)
                var uploadFirebaseFlag = false
                if (file.exists()) {
                    try {
                        AudioStatusList = ArrayList()
                        val name: RequestBody
                        val reqFile = RequestBody.create(MediaType.parse("audio/*"), file)
                        val body = MultipartBody.Part.createFormData("audio", file.name, reqFile)
                        name = if (uniqueId == null) {
                            RequestBody.create(MediaType.parse("text/plain"), "0")
                        } else {
                            RequestBody.create(MediaType.parse("text/plain"), uniqueId)
                        }
                        try {
                            val req = service.postImage(body, name)
                            req.enqueue(object : Callback<ArrayList<AudioDataResponse>> {
                                override fun onResponse(call: Call<ArrayList<AudioDataResponse>>, response: Response<ArrayList<AudioDataResponse>>) {
                                    try {
                                        println("Response :" + response.message())
                                        AudioStatusList = response.body()
                                        val yourFilePath = DEFAULT_STORAGE_LOCATION + "/" + uniqueId
                                        val file = File(yourFilePath)
                                        val result = file.delete()
                                        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                                        val status = response.body()!![0].status
                                        val audioFile = response.body()!![0].filename
                                        uploadFirebaseFlag = true
                                        try {
                                            if (AudioStatusList.size > 0) {
                                                dbhelper = DBHelper(context)
                                                for (data in AudioStatusList) {
                                                    dbhelper.updateSyncStatus(audioFile, status)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                override fun onFailure(
                                    call: Call<ArrayList<AudioDataResponse?>?>?,
                                    t: Throwable
                                ) {
                                    t.printStackTrace()
                                    println("Error Response :" + t.message)
                                    CallLogContentObserver.uploadFirebaseFlag = false
                                }
                            })

                            println("uploadFirebaseFlag : " + CallLogContentObserver.uploadFirebaseFlag)
                        } catch (Exception e) {
                            e.printStackTrace()
                        }
                    }
                }


            } catch (Exception fe) {
                fe.printStackTrace()
            }

            }


            return uploadFirebaseFlag
        }

        fun clearCallinfoData() {
            callInfo!!.ageOfVehicle = 0
            callInfo!!.agentName = null
            callInfo!!.callDate = null
            callInfo!!.callDuration = null
            callInfo!!.callTime = null
            callInfo!!.callType = null
            callInfo!!.callTypePicId = 0
            callInfo!!.customerName = null
            callInfo!!.customerPhone = null
            callInfo!!.daysBetweenVisit = 0
            callInfo!!.dealerCode = null
            callInfo!!.filePath = null
            callInfo!!.interactionDate = null
            callInfo!!.interactionTime = null
            callInfo!!.latitude = null
            callInfo!!.longitude = null
            callInfo!!.makeCallFrom = null
            callInfo!!.ringTime = null
            callInfo!!.ringingTime = 0
            callInfo!!.uniqueidForCallSync = 0
            callInfo!!.fileSize = "0.00 kb"
        }


    }


}


