package retrofitinterface

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class AudioDataResponse {
    @SerializedName("fileName")
    @Expose
    var filename: String,
    @SerializedName("status")
    @Expose
    var status: String
    ) {
        constructor() : this("", "")
        constructor(status: String) : this("", status)
    }
}