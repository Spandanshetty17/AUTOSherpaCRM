package service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import com.demo.autosherpa3.WyzConnectApp
import com.demo.autosherpa3.SplashActivity.MY_PREFS_NAME
import database.DBHelper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrointerface.APIClient
import retrointerface.AudioDataResponse
import util.CommonSettings
import java.io.File
import java.util.*


class AysncRecordSync : Service(){
    private val DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
    private lateinit var dbHelper: DBHelper
    private lateinit var service: UploadService
    private lateinit var settings: CommonSettings
    private lateinit var prefs: SharedPreferences
    private lateinit var context: Context
    private var uploadFirebaseFlag = false
    private var AudioStatusList = ArrayList<AudioDataResponse>()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        settings = (application as WyzConnectApp).settings
        context = this
        if (mTimer != null) {
            mTimer?.cancel()
        } else {
            mTimer = Timer()
        }
        mTimer?.scheduleAtFixedRate(TimeDisplayTimerTask(), 0, 300000)
    }

    private var mHandler = Handler(Looper.getMainLooper())
    private var mTimer: Timer? = null

    inner class TimeDisplayTimerTask : TimerTask() {
        override fun run() {
            mHandler.post {
                syncRecord()
            }
        }
    }

    private fun syncRecord() {
        var files: ArrayList<String>
        try {
            dbHelper = DBHelper(context)
            files = dbHelper.getNotSyncedFile()
            if (files.size > 0) {
                for (filename in files) {
                    UploadFileUsingRetroFit(filename)
                }
                println("syncRecord : ${files.size}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun UploadFileUsingRetroFit(uniqueId: String): Boolean {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val dealerUrl = prefs.getString("DealerUrl", "")
        service = APIClient.getClient(dealerUrl).create(UploadService::class.java)
        AudioStatusList = ArrayList()
        val name: RequestBody
        val file = File("$DEFAULT_STORAGE_LOCATION/$uniqueId")
        try {
            if (file.exists()) {
                val reqFile = RequestBody.create(MediaType.parse("audio/*"), file)
                val body = MultipartBody.Part.createFormData(
                    "audio",
                    "${settings.dealerId}/${file.name}",
                    reqFile
                )
                name = if (uniqueId == null) {
                    RequestBody.create(MediaType.parse("text/plain"), "0")
                } else {
                    RequestBody.create(MediaType.parse("text/plain"), uniqueId)
                }
                try {
                    val req: Call<ArrayList<AudioDataResponse>> = service.postImage(body, name)
                    req.enqueue(object : Callback<ArrayList<AudioDataResponse>> {
                        override fun onResponse(
                            call: Call<ArrayList<AudioDataResponse>>,
                            response: Response<ArrayList<AudioDataResponse>>
                        ) {
                            try {
                                println("Response : ${response.message()}")
                                AudioStatusList = response.body() ?: ArrayList()
                                uploadFirebaseFlag = true
                                try {
                                    if (!AudioStatusList.isEmpty()) {
                                        dbHelper = DBHelper(context)
                                        for (data in AudioStatusList) {
                                            dbHelper.updateSyncStatus(uniqueId, "1")
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
                            println("Error Response : ${t.message}")
                            uploadFirebaseFlag = false
                        }
                    })
                    println("uploadFirebaseFlag : $uploadFirebaseFlag")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    } catch (Exception fe)
    {
        fe.printStackTrace()
    }
    return uploadFirebaseFlag
}
}
