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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.CallLog
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.autosherpas.autosherpa3.SplashActivity.MY_PREFS_NAME
import activity.WyzConnectApp
import com.google.firebase.database.*
import database.DBHelper
import entity.CallInfo
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
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created By 1524 on 10/1/2020
 */
class CallLogContentObserver(handler: Handler?) : ContentObserver(handler),
    OnRequestPermissionsResultCallback {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run [.onChange] on, or null if none.
     */
    private var caller_name: String? = null
    private var callInfo: CallInfo? = null
    fun setContext(cn: Context?) {
        context = cn
    }

    var strqueuetype: String? = null
    var callidlestate: String? = null
    var strMakeCallFrom: String? = null
    var strrecordId: String? = null
    var strrecusname: String? = null
    var strmoduletype: String? = null
    var strfromnotification: String? = null
    var strforeorbackground: String? = null
    var phNumber: String? = null
    var AudioStatusList: ArrayList<AudioDataResponse>? = null
    var prefs: SharedPreferences? = null
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        settings = (context!!.applicationContext as WyzConnectApp).getSettings()
        UPLOAD_PATH = "/" + settings.getDealerId().toString() + "/"
        Log.d(TAG, "Received onChange message")
        context!!.contentResolver.unregisterContentObserver(this)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            context!!.applicationContext
        )
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
            val uuid: String = CallStatusSemaphoreLock.getInstance().getUniquieId()
            callInfo = (context!!.applicationContext as WyzConnectApp).getCallInfoCache()
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
        var uniqueId: String = settings.uniqueidForCallSync
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        /*  String order = "date ASC limit 100";
       // Cursor managedCursor = context.getContentResolver().query( android.provider.CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER + " LIMIT 1" );
        Cursor managedCursor = context.getContentResolver().query( android.provider.CallLog.Calls.CONTENT_URI, null, null, null, order );*/
        val queryArgs = Bundle()
        queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, 30)
        queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, 20)
        var managedCursor: Cursor? = null // Cancellation signal.
        managedCursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.contentResolver.query(
                CallLog.Calls.CONTENT_URI,  // Content Uri is specific to individual content providers.
                null,  // String[] describing which columns to return.
                queryArgs,  // Query arguments.
                null
            )
        } else {
            context!!.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER + " LIMIT 1"
            )
        }
        val name = managedCursor!!.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
        val duration1 = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        val type1 = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val date1 = managedCursor.getColumnIndex(CallLog.Calls.DATE)
        if (managedCursor.moveToFirst()) {
            caller_name = managedCursor.getString(name)
            if (callInfo != null) {
                caller_name = callInfo!!.customerName
                strmoduletype = callInfo!!.makeCallFrom
                Log.d(TAG, "callinfo is not null")
            } else {
                callInfo = CallInfo()
                Log.d(
                    TAG,
                    "new callinfo has created $callInfo"
                )
                caller_name = "UNKNOWN"
                callInfo!!.customerName = caller_name
                strmoduletype = "DialPad"
                /** DANGER ZONE SHOULD BE REMOVED ONCE USER AUTHENTICATION IS IMPLEMENTED  */
            }
            phNumber = managedCursor.getString(number)
            val callDuration = managedCursor.getString(duration1)
            val type = managedCursor.getString(type1)
            val date = managedCursor.getString(date1)
            var calltype: String? = null
            val dircode = type.toInt()
            Log.d(
                TAG,
                "Obtained from call logs caller name: $caller_name"
            )
            Log.d(
                TAG,
                "Obtained from call logs PhoneNumber: $phNumber"
            )
            Log.d(
                TAG,
                "Obtained from call logs callDuration: $callDuration"
            )
            calltype = when (dircode) {
                CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                else -> "MISSED"
            }
            val sdf_date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdf_time = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateString = sdf_date.format(Date(date.toLong()))
            val timeString = sdf_time.format(Date(date.toLong()))
            //  String duration_new=sdf_dur.format(new Date(Long.parseLong(callDuration)));
            Log.d(
                TAG,
                "Obtained from call logs call type: $calltype"
            )
            Log.d(
                TAG,
                "Obtained from call logs call date: $dateString"
            )
            Log.d(
                TAG,
                "Obtained from call logs call time: $timeString"
            )
            if ("MISSED" == calltype) {
                uniqueId = ""
            }
            if ("OUTGOING" != calltype) {
                if ("INCOMING" == calltype && callDuration == "0") {
                    CallBroadCastReceiver.ismissed = false
                    Log.d(TAG, "incoming callduration is 0")
                    calltype = "MISSED"
                    callInfo!!.callTypePicId = R.mipmap.ic_call_missed_outgoing_color_24dp
                } else if (CallBroadCastReceiver.ismissed) {
                    Log.d(TAG, "ismissed is true")
                    calltype = "MISSED"
                    callInfo!!.callTypePicId = R.mipmap.ic_call_missed_outgoing_color_24dp
                    CallBroadCastReceiver.ismissed = false
                }
            }
            callInfo!!.customerName = caller_name
            callInfo!!.agentName = settings.getUserId()
            callInfo!!.customerPhone = phNumber
            callInfo!!.callDate = dateString
            callInfo!!.callTime = timeString
            callInfo!!.callDuration = callDuration
            callInfo!!.callType = calltype
            callInfo!!.interactionDate = dateString
            callInfo!!.interactionTime = timeString
            if (callDuration.toDouble() <= 0) {
                val totalRingTime: Double = CallBroadCastReceiver.total_time
                Log.d("CallLogContentObserver", "TotalRingTime $totalRingTime")
                callInfo!!.ringTime = totalRingTime.toString()
                CallBroadCastReceiver.total_time = 0.0
            } else if (CallBroadCastReceiver.total_time < callDuration.toDouble()) {
                val totalRingTime: Double = CallBroadCastReceiver.total_time
                Log.d("CallLogContentObserver", "TotalRingTime $totalRingTime")
                callInfo!!.ringTime = totalRingTime.toString()
                CallBroadCastReceiver.total_time = 0.0
            } else {
                val totalRingTime: Double = CallBroadCastReceiver.total_time
                //double totalRingTime = CallBroadCastReceiver.total_time - Double.parseDouble(callDuration);
                Log.d("CallLogContentObserver", "TotalRingTime $totalRingTime")
                callInfo!!.ringTime = totalRingTime.toString()
                CallBroadCastReceiver.total_time = 0.0
            }
            Log.d("Duration", callDuration)
            callInfo!!.longitude = settings.getLongitude()
            callInfo!!.latitude = settings.getLatitude()
            callInfo!!.filePath = settings.getDealerId() + "/" + settings.fileNameWithUniqueId
            callInfo!!.dealerCode = settings.getDealerId()
            if (callInfo!!.callType != "MISSED") {
                if (uniqueId != null) {
                    val bytearray: ByteArray? = null
                    val fileInputStream: FileInputStream? = null
                    val file =
                        File(DEFAULT_STORAGE_LOCATION + "/" + settings.fileNameWithUniqueId)

                    // New Code
                    var inputStream: InputStream? = null
                    try {
                        inputStream = FileInputStream(file)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                    println("File Size:" + file.totalSpace)
                    val fileSize = file.length()
                    println("File Length:$fileSize")

                    /* byte[] bytes;
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try {
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("BYTEARRAY SIZE", String.valueOf(+output.size()));

                    bytes = output.toByteArray();
                    String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
                    //callInfo.setMediaFile(encodedString);

                    Log.d("ENCODED STRING SIZE", String.valueOf(encodedString.length()));

                    byte[] decoded = Base64.decode(encodedString, 0);
                    Log.d("~~~~~~~~ Decoded: ", Arrays.toString(decoded));
                    try {
                        File file2 = new File(Environment.getExternalStorageDirectory() + "/hellotest.wav");
                        FileOutputStream os = new FileOutputStream(file2, true);
                        os.write(decoded);
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
            }
            if (callInfo!!.callType == "MISSED") {
                Log.d(TAG, "Missedcall firebase storing")
                callInfo!!.filePath = ""
                callInfo!!.fileSize = "0.00 kb"
                val handler = Handler()
                handler.postDelayed({
                    CallHistory.getInstance().WebHistory(strMakeCallFrom, strrecordId, callInfo)
                    settings.vehicleRegNo = null
                    clearCallinfoData()
                }, 2000) //2 seconds
            } else {
                if (callInfo!!.callType == "INCOMING") {
                    if (strmoduletype == CallHistory.serviceAdvisor || strmoduletype == CallHistory.serviceAdvisors) {
                    } else if (settings.getUserRole()
                            .equals(CallHistory.strCRE) || settings.getUserRole()
                            .equals(CallHistory.strCre) || settings.getUserRole()
                            .equals(CallHistory.strcre)
                    ) {
                        callInfo!!.fileSize = ""
                        val file =
                            File(DEFAULT_STORAGE_LOCATION + "/" + settings.fileNameWithUniqueId)
                        try {
                            if (file.exists() && file.isFile) {
                                val fileSize = getFileSizeKiloBytes(file)
                                callInfo!!.fileSize = fileSize
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
                        }, 2000) //2 seconds
                    } else if (settings.getUserRole()
                            .equals(CallHistory.strDRIVER) || settings.getUserRole()
                            .equals(CallHistory.strDriver) || settings.getUserRole()
                            .equals(CallHistory.strdriver)
                    ) {
                        //deleteCallRecording();
                    } else if (strmoduletype == CallHistory.strpsf) {
                        // psfDisposition();
                    }
                } else {
                    if (strqueuetype == "WEB" || strqueuetype == "Web" || strqueuetype == "web" && callidlestate == "IDLE") {
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                            context!!.applicationContext
                        )
                        sharedPreferences.edit().putString("queueType", "").apply()
                        sharedPreferences.edit().putString("idlestate", "").apply()
                        sharedPreferences.edit().putString("webmakecallfrom", "").apply()
                        sharedPreferences.edit().putString("recordid", "").apply()
                        if (strrecusname == "" || strrecusname!!.isEmpty()) {
                        } else {
                            callInfo!!.customerName = strrecusname
                        }
                        callInfo!!.fileSize = ""
                        val file =
                            File(DEFAULT_STORAGE_LOCATION + "/" + settings.fileNameWithUniqueId)
                        try {
                            if (file.exists() && file.isFile) {
                                val fileSize = getFileSizeKiloBytes(file)
                                callInfo!!.fileSize = fileSize
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
                        }, 2000) //2 seconds
                    } else if (strqueuetype == CallHistory.strDriver && callidlestate == "IDLE") {
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                            context!!.applicationContext
                        )
                        sharedPreferences.edit().putString("queueType", "").apply()
                        sharedPreferences.edit().putString("idlestate", "").apply()
                        //sharedPreferences.edit().putString("filepath", callInfo.getFilePath()).apply();
                        updatecallStatus()
                    } else if (strmoduletype == "DialPad" || settings.getUserRole()
                            .equals(CallHistory.strCRE) || settings.getUserRole()
                            .equals(CallHistory.strCre) || settings.getUserRole()
                            .equals(CallHistory.strcre) && callInfo!!.callType == "OUTGOING"
                    ) {
                        callInfo!!.filePath =
                            settings.getDealerId() + "/" + settings.fileNameWithUniqueId
                        callInfo!!.fileSize = ""
                        val file =
                            File(DEFAULT_STORAGE_LOCATION + "/" + settings.fileNameWithUniqueId)
                        try {
                            if (file.exists() && file.isFile) {
                                val fileSize = getFileSizeKiloBytes(file)
                                callInfo!!.fileSize = fileSize
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
                        }, 2000) //2 seconds
                    } else {
                        if (strmoduletype == CallHistory.serviceAdvisor || strmoduletype == CallHistory.serviceAdvisors && callInfo!!.callType == "OUTGOING") {
                        } else if (strmoduletype == CallHistory.strpsf && callInfo!!.callType == "OUTGOING") {
                        } else if (settings.getUserRole()
                                .equals(CallHistory.strCRE) || settings.getUserRole()
                                .equals(CallHistory.strCre) || settings.getUserRole()
                                .equals(CallHistory.strcre) && callInfo!!.callType == "OUTGOING"
                        ) {
                        } else if (settings.getUserRole()
                                .equals(CallHistory.strDRIVER) || settings.getUserRole()
                                .equals(CallHistory.strDriver) || settings.getUserRole()
                                .equals(CallHistory.strdriver) && callInfo!!.callType == "OUTGOING"
                        ) {
                        } else if (strmoduletype == "DialPad") {
                        }
                    }
                }
                Log.d(TAG, "calldispositionactivity has started!")
                AsyncSendFile().execute()
            }
            Log.d(
                TAG,
                "callinfo before set cache$callInfo"
            )
            (context!!.applicationContext as WyzConnectApp).setCallInfoCache(callInfo)
            setCallInfoCache()
        }
        managedCursor.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CALL_LOG) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "Call Log permission has now been granted.")
            } else {
                Log.i(TAG, "Call Log permission was NOT granted.")
            }
        }
    }

    fun updatecallStatus() {
        val iscallmadereference =
            FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.DRIVERPICKUP_URL)
        val pushkey = arrayOfNulls<String>(1)
        iscallmadereference.orderByChild(CallHistory.strvehicleNumber)
            .equalTo(callInfo!!.vehicleNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (child in dataSnapshot.children) {
                        Log.d("children count", dataSnapshot.children.toString())
                        Log.d("User key", child.key!!)
                        pushkey[0] = child.key
                        Log.d("User ref", child.ref.toString())
                        Log.d("User val", child.value.toString())
                    }
                    if (dataSnapshot.childrenCount == 0L || dataSnapshot.value == null) {
                        println("Sorry number is not present in schedule call")
                    } else {
                        if (callInfo!!.callDuration != "0") {
                            Toast.makeText(
                                context,
                                "CallDuration is " + callInfo!!.callDuration,
                                Toast.LENGTH_SHORT
                            ).show()
                            iscallmadereference.child(pushkey[0]!!).child("isCallMade")
                                .setValue("yes")
                            iscallmadereference.child(pushkey[0]!!).child("mediaFile").setValue(
                                callInfo!!.mediaFile
                            )
                            insertHistory()
                        } else {
                            Toast.makeText(context, R.string.durationzero, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                override fun onCancelled(DatabaseError: DatabaseError) {}
            })
    }

    fun insertHistory() {
        val VehicleStatusUpdateRef =
            FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.DRIVERPICKED_URL)
        val lastRef = VehicleStatusUpdateRef.push()
        currentDateTime()
        changestatus(callInfo)
        callInfo!!.interactionDate = dateString
        callInfo!!.interactionTime = timeString
        callInfo.setIsCallMade("yes")
        if (callInfo!!.callDuration != "0") {
            lastRef.setValue(callInfo)
        }
        callInfo = null
        (context!!.applicationContext as WyzConnectApp).setCallInfoCache(null)
    }

    fun currentDateTime() {
        val sdf_date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdf_time = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        dateString = sdf_date.format(Date())
        timeString = sdf_time.format(Date())
    }

    fun changestatus(callInfo: CallInfo?) {
        if (callInfo!!.trackStatus == "Cancelled") {
            callInfo.trackStatus = CallHistory.STATUS_CANCELLED.toString()
        } else if (callInfo.trackStatus == "PickupStarted") {
            callInfo.trackStatus = CallHistory.STATUS_STARTED.toString()
        } else if (callInfo.trackStatus == "ReachedPoint") {
            callInfo.trackStatus = CallHistory.STATUS_REACHED_POINT.toString()
        } else if (callInfo.trackStatus == "VehiclePicked") {
            callInfo.trackStatus = CallHistory.STATUS_PICKEDUP.toString()
        } else if (callInfo.trackStatus == "Completed") {
            callInfo.trackStatus = CallHistory.STATUS_DROPPED.toString()
        }
    }

    fun setCallInfoCache() {
        Log.d(TAG, "Moduletype is $strmoduletype")
        if (strmoduletype == "DialPad" || strqueuetype == "WEB" || strqueuetype == "Web" || strqueuetype == "web") {
            (context!!.applicationContext as WyzConnectApp).setCallInfoCache(null)
        }
    }

    inner class AsyncSendFile :
        AsyncTask<Void?, Void?, Void?>() {
        var flag = false
        protected override fun doInBackground(vararg voids: Void): Void? {
            // flag = ftpFiles();
            flag = UploadFileUsingRetroFit(settings.fileNameWithUniqueId)
            /*if(flag)
            {
                try {
                    deleteCallRecording(settings.getFileNameWithUniqueId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/return null
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

    fun UploadFileUsingRetroFit(uniqueId: String?): Boolean {
        prefs = context!!.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
        val dealerUrl = prefs.getString("DealerUrl", "")
        service = APIClient.getClient(dealerUrl).create(
            UploadService::class.java
        )
        val file = File(DEFAULT_STORAGE_LOCATION + "/" + uniqueId)
        if (file.exists()) {
            try {
                if (file.exists()) {
                    AudioStatusList = ArrayList<AudioDataResponse>()
                    val name: RequestBody
                    val reqFile = RequestBody.create(parse.parse("audio/*"), file)
                    val body: Part = createFormData.createFormData("audio", file.name, reqFile)
                    if (uniqueId == null) {
                        name = RequestBody.create(parse.parse("text/plain"), "0")
                    } else {
                        name = RequestBody.create(parse.parse("text/plain"), uniqueId)
                        try {
                            val req: Call<ArrayList<AudioDataResponse>> =
                                service!!.postImage(body, name)
                            req.enqueue(object : Callback<ArrayList<AudioDataResponse>> {
                                override fun onResponse(
                                    call: Call<ArrayList<AudioDataResponse>>,
                                    response: Response<ArrayList<AudioDataResponse>>
                                ) {
                                    try {
                                        println("Response :" + response.message())
                                        AudioStatusList = response.body()
                                        val yourFilePath = DEFAULT_STORAGE_LOCATION + "/" + uniqueId
                                        val file = File(yourFilePath)
                                        val result = file.delete()
                                        context!!.sendBroadcast(
                                            Intent(
                                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                                Uri.fromFile(file)
                                            )
                                        )
                                        val status: String = response.body()!![0].getStatus()
                                        val audioFile: String = response.body()!![0].getFilename()
                                        uploadFirebaseFlag = true
                                        try {
                                            if (AudioStatusList!!.size > 0) {
                                                dbhelper = DBHelper(context)
                                                for (data in AudioStatusList) {
                                                    dbhelper!!.updateSyncStatus(audioFile, status)
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
                                    call: Call<ArrayList<AudioDataResponse>>,
                                    t: Throwable
                                ) {
                                    t.printStackTrace()
                                    println("Error Response :" + t.message)
                                    uploadFirebaseFlag = false
                                }
                            })
                            println("uploadFirebaseFlag : " + uploadFirebaseFlag)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (fe: Exception) {
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

    companion object {
        private var settings: CommonSettings? = null
        private var context: Context? = null
        private const val TAG = "CallLogContentObserver"
        const val DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
        var dbhelper: DBHelper? = null
        var service: UploadService? = null
        var uploadFirebaseFlag = false
        private const val PERMISSION_REQUEST_CALL_LOG = 1
        private var dateString: String? = null
        private var timeString: String? = null
        private const val HOSTNAME = "137.59.201.20"
        private const val USERNAME = "wyzaudio"
        private const val PASSWORD = "audio@123"
        private const val PORT = "2121"
        private var UPLOAD_PATH: String? = null
        private fun getFileSizeKiloBytes(file: File): String {
            val f = (file.length() / 1024).toFloat()
            return String.format("%.2f", f) + " kb"
        }

        @Throws(IOException::class)
        fun deleteCallRecording(filename: String) {
            val file = File(DEFAULT_STORAGE_LOCATION + "/" + filename)
            if (file.exists()) {
                val deleted = file.delete()
                Toast.makeText(context, "Deleted Successfullly", Toast.LENGTH_SHORT).show()
                // Toast.makeText(mContext, String.valueOf(deleted), Toast.LENGTH_SHORT).show();
            }
        }
    }
}