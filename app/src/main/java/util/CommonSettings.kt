package util

import android.content.Context
import android.content.SharedPreferences
import entity.CallInfo
import java.io.File

class CommonSettings  {

    fun getSYNC_SOURCE(): String? {
        return SYNC_SOURCE}
    }


    fun setSYNC_SOURCE(SYNC_SOURCE: String) {
        SYNC_SOURCE = SYNC_SOURCE
        appDataEditor!!.putString("SYNC_SOURCE", SYNC_SOURCE)
        appDataEditor!!.commit()
    }

private var AudioSource = 0

private var SYNC_SOURCE = ""
private const val regToken = ""

private var UserFirstName: String? = null

private var UserLastName: String? = null

private var DealerId: String? = null

private var DealerName: String? = null

private var Jwtoken: String? = null

private var longitude: String? = null

private var latitude: String? = null

private var appContext: Context? = null

private val instance: CommonSettings? = null

private val UserStatus: String? = null

private val UserMessage: String? = null

private var UserLocationStatus: String? = null

private const val DisableUser = false

private var UserName: String? = null

private var UserId: String? = null

private var UserRole: String? = null

private var UserEmail: String? = null

private val imeiNumber: String? = null

private var isAuthenticated = false

private const val isServerReachable = false

private val userPhoneNumber: String? = null

private var appData: SharedPreferences? = null

private var appDataEditor: SharedPreferences.Editor? = null

private val callInfoCache: CallInfo? = null

private val ServiceMessage: String? = null

private const val savemessagestatus = false

private val InsuranceMessages: List<java.util.HashMap<String, String>>? = null

private val PSFMessages: List<java.util.HashMap<String, java.util.ArrayList<String>>>? = null

private var scheduleextraminute = 0

private var Insuranceextraminute = 0

private var RegIdUpdated: String? = null

private val uniqueidForCallSync: String? = null

private val FileNameWithUniqueId: String? = null

fun getFile(): File? {
    return file
}

fun setFile(file: File) {
    this.file = file
}

private val file: File? = null

private val userMobileNo: String? = null


