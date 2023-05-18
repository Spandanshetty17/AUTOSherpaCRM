package retrofitinterface

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class AudioDataResponse {
    @SerializedName("fileName")
    @Expose
    var filename: String
    @SerializedName("status")
    @Expose
    var status: String


    fun AudioDataResponse(filename: String?, status: String?) {
        this.filename = filename!!
        this.status = status!!
    }

    fun getFilename(): String? {
        return filename
    }

    fun setFilename(filename: String?) {
        this.filename = filename!!
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status!!
    }

}
