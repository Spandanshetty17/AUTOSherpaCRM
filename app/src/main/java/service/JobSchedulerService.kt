package service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.annotation.RequiresApi
import activity.WyzConnectApp
import database.DBHelper
import database.DatabaseManager
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrointerface.APIClient
import retrointerface.AudioDataResponse
import util.CommonSettings
import java.io.File

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerService : JobService() {
    var service: UploadService? = null
    var DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
    var AudioStatusList: ArrayList<AudioDataResponse>? = null
    var uploadFirebaseFlag = false
    var dbhelper: DBHelper? = null
    var settings: CommonSettings? = null
    var prefs: SharedPreferences? = null
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        if (CommonSettings.instance.haveNetworkConnection()) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            //    syncAudioRecord();
        } else {
            Log.i("Internet", "No Internet")
        }
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

    private class JobTask private constructor(private val jobService: JobService) :
        AsyncTask<JobParameters?, Void?, JobParameters>() {
        protected override fun doInBackground(vararg params: JobParameters): JobParameters {
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
        val files: ArrayList<String>
        try {
            settings = (this.applicationContext as WyzConnectApp).getSettings()
            dbhelper = DBHelper(this)
            DatabaseManager.initializeInstance(dbhelper)
            files = dbhelper.getNotSyncedFileAll()
            if (!files.isEmpty()) {
                for (filename in files) {
                    UploadFileUsingRetroFit(filename)
                }
                //System.out.println("syncRecord : " + files.size());
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
        val dealerUrl = prefs.getString("DealerUrl", "")
        service = APIClient.getClient(dealerUrl).create(UploadService::class.java)
        dbhelper = DBHelper(this)
        val file = File("$DEFAULT_STORAGE_LOCATION/$uniqueId")
        if (file.exists()) {
            try {
                if (file.exists()) {
                    AudioStatusList = ArrayList<AudioDataResponse>()
                    val name: RequestBody
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
                                        assert(response.body() != null)
                                        val status: String = response.body()!![0].getStatus()
                                        val file: String = response.body()!![0].getFilename()
                                        uploadFirebaseFlag = true
                                        try {
                                            if (AudioStatusList!!.size > 0) {
                                                dbhelper = DBHelper(applicationContext)
                                                for (data in AudioStatusList) {
                                                    dbhelper.updateSyncStatus(file, status)
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
        }
        return uploadFirebaseFlag
    }
}