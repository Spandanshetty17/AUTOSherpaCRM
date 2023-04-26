package service

import java.util.ArrayList
import entity.AddressModel
import entity.AppLogin
import entity.AppVersion
import entity.ChangeAddressModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrointerface.AudioDataResponse


interface UploadService {
    @Multipart
    @POST("api/AndroInteraction/processAudio")
    fun postImage(@Part audio: MultipartBody.Part, @Part("name") name: RequestBody): Call<ArrayList<AudioDataResponse>>
    @GET("api/AndroInteraction")
    fun getAppVersion(): Call<AppVersion>

    @POST("api/AndroInteraction/")
    @FormUrlEncoded
    fun getLoginData(
        @Field("phoneNumber") phoneNumber: String,
        @Field("phoneIMEINo") phoneIMEINo: String,
        @Field("registrationId") registrationId: String,
        @Field("latestAppVersion") latestAppVersion: String
    ): Call<AppLogin>
}