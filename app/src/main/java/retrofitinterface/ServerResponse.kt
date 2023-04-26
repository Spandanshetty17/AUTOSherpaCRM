package retrofitinterface

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ServerResponse {

        @SerializedName("success")
        @Expose
        var success: String? = null

        @SerializedName("message")
        @Expose
        var message: String? = null

}