fun getCallInfoCache(): CallInfo? {
        return callInfoCache
    }

    fun setCallInfoCache(callInfoCache: CallInfo) {
        this.callInfoCache = callInfoCache
        appDataEditor?.putString("callInfoCache", callInfoCache.toString())?.commit()
    }

    fun getDeviceManufacturer(): String? {
        return DeviceManufacturer
    }

    fun setDeviceManufacturer(deviceManufacturer: String) {
        DeviceManufacturer = deviceManufacturer
    }

    fun attachToContext(ctx: Context) {
        appContext = ctx
        appData = appContext!!.getSharedPreferences("WyzConnectMobile", Context.MODE_PRIVATE)
        appDataEditor = appData.edit()
        UserName = appData.getString("UserName", "")!!
        UserEmail = appData.getString("UserEmail", "")!!
        UserId = appData.getString("UserId", "")!!
        UserRole = appData.getString("UserRole", "")!!
        isAuthenticated = appData.getBoolean("isAuthenticated", false)
        longitude = appData.getString("Longitude", "0.0")!!
        latitude = appData.getString("Latitude", "0.0")!!
        //this.QueueType = appData.getString("queueType","");
        UserLocationStatus = appData.getString("UserLocationStatus", "")!!
        SYNC_SOURCE = appData.getString("SYNC_SOURCE", "")!!
        UserFirstName = appData.getString("UserFirstName", "")!!
        UserLastName = appData.getString("UserLastName", "")!!
        DealerId = appData.getString("DealerId", "")!!
        DealerName = appData.getString("DealerName", "")!!
        Jwtoken = appData.getString("Jwtoken", "")!!
        AudioSource = appData.getInt("AudioSource", 1)
        scheduleextraminute = appData.getInt("scheduleextraminute", 25)
        Insuranceextraminute = appData.getInt("Insuranceextraminute", 15)
        RegIdUpdated = appData.getString("RegIdUpdated", "")!!
    }

    var userName: String
        get() = appData?.getString("UserName", "") ?: ""
        set(value) {
            appDataEditor?.putString("UserName", value)
            appDataEditor?.commit()
        }

    var userId: String
        get() = appData?.getString("UserId", "") ?: ""
        set(value) {
            appDataEditor?.putString("UserId", value)
            appDataEditor?.commit()
        }

    var userRole: String
        get() = appData?.getString("UserRole", "") ?: ""
        set(value) {
            appDataEditor?.putString("UserRole", value)
            appDataEditor?.commit()
        }

    var userEmail: String
        get() = appData?.getString("UserEmail", "") ?: ""
        set(value) {
            appDataEditor?.putString("UserEmail", value)
            appDataEditor?.commit()
        }

    var isAuthenticated: Boolean
        get() = appData?.getBoolean("isAuthenticated", false) ?: false
        set(value) {
            appDataEditor?.putBoolean("isAuthenticated", value)
            appDataEditor?.commit()
        }

    var isServerReachable: Boolean
        get() = appData?.getBoolean("isServerReachable", false) ?: false
        set(value) {
            appDataEditor?.putBoolean("isServerReachable", value)
            appDataEditor?.commit()
        }

    var imeiNumber: String
        get() = appData?.getString("imeiNumber", "") ?: ""
        set(value) {
            appDataEditor?.putString("imeiNumber", value)
            appDataEditor?.commit()
        }

    var userPhoneNumber: String
        get() = appData?.getString("userPhoneNumber", "") ?: ""
        set(value) {
            appDataEditor?.putString("userPhoneNumber", value)
            appDataEditor?.commit()
        }

    var longitude: String
        get() = appData?.getString("Longitude", "0.0") ?: "0.0"
        set(value) {
            appDataEditor?.putString("Longitude", value)
            appDataEditor?.commit()
        }

    var latitude: String
        get() = appData?.getString("Latitude", "0.0") ?: "0.0"
        set(value) {
            appDataEditor?.putString("Latitude", value)
            appDataEditor?.commit()
        }

    var userFirstName: String
        get() = appData?.getString("UserFirstName", "") ?: ""
        set(value) {
            appDataEditor?.putString("UserFirstName", value)
            appDataEditor?.commit()
        }

    var userLastName: String
        get() = appData?.getString("UserLastName", "") ?: ""
        set(value) {
            appDataEditor?.putString("UserLastName", value)
            appDataEditor?.commit()
        }

    var dealerId: String
        get() = appData?.getString("DealerId", "") ?: ""
        set(value) {
            appDataEditor?.putString("DealerId", value)
            appDataEditor?.commit()
        }

    var dealerName: String
        get() = appData?.getString("DealerName", "") ?: ""
        set(value) {
            appDataEditor?.putString("DealerName", value)
            appDataEditor?.commit()
        }

    fun getJwtoken(): String? {
        return Jwtoken
    }

    fun setJwtoken(jwtoken: String) {
        Jwtoken = jwtoken
        appDataEditor!!.putString("Jwtoken", jwtoken)
        appDataEditor!!.commit()
    }

    var InsuranceMessages: List<HashMap<String, String>>? = null
        get() = field
        set(value) {
            field = value
        }

    var PSFMessages: List<HashMap<String, ArrayList<String>>>? = null
        get() = field
        set(value) {
            field = value
        }

    var scheduleextraminute: Int = 0
        get() = field
        set(value) {
            field = value
            appDataEditor.putInt("scheduleextraminute", value)
            appDataEditor.commit()
        }

    var Insuranceextraminute: Int = 0
        get() = field
        set(value) {
            field = value
            appDataEditor.putInt("Insuranceextraminute", value)
            appDataEditor.commit()
        }

    var RegIdUpdated: String? = null
        get() = field
        set(value) {
            field = value
            appDataEditor.putString("RegIdUpdated", value)
            appDataEditor.commit()
        }

    fun haveNetworkConnection(): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false

        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                //Toast.makeText(appContext, "Connected to "+activeNetwork.typeName, Toast.LENGTH_SHORT).show();
                haveConnectedWifi = true
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile datas
                //Toast.makeText(appContext, "Connected to "+activeNetwork.typeName, Toast.LENGTH_SHORT).show();
                haveConnectedMobile = true
            }
        } else {
            // not connected to the internet
        }
        return haveConnectedWifi || haveConnectedMobile
    }
