package service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import database.DBHelper
import database.DatabaseManager

override fun onCreate() {
    super.onCreate()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(1, Notification())
    deletecallrecordings()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun startMyOwnForeground() {
    val NOTIFICATION_CHANNEL_ID = "example.permanence"
    val channelName = "Background Service"
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
    startForeground(2, notification)
}

override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    dbHelper = DBHelper(c)
    DatabaseManager.initializeInstance(dbHelper)
    dbHelper.delete7DaysOldAudioRecords()
    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)
    dbHelper = DBHelper(c)
    dbHelper.deleteRecordingFiles()
    startTimer()
    return START_STICKY
}

override fun onDestroy() {
    super.onDestroy()
    stoptimertask()

    val broadcastIntent = Intent()
    broadcastIntent.action = "restartservice"
    broadcastIntent.setClass(this, Restarter::class.java)
    this.sendBroadcast(broadcastIntent)
}

private lateinit var timer: Timer
private lateinit var timerTask: TimerTask

private fun startTimer() {
    timer = Timer()
    timerTask = object : TimerTask() {
        override fun run() {
            Log.i("Count", "=========  " + (counter++))
        }
    }
    timer.schedule(timerTask, 1000, 300000)
}

private fun stoptimertask() {
    timer.cancel()
}

override fun onBind(intent: Intent): IBinder? {
    return null
}

private fun deletecallrecordings() {
    val newFile = File(DEFAULT_STORAGE_LOCATION)
    var callRecords = emptyArray<File>()
    if (newFile.isDirectory) {
        callRecords = newFile.listFiles() ?: emptyArray()
    }
    for (file in callRecords) {
        if (file.exists()) {
            val time = Calendar.getInstance()
            time.add(Calendar.DAY_OF_YEAR, -28)
            val lastModified = Date(file.lastModified())
            if (lastModified.before(time.time)) {
                val isDeleted = file.delete()
                if (isDeleted) Log.d("deleted file :", file.delete().toString())
            }
        }
    }
}