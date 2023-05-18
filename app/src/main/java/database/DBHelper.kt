package service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import com.autosherpas.autosherpa3.SplashActivity.MY_PREFS_NAME
import com.autosherpas.autosherpa3.WyzConnectApp
import database.DBHelper
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrointerface.APIClient
import retrointerface.AudioDataResponse
import util.CommonSettings
import java.io.File
import java.util.*

/**
 * Created by w-1524 on 22-JAN-20.
 */
class AysncRecordSync : Service() {
    var dbHelper: DBHelper? = null
    var c: Context = this
    private val mHandler = Handler()
    private var mTimer: Timer? = null
    val DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
    var dbhelper: DBHelper? = null
    var service: UploadService? = null
    var uploadFirebaseFlag = false
    private var settings: CommonSettings? = null
    var prefs: SharedPreferences? = null
    private var context: Context? = null
    var AudioStatusList: ArrayList<AudioDataResponse>? = null
    fun syncRecord() {
        val files: ArrayList<String>
        try {
            dbHelper = DBHelper(c)
            files = dbHelper.getNotSyncedFile()
            if (files.size > 0) {
                for (filename in files) {
                    UploadFileUsingRetroFit(filename)
                }
                println("syncRecord : " + files.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        // cancel if already existed
        settings = (application as WyzConnectApp).getSettings()
        context = this
        if (mTimer != null) {
            mTimer!!.cancel()
        } else {
            // recreate new
            mTimer = Timer()
        }
        // schedule task
        mTimer!!.scheduleAtFixedRate(TimeDisplayTimerTask(), 0, 300000)
    }

    internal inner class TimeDisplayTimerTask : TimerTask() {
        override fun run() {
            // run on another thread
            mHandler.post {}
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun UploadFileUsingRetroFit(uniqueId: String?): Boolean {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val dealerUrl = prefs.getString("DealerUrl", "")
        service = APIClient.getClient(dealerUrl).create(UploadService::class.java)
        AudioStatusList = ArrayList<AudioDataResponse>()
        val name: RequestBody
        val file = File("$DEFAULT_STORAGE_LOCATION/$uniqueId")
        try {
            if (file.exists()) {
                val reqFile = RequestBody.create(parse.parse("audio/*"), file)
                val body: Part = createFormData.createFormData(
                    "audio",
                    settings.getDealerId() + "/" + file.name,
                    reqFile
                )
                if (uniqueId == null) {
                    name = RequestBody.create(parse.parse("text/plain"), "0")
                } else {
                    name = RequestBody.create(parse.parse("text/plain"), uniqueId)
                    try {
                        val req: Call<ArrayList<AudioDataResponse>> =
                            service!!.postImage(body, name)
                        req.enqueue(object : Callback<ArrayList<AudioDataResponse>?> {
                            override fun onResponse(
                                call: Call<ArrayList<AudioDataResponse>?>,
                                response: Response<ArrayList<AudioDataResponse>?>
                            ) {
                                try {
                                    println("Response :" + response.message())
                                    AudioStatusList = response.body()
                                    uploadFirebaseFlag = true
                                    try {
                                        if (!AudioStatusList!!.isEmpty()) {
                                            dbhelper = DBHelper(context)
                                            for (data in AudioStatusList) {
                                                dbhelper.updateSyncStatus(uniqueId, "1")
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
                                call: Call<ArrayList<AudioDataResponse>?>,
                                t: Throwable
                            ) {
                                t.printStackTrace()
                                println("Error Response :" + t.message)
                                uploadFirebaseFlag = false
                            }
                        })
                        println("uploadFirebaseFlag : $uploadFirebaseFlag")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (fe: Exception) {
            fe.printStackTrace()
        }
        return uploadFirebaseFlag
    }
}