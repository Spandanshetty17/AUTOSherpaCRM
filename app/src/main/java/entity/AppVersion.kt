package entity

import com.google.gson.annotations.SerializedName

data class AppVersion(
    @SerializedName("latestAppVersion")
    var latestAppVersion: String? = null,
    @SerializedName("urlLink")
    var urlLink: String? = null
)