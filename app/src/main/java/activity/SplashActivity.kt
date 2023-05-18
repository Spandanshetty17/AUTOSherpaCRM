package activity

import activity.CREActivity
import android.app.ActivityManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
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
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDex
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import receiver.NetworkReceiver
import service.JobSchedulerService
import service.YourService
import util.CommonSettings
import android.support.multidex.MultiDexApplication

/**
 * Created By 1526 on 1/10/2020
 */

public class SplashActivity : AppCompatActivity() : MultiDexApplication{
    var mServiceIntent: Intent? = null
    private var mYourService: YourService? = null
    private var mNetworkReceiver: BroadcastReceiver? = null

    const val MY_PREFS_NAME = "UpdateSharedPref"
    private var settings: CommonSettings? = null
    const val TAG = "WyzSplash"
    const val JOB_ID = 1
    var latestVersion = ""
    var updateUrl = ""
    var currentVersion = ""
    var appVersion = ""
    var id_splash: RelativeLayout? = null
    const val FCM_TOKEN = "FCMToken"

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MultiDex.install(this)
        hideStatusBar()
        settings = CommonSettings.instance
        Log.i(TAG, "Authentication status :" + settings.isAuthenticated())
        setContentView(R.layout.activity_splash)
        currentVersion = getAppVersion(this@SplashActivity)
        checkUpdate()
        mNetworkReceiver = NetworkReceiver()
        id_splash = findViewById(R.id.id_splash)
        registerNetworkBroadcastForNougat()
        mYourService = YourService()
        if (FirebaseDatabase.getInstance() == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }
        mServiceIntent = Intent(this, mYourService.getClass())
        if (!isMyServiceRunning(mYourService.getClass())) {
            startService(mServiceIntent)
        }
        val jobInfo: JobInfo
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (task.isComplete) {
                val token = task.result
                settings.regToken = token
            }
        }
        jobInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobInfo.Builder(
                JOB_ID, ComponentName(
                    this,
                    JobSchedulerService::class.java
                )
            )
                .setPeriodic((15 * 60 * 1000).toLong(), (7 * 60 * 1000).toLong())
                .build()
        } else {
            JobInfo.Builder(
                JOB_ID, ComponentName(
                    this,
                    JobSchedulerService::class.java
                )
            )
                .setPeriodic((3 * 60 * 1000).toLong())
                .build()
        }
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler?.schedule(jobInfo)
    }

    private fun checkUpdate() {
        latestVersion = "1.0"
        val editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putString("LatestVersion", latestVersion)
        editor.putString("CurrentVersion", currentVersion)
        editor.apply()
        val handler = Handler()
        handler.postDelayed({
            if (settings.isDisabeUser) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        R.string.userdisabled,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                }
            } else if (!settings.isAuthenticated()) {
                val intent = Intent(applicationContext, ActivityPhoneAuth::class.java)
                intent.putExtra("LatestVersion", latestVersion)
                intent.putExtra("CurrentVersion", currentVersion)
                startActivity(intent)
            } else if (settings.getUserRole().equalsIgnoreCase("CRE")) {
                if (latestVersion.equals(
                        currentVersion,
                        ignoreCase = true
                    )
                ) {
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

    //hide status bar
    private fun hideStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        val actionBar = supportActionBar
        actionBar?.hide()
    }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    //checking service is running or not
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }

    private fun getAppVersion(context: Context): String {
        var result = ""
        try {
            result = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
            result = result.replace("[a-zA-Z]|-".toRegex(), "")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, e.message!!)
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

    protected fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

}