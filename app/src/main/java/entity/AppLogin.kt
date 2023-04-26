package entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AppLogin(
    @SerializedName("authenticationStatus")
    @Expose
    var authenticationStatus: String? = null,
    @SerializedName("userId")
    @Expose
    var userId: String? = null,
    @SerializedName("userRole")
    @Expose
    var userRole: String? = null,
    @SerializedName("userFName")
    @Expose
    var userFName: String? = null,
    @SerializedName("userLName")
    @Expose
    var userLName: String? = null,
    @SerializedName("dealerId")
    @Expose
    var dealerId: String? = null,
    @SerializedName("dealerName")
    @Expose
    var dealerName: String? = null,
    @SerializedName("userEmail")
    @Expose
    var userEmail: Any? = null,
    @SerializedName("jwtToken")
    @Expose
    var jwtToken: String? = null,
    @SerializedName("fireBaseUrl")
    @Expose
    var fireBaseUrl: String? = null
)