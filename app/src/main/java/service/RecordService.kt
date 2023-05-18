package service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import activity.WyzConnectApp
import database.DBHelper
import database.DatabaseManager
import util.CallStatusSemaphoreLock
import util.CommonSettings
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created By 1524 on 10/1/2020
 */
class RecordService : Service(), MediaRecorder.OnInfoListener,
    MediaRecorder.OnErrorListener {
    private var isRecording = false
    private var recording: File? = null
    var settings: CommonSettings? = null
    var dbHelper: DBHelper? = null
    var audiomanager: AudioManager? = null
    private fun makeOutputFile(fileNamePrefix: String): File? {
        val dir = File(DEFAULT_STORAGE_LOCATION)

        // test dir for existence and writeability
        if (!dir.exists()) {
            try {
                dir.mkdirs()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "RecordService::makeOutputFile unable to create directory $dir: $e"
                )
                return null
            }
        } else {
            if (!dir.canWrite()) {
                Log.e(
                    TAG,
                    "RecordService::makeOutputFile does not have write permission for directory: $dir"
                )
                return null
            }
        }
        return try {
            File.createTempFile(fileNamePrefix + "_", ".mp3", dir)
        } catch (e: IOException) {
            Log.e(
                TAG,
                "RecordService::makeOutputFile unable to create temp file in $dir: $e"
            )
            null
        }
    }

    override fun onCreate() {
        super.onCreate()
        settings = (application as WyzConnectApp).getSettings()
        recorder = MediaRecorder()
        Log.i(TAG, "onCreate created MediaRecorder object")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            3,
            Notification()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "wyzmindz.wyzcrm9002"
        val channelName = "Recorder Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(4, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val c = applicationContext
        dbHelper = DBHelper(c)
        DatabaseManager.initializeInstance(dbHelper)
        Log.e(
            TAG,
            "RecordService::onStartCommand called while isRecording:$isRecording"
        )
        val cal = Calendar.getInstance()
        val df = SimpleDateFormat("yyyyMMddHHmmss")
        val formattedDate = df.format(cal.time)
        val audiosource: Int = settings.getAudioSource()
        Log.e(TAG, "audiosource $audiosource")
        val audioformat = MediaRecorder.OutputFormat.MPEG_4
        val uniqueId: String = settings.uniqueidForCallSync
        val symbol = "_"
        val num: String = settings.userMobileNo
        val number = num.replace("+", "_")
        recording =
            makeOutputFile(settings.getUserId() + symbol + settings.vehicleRegNo + symbol + number + symbol + uniqueId + "_" + formattedDate)
        Log.e(TAG, "FileName" + settings.fileNameWithUniqueId)
        if (recording == null) {
            recorder = null
            Log.e("null record", "Record is null")
            // return; //return 0;
        } else {
            settings.fileNameWithUniqueId = recording!!.name
            Log.e(TAG, " recording PATH " + recording!!.name)
            settings.file = recording
            Log.e(TAG, "FilePath of the recording: " + recording!!.absolutePath)
            audiomanager = getSystemService(AUDIO_SERVICE) as AudioManager
            Log.e(
                TAG,
                "RecordService will config MediaRecorder with audiosource: $audiosource audioformat: $audioformat"
            )
            try {
                recorder!!.reset()
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Log.e(TAG, "L" + "L")
                    recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                    Log.e(TAG, "In M" + "In M")
                    Toast.makeText(this, "6.0", Toast.LENGTH_SHORT).show()
                    recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                    Log.e(TAG, "NNNN" + "NNNN")
                    Toast.makeText(this, "7.0", Toast.LENGTH_SHORT).show()
                    val i = audiomanager!!.getRouting(2)
                    audiomanager!!.mode = AudioManager.MODE_IN_CALL
                    audiomanager!!.isMicrophoneMute = false
                    audiomanager!!.isSpeakerphoneOn = true
                    var j = audiomanager!!.getStreamMaxVolume(0)
                    if (j < 0) j = 1
                    val k = j / 2 + 1
                    audiomanager!!.setStreamVolume(0, k, 0)
                    audiomanager!!.setRouting(2, 11, 15)
                    recorder!!.setAudioSamplingRate(8000)
                    recorder!!.setAudioEncodingBitRate(12200)
                    recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION) //1-MIC
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                    Log.e(TAG, "N_MR1" + "N_MR1")
                    recorder!!.reset()
                    recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION) //1-MIC
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    recorder!!.setAudioChannels(2)
                    try {
                        val i = audiomanager!!.getRouting(2)
                        audiomanager!!.mode = AudioManager.MODE_IN_CALL
                        audiomanager!!.isSpeakerphoneOn = true
                        var j = audiomanager!!.getStreamMaxVolume(0)
                        if (j < 0) {
                            j = 1
                        }
                        audiomanager!!.setStreamVolume(0, j / 2 + 1, 0)
                        audiomanager!!.setRouting(2, 11, 15)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                    Log.e(TAG, "O" + "O")
                    recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    recorder!!.setAudioChannels(2)
                    try {
                        val i = audiomanager!!.getRouting(2)
                        audiomanager!!.mode = AudioManager.MODE_IN_CALL
                        // audiomanager.setMicrophoneMute(false);
                        audiomanager!!.isSpeakerphoneOn = true
                        var j = audiomanager!!.getStreamMaxVolume(0)
                        if (j < 0) {
                            j = 1
                        }
                        audiomanager!!.setStreamVolume(0, j / 2 + 1, 0)
                        audiomanager!!.setRouting(2, 11, 15)
                    } catch (e: Exception) {
                        Log.e("Record is ", "Record is coming " + e.message)
                        val message = e.message
                        e.printStackTrace()
                    }
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
                    Log.e(TAG, "O" + "O(8.1.0)")
                    try {
                        recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        recorder!!.setAudioChannels(2)
                        try {
                            val i = audiomanager!!.getRouting(2)
                            audiomanager!!.mode = AudioManager.MODE_IN_CALL
                            audiomanager!!.isSpeakerphoneOn = true
                            var j = audiomanager!!.getStreamMaxVolume(0)
                            if (j < 0) {
                                j = 1
                            }
                            audiomanager!!.setStreamVolume(0, j / 2 + 1, 0)
                            audiomanager!!.setRouting(2, 11, 15)
                        } catch (e: Exception) {
                            val message = e.message
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        val message = e.message
                        e.printStackTrace()
                    }
                } else {
                    Log.e(TAG, "ELSE" + "ELSE ")
                    recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                }
                recorder!!.setOutputFile(recording!!.absolutePath)
                Log.e(TAG, "set file: " + recording!!.absolutePath)
                //recorder.setMaxDuration(msDuration); //1000); // 1 seconds
                //recorder.setMaxFileSize(bytesMax); //1024*1024); // 1KB
                val errorListener =
                    MediaRecorder.OnErrorListener { arg0: MediaRecorder?, arg1: Int, arg2: Int ->
                        Log.e(
                            TAG,
                            "OnErrorListener $arg1,$arg2"
                        )
                    }
                recorder!!.setOnErrorListener(errorListener)
                val infoListener =
                    MediaRecorder.OnInfoListener { arg0: MediaRecorder?, arg1: Int, arg2: Int ->
                        Log.e(
                            TAG,
                            "OnInfoListener $arg1,$arg2"
                        )
                    }
                recorder!!.setOnInfoListener(infoListener)
                try {
                    recorder!!.prepare()
                    Thread.sleep(2000)
                } catch (e: IOException) {
                    val message = e.message
                    Log.e(
                        TAG,
                        "RecordService::onStart() IOException attempting recorder.prepare()\n"
                    )
                    recorder = null
                    //return; //return 0; //START_STICKY;
                }
                Log.e(TAG, "recorder.prepare() returned")
                try {
                    recorder!!.start()
                    isRecording = true
                    Log.e(TAG, "recorder.start() returned")
                } catch (e: Exception) {
                    val message = e.message
                    e.printStackTrace()
                    Log.e("Record falied", "Record is : " + e.message)
                    /*
                    Toast.makeText(getApplicationContext(), "In Recording : " + e.getMessage(), Toast.LENGTH_SHORT).show();
*/setAudioFunctions(recorder)
                }
                //updateNotification(true);
                val fileName = recording!!.absolutePath
                try {
                    CallStatusSemaphoreLock.getInstance().setUniquieId(fileName)
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                        applicationContext
                    )
                    sharedPreferences.edit().putString("recording", "ONGOING").apply()
                    dbHelper.insertDetails(recording!!.name, "0", recording!!.absolutePath, "0")
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread interrupted while trying to obtain lock")
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                Log.e(TAG, "RecordService::onStart caught unexpected exception", e)
                recorder = null
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private inner class bufData private constructor() {
        var buffer = 0
        var sampling = 0
    }

    fun setAudioFunctions(recorder: MediaRecorder?) {
        var recorder = recorder
        recorder!!.reset()
        recorder.release()
        recorder = null
        if (recorder == null) {
            recorder = MediaRecorder()
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
        Log.d(TAG, "set encoder default")
        recorder.setOutputFile(recording!!.absolutePath)
        Log.d(TAG, "set file: " + recording!!.absolutePath)
        try {
            recorder.prepare()
        } catch (e: IOException) {
            Log.e(TAG, "RecordService::onStart() IOException attempting recorder.prepare()\n")
            recorder = null
            //return; //return 0; //START_STICKY;
        }
        Log.e(TAG, "recorder.prepare() returned")
        try {
            recorder.start()
            isRecording = true
            Log.e(TAG, "recorder.start() returned")
        } catch (e: Exception) {
            e.printStackTrace()
            /*
            Toast.makeText(getApplicationContext(), "In Recording : " + e.getMessage(), Toast.LENGTH_SHORT).show();
*/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != recorder) {
            Log.e(TAG, "RecordService::onDestroy calling recorder.release()")
            isRecording = false
            recorder!!.release()
        }
    }

    // methods to handle binding the service
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        return false
    }

    override fun onRebind(intent: Intent) {}

    // MediaRecorder.OnInfoListener
    override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
        Log.e(
            TAG,
            "RecordService got MediaRecorder onInfo callback with what: $what extra: $extra"
        )
        isRecording = false
    }

    // MediaRecorder.OnErrorListener
    override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
        Log.e(
            TAG,
            "RecordService got MediaRecorder onError callback with what: $what extra: $extra"
        )
        isRecording = false
        mr.release()
    }

    companion object {
        private const val TAG = "RecordService"
        const val DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
        var recorder: MediaRecorder? = null
    }
}