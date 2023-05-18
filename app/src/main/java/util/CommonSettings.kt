package util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import entity.CallInfo
import util.CommonSettings
import java.io.File
import java.util.ArrayList
import java.util.HashMap

object CommonSettings {
    var isCheckForeground = false
    private var AudioSource = 0
    private var SYNC_SOURCE: String? = ""
    var regToken = ""
    private var UserFirstName: String? = null
    private var UserLastName: String? = null
    private var DealerId: String? = null
    private var DealerName: String? = null
    private var Jwtoken: String? = null
    private var longitude: String? = null
    private var latitude: String? = null
    private var appContext: Context? = null
    var userStatus: String? = null
    var userMessage: String? = null
    private var UserLocationStatus: String? = null
    var isDisabeUser = false
    private var UserName: String? = null
    private var UserId: String? = null
    private var UserRole: String? = null
    private var UserEmail: String? = null
    var imeiNumber: String? = null
    private var isAuthenticated = false
    var isServerReachable = false
    var userPhoneNumber: String? = null
    private var appData: SharedPreferences? = null
    private var appDataEditor: SharedPreferences.Editor? = null
    var callInfoCache: CallInfo? = null
    private val ServiceMessage: String? = null
    var isSavemessagestatus = false
    var insuranceMessages: List<HashMap<String, String>>? = null
    var pSFMessages: List<HashMap<String, ArrayList<String>>>? = null
    private var scheduleextraminute = 0
    private var Insuranceextraminute = 0
    private var RegIdUpdated: String? = null
    var uniqueidForCallSync: String? = null
    var fileNameWithUniqueId: String? = null
    var file: File? = null
    var userMobileNo: String? = null
    var vehicleRegNo: String? = null
    var deviceManufacturer: String? = null

    init {
        isAuthenticated = false
    }
    val cm = appContext!!.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) // connected to the internet
        {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                //Toast.makeText(appContext, "Connected to "+activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                haveConnectedWifi = true
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile datas
                //Toast.makeText(appContext, "Connected to "+activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                haveConnectedMobile = true
            }
        } else  // not connected to the internet
        {
        }
        return haveConnectedWifi || haveConnectedMobile
    }

    companion object {
        var instance: CommonSettings? = null
            get() {
                if (field == null) {
                    field = CommonSettings()
                    return field
                }
                return field
            }
            private set
    }
}