package service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.demo.autosherpa3.WyzConnectApp
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import database.DBHelper
import database.DatabaseManager
import util.CallStatusSemaphoreLock
import util.CommonSettings

class RecordService : Service(), MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    companion object {
        private const val TAG = "RecordService"
        private const val DEFAULT_STORAGE_LOCATION = "/sdcard/wyzcallrecorder"
    }

    private var isRecording = false
    private var recording: File? = null
    private lateinit var settings: CommonSettings
    private lateinit var dbHelper: DBHelper
    private lateinit var audiomanager: AudioManager

    override fun onCreate() {
        super.onCreate()
        settings = (application as WyzConnectApp).settings
        MediaRecorder().also { recorder ->
            recorder.run {
                Log.i(TAG, "onCreate created MediaRecorder object")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground()
                } else {
                    startForeground(3, Notification())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "wyzmindz.wyzcrm9002"
        val channelName = "Recorder Service"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        Log.e(TAG, "RecordService::onStartCommand called while isRecording:$isRecording")

        val cal = Calendar.getInstance()
        val df = SimpleDateFormat("yyyyMMddHHmmss")
        val formattedDate = df.format(cal.time)

        val audiosource = settings.audioSource
        Log.e(TAG, "audiosource $audiosource")
        val audioformat = MediaRecorder.OutputFormat.MPEG_4

        val uniqueId = settings.uniqueidForCallSync
        val symbol = "_"
        val num = settings.userMobileNo
        val number = num.replace("+", "_")
        recording = makeOutputFile("${settings.userId}$symbol${settings.vehicleRegNo}$symbol$number$symbol$uniqueId_$formattedDate")
        Log.e(TAG, "FileName${settings.fileNameWithUniqueId}")

        recording?.let {
            settings.fileNameWithUniqueId = it.name
            Log.e(TAG, " recording PATH ${it.name}")
            settings.file = it
            Log.e(TAG, "FilePath of the recording: ${it.absolutePath}")

            audiomanager = getSystemService(AUDIO_SERVICE) as AudioManager

            Log.e(TAG, "RecordService will config MediaRecorder with audiosource: $audiosource audioformat: $audioformat")

            try {
                recorder.reset()
                when {
                    android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 -> {
                        Log.e(TAG, "L" + "L")
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    }
                    android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M -> {
                        Log.e(TAG, "In M" + "In M")
                        Toast.makeText(this, "6.0", Toast.LENGTH_SHORT).show()
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    }
                    android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.N -> {
                        Log.e(TAG, "NNNN" + "NNNN")
                        Toast.makeText(this, "7.0", Toast.LENGTH_SHORT).show()
                        val i = audiomanager.getRouting(2)
                        audiomanager.mode = 2
                        audiomanager.isMicrophoneMute = false
                        audiomanager.isSpeakerphoneOn = true
                        var j = audiomanager.getStreamMaxVolume(0)
                        if (j < 0) {
                            j = 1
                        }
                        val k = j / 2 + 1
                        audiomanager.setStreamVolume(0, k, 0)
                        audiomanager.setRouting(2, 11, 15)
                        recorder.setAudioSamplingRate(8000)
                        recorder.setAudioEncodingBitRate(12200)
                        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    }
                    android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 -> {
                        Log.e(TAG, "N_MR1" + "N_MR1")
                        recorder.reset()
                        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        recorder.setAudioChannels(2)
                        try {
                            val i = audiomanager.getRouting(2)
                            audiomanager.mode = 2
                            audiomanager.isSpeakerphoneOn = true
                            var j = audiomanager.getStreamMaxVolume(0)
                            if (j < 0) {
                                j = 1
                            }
                            audiomanager.setStreamVolume(0, j / 2 + 1, 0)
                            audiomanager.setRouting(2, 11, 15)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.O -> {
                        Log.e(TAG, "O" + "O")
                        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        recorder.setAudioChannels(2)
                        try {
                            val i = audiomanager.getRouting(2)
                            audiomanager.mode = 2
                            audiomanager.isSpeakerphoneOn = true
                            var j = audiomanager.getStreamMaxVolume(0)
                            if (j < 0) {
                                j = 1
                            }


                            audioManager.setStreamVolume(0, (j / 2) + 1, 0)
                            audioManager.setRouting(2, 11, 15)
                        } catch (e: Exception) {
                            val message = e.message
                            e.printStackTrace()
                        }
                    } else {
                    Log.e(TAG, "ELSE" + "ELSE ")
                    it.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                    it.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    it.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                }

                    it.setOutputFile(recording.absolutePath)
                            Log.e(TAG, "set file: " + recording.absolutePath)

                        val errorListener = MediaRecorder.OnErrorListener { _, what, extra ->
                        Log.e(TAG, "OnErrorListener $what,$extra")
                        //terminateAndEraseFile();
                    }
                    it.setOnErrorListener(errorListener)

                        val infoListener = MediaRecorder.OnInfoListener { _, what, extra ->
                        Log.e(TAG, "OnInfoListener $what,$extra")
                        //terminateAndEraseFile();
                    }
                    it.setOnInfoListener(infoListener)

                        try {
                        it.prepare()
                        Thread.sleep(2000)
                    } catch (e: IOException) {
                    val message = e.message
                    Log.e(TAG, "RecordService::onStart() IOException attempting recorder.prepare()\n")
                    recorder = null
                    //return; //return 0; //START_STICKY;
                }
                        Log.e(TAG, "recorder.prepare() returned")
                    try {
                        it.start()
                        isRecording = true
                        Log.e(TAG, "recorder.start() returned")
                    } catch (e: Exception) {
                        val message = e.message
                        e.printStackTrace()
                        Log.e("Record falied", "Record is : $message")
                        setAudioFunctions(recorder)
                    }

                        val fileName = recording.absolutePath
                    try {
                        CallStatusSemaphoreLock.getInstance().setUniquieId(fileName)
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        sharedPreferences.edit().putString("recording", "ONGOING").apply()
                        dbHelper.insertDetails(recording.name, "0", recording.absolutePath, "0")
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "Thread interrupted while trying to obtain lock")
                        e.printStackTrace()
                    }

                    catch (e: Exception) {
                        Log.e(TAG, "RecordService::onStart caught unexpected exception", e)
                        recorder = null
                    }
                        return super.onStartCommand(intent, flags, startId)
                }

                private class BufData {
                    var buffer = 0
                    var sampling = 0
                }

                fun setAudioFunctions(recorder: MediaRecorder) {
                    recorder.reset()
                    recorder.release()
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
                    Log.d(TAG, "set encoder default")
                    recorder.setOutputFile(recording.absolutePath)
                    Log.d(TAG, "set file: " + recording.absolutePath)
                    try {
                        recorder.prepare()
                    } catch (e: IOException) {
                        Log.e(TAG, "RecordService::onStart() IOException attempting recorder.prepare()\n")
                        recorder.release()
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
                    if (::recorder.isInitialized) {
                        Log.e(TAG, "RecordService::onDestroy calling recorder.release()")
                        isRecording = false
                        recorder.release()
                    }
                }

                // methods to handle binding the service
                override fun onBind(intent: Intent): IBinder? {
                    return null
                }

                override fun onUnbind(intent: Intent): Boolean {
                    return false
                }

                override fun onRebind(intent: Intent) {
                }

                override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
                    Log.e(TAG, "RecordService got MediaRecorder onInfo callback with what: $what extra: $extra")
                    isRecording = false
                }

                override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
                    Log.e(TAG, "RecordService got MediaRecorder onError callback with what: $what extra: $extra")
                    isRecording = false
                    mr.release()
                }
            }