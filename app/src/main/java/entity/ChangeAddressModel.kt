package entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ChangeAddressModel {
    @SerializedName("message")
    @Expose
    var message: String? = null
}