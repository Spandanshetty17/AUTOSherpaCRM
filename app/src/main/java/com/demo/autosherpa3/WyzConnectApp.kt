package com.demo.autosherpa3

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jaredrummler.android.device.DeviceName
import entity.CallInfo
import util.CommonSettings

class WyzConnectApp : Application() {
    private val TWITTER_KEY = "85ZAID1RVE2dpTXevtgTgS7yr"
    private val TWITTER_SECRET = "RvzgZq0eBMbllz9lDXBad75MXWlKcLMSLFiuvyGW6e8e6tdm3P"
    private val TAG = "WyzConnectApp"

    private var settings: CommonSettings? = null
    private var callInfoCache: CallInfo? = null

    fun isFromMakeCall(): Boolean {
        return fromMakeCall
    }

    fun setFromMakeCall(fromMakeCall: Boolean) {
        this.fromMakeCall = fromMakeCall
    }

    private var fromMakeCall = false

    fun getSettings(): CommonSettings? {
        return settings
    }

    fun getCallInfoCache(): CallInfo? {
        return callInfoCache
    }

    fun setCallInfoCache(currentCallInfo: CallInfo?) {
        callInfoCache = currentCallInfo
    }


    val FCM_TOKEN = "FCMToken"
    var calledAlready = false


    var mAuth: FirebaseAuth? = null
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private val context: Context? = null

    private val PERMISSIONS_REQUEST_READ_PHONE_STATE = 0
    private val imeiNumber: String? = null
    private val wyzConnectApp: WyzConnectApp? = null


    override fun onCreate() {
        super.onCreate()

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }


        WyzConnectApp.context = applicationContext

        WyzConnectApp.wyzConnectApp = this



        settings = CommonSettings.getInstance()
        settings!!.attachToContext(this)
        callInfoCache = null
        val authenticationFlag = settings!!.isAuthenticated
        Log.i(TAG, "Authentication Flag: $authenticationFlag")
        settings!!.deviceManufacturer = DeviceName.getDeviceInfo(this).manufacturer
        Log.i(TAG, "Manufacturer : " + settings!!.deviceManufacturer)

        val i = Intent(applicationContext,WyzConnectApp::class.java)
        startActivity(i)


    }


    @Synchronized
    fun getInstance(): WyzConnectApp? {
        return WyzConnectApp.wyzConnectApp
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}