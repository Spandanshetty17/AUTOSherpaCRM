package com.demo.autosherpa3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.ActivityManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import activity.CREActivity
import receiver.NetworkReceiver
import service.JobSchedulerService
import service.YourService
import util.CommonSettings


class SplashActivity : AppCompatActivity() {

    private var mServiceIntent: Intent? = null
    private var mYourService: YourService? = null
    private var mNetworkReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MultiDex.install(this)
        hideStatusBar()
        settings = CommonSettings.getInstance()
        Log.i(TAG, "Authentication status :" + settings.isAuthenticated())
        setContentView(R.layout.activity_splash)
        currentVersion = getAppVersion(this)
        checkUpdate()
        mNetworkReceiver = NetworkReceiver()
        id_splash = findViewById(R.id.id_splash)
        registerNetworkBroadcastForNougat()
        mYourService = YourService()
        if (FirebaseDatabase.getInstance() == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }
        mServiceIntent = Intent(this, mYourService!!.javaClass)
        if (!isMyServiceRunning(mYourService!!.javaClass)) {
            startService(mServiceIntent)
        }

        val jobInfo: JobInfo
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                settings.setRegToken(token)
            }
        }
        jobInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobInfo.Builder(JOB_ID, ComponentName(this, JobSchedulerService::class.java))
                .setPeriodic(15 * 60 * 1000, 7 * 60 * 1000)
                .build()
        } else {
            JobInfo.Builder(JOB_ID, ComponentName(this, JobSchedulerService::class.java))
                .setPeriodic(3 * 60 * 1000)
                .build()
        }
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo)
        }
    }

    private fun checkUpdate() {
        latestVersion = "1.0"
        val editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putString("LatestVersion", latestVersion)
        editor.putString("CurrentVersion", currentVersion)
        editor.apply()
        Handler().postDelayed({
            if (settings.isDisabeUser()) {
                runOnUiThread {
                    Toast.makeText(applicationContext, R.string.userdisabled, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else if (!settings.isAuthenticated()) {
                val intent = Intent(applicationContext, ActivityPhoneAuth::class.java)
                intent.putExtra("LatestVersion", latestVersion)
                intent.putExtra("CurrentVersion", currentVersion)
                startActivity(intent)
            } else if (settings.getUserRole().equals("CRE", ignoreCase = true)) {
                if (latestVersion.equals(currentVersion, ignoreCase = true)) {
                    val intent = Intent(baseContext, CREActivity::class.java)
                    intent.putExtra("LatestVersion", latestVersion)
                    intent.putExtra("CurrentVersion", currentVersion)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                } else {
                    FirebaseAuth.getInstance().signOut()
                    settings.setIsAuthenticated(false)
                    startActivity(Intent(this@SplashActivity, SplashActivity::class.java))
                    finish()
                }
            }
        }, 2000)
    }

    private fun hideStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        supportActionBar?.hide()
    }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString())
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString())
        return false
    }

    private fun getAppVersion(context: Context): String {
        var result = ""
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            result = packageInfo.versionName
            result = result.replace("[a-zA-Z]|-".toRegex(), "")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, e.message)
        }
        return result
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onDestroy() {
        unregisterNetworkChanges()
        stopService(mServiceIntent)
        Log.i("MAINACT", "onDestroy!")
        super.onDestroy()
    }

    private fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}