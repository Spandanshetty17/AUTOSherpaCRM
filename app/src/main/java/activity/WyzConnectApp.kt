package activity

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.FirebaseDatabase
import com.jaredrummler.android.device.DeviceName
import entity.CallInfo
import util.CommonSettings


/**
 * Created by wyz-1524 on 11-1-2020.
 */

class WyzConnectApp : MultidexApplication {
    private const val TWITTER_KEY = "85ZAID1RVE2dpTXevtgTgS7yr"
    private const val TWITTER_SECRET = "RvzgZq0eBMbllz9lDXBad75MXWlKcLMSLFiuvyGW6e8e6tdm3P"
    const val TAG = "WyzConnectApp"
    const val FCM_TOKEN = "FCMToken"
    var calledAlready = false
    private var context: Context? = null
    private const val PERMISSIONS_REQUEST_READ_PHONE_STATE = 0

    @get:Synchronized
    var instance: WyzConnectApp? = null
        private set

    var settings: CommonSettings? = null
        private set
    var callInfoCache: CallInfo? = null
    var isFromMakeCall = false
    private val mAuth: FirebaseAuth? = null
    private val mAuthListener: AuthStateListener? = null
    private val imeiNumber: String? = null
    override fun onCreate() {
        super.onCreate()
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }
        context = applicationContext
        instance = this
        settings = CommonSettings.instance
        settings.attachToContext(this)
        callInfoCache = null
        val authenticationFlag: Boolean = settings.isAuthenticated()
        Log.i(TAG, "Authentication Flag: $authenticationFlag")
        settings.deviceManufacturer = DeviceName.getDeviceInfo(this).manufacturer
        Log.i(TAG, "Manufacturer : " + settings.deviceManufacturer)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}