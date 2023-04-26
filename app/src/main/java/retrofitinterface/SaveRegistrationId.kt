package retrofitinterface

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface SaveRegistrationId {
    //This method is used for "POST"
    @FormUrlEncoded
    @POST("/api.php")
    fun post(
        @Field("method") method: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<ServerResponse>

    //This method is used for "GET"
    @GET("/updateUserAuthentication")
    fun get(
        @Query("phoneIMEINo") phoneIMEINo: String,
        @Query("registrationId") registrationId: String
    ): Call<ServerResponse>
}