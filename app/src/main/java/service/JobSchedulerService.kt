package service

import com.demo.autosherpa3.SplashActivity.MY_PREFS_NAME
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import com.demo.autosherpa3.WyzConnectApp
import java.io.File
import java.util.ArrayList
import database.DBHelper
import database.DatabaseManager
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrointerface.APIClient
import retrointerface.AudioDataResponse
import util.CommonSettings


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerService : JobService() {
    private var service: UploadService? = null
    var DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
    private var audioStatusList: ArrayList<AudioDataResponse>? = null
    private var uploadFirebaseFlag = false
    private var dbhelper: DBHelper? = null
    private var settings: CommonSettings? = null
    private var prefs: SharedPreferences? = null
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        if (CommonSettings.getInstance().haveNetworkConnection()) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            //syncAudioRecord()
        } else {
            Log.i("Internet", "No Internet")
        }
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

    private inner class JobTask(private val jobService: JobService) : AsyncTask<JobParameters, Void, JobParameters>() {
        override fun doInBackground(vararg params: JobParameters): JobParameters {
            for (i in 1..10) {
                Log.e("number", "num$i")
            }
            return params[0]
        }

        override fun onPostExecute(jobParameters: JobParameters) {
            jobService.jobFinished(jobParameters, false)
        }
    }

    fun syncAudioRecord() {
        var files: ArrayList<String>
        try {
            settings = (applicationContext as WyzConnectApp).settings
            dbhelper = DBHelper(this)
            DatabaseManager.initializeInstance(dbhelper)
            files = dbhelper!!.notSyncedFileAll
            if (!files.isEmpty()) {
                for (filename in files) {
                    UploadFileUsingRetroFit(filename)
                }
                Log.d("syncAudioRecord", files.size.toString())
            } else {
                Log.d("No files to upload", "No files to upload")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    fun UploadFileUsingRetroFit(uniqueId: String?): Boolean {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val dealerUrl = prefs!!.getString("DealerUrl", "")
        service = APIClient.getClient(dealerUrl).create(UploadService::class.java)
        dbhelper = DBHelper(this)
        val file = File(DEFAULT_STORAGE_LOCATION + "/" + uniqueId)
        if (file.exists()) {
            try {
                if (file.exists()) {
                    audioStatusList = ArrayList()
                    val name: RequestBody
                    val reqFile = RequestBody.create(MediaType.parse("audio/*"), file)
                    val body = MultipartBody.Part.createFormData("audio", settings!!.dealerId + "/" + file.name, reqFile)
                    name = if (uniqueId == null) {
                        RequestBody.create(MediaType.parse("text/plain"), "0")
                    } else {
                        RequestBody.create(MediaType.parse("text/plain"), uniqueId)
                        try {
                            val req = service!!.postImage(body, name)
                            req.enqueue(object : Callback<ArrayList<AudioDataResponse>> {
                            }

                                    override fun onResponse(call: Call<ArrayList<AudioDataResponse>>, response: Response<ArrayList<AudioDataResponse>>) {
                                try {
                                    println("Response : " + response.message())
                                    val audioStatusList = response.body()
                                    audioStatusList?.let {
                                        val status = it[0].status
                                        val file = it[0].filename
                                        uploadFirebaseFlag = true
                                        try {
                                            if (audioStatusList.size > 0) {
                                                val dbhelper = DBHelper(applicationContext)
                                                for (data in audioStatusList) {
                                                    dbhelper.updateSyncStatus(file, status)
                                                }
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                                    override fun onFailure(call: Call<ArrayList<AudioDataResponse>>, t: Throwable) {
                                t.printStackTrace()
                                println("Error Response : " + t.message)
                                uploadFirebaseFlag = false
                            }
                        })
                        println("uploadFirebaseFlag : $uploadFirebaseFlag")
                    }
                }
            } catch (fe: FileNotFoundException) {
                fe.printStackTrace()
            }
        }
        return uploadFirebaseFlag
    }
}
