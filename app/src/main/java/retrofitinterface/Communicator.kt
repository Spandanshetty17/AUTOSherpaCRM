package retrofitinterface

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Communicator {
    private const val TAG = "Communicator"
    private const val SERVER_URL = "http://atul.wyzmindz.com:9002"
    fun loginPost(username: String, password: String) {
        // Here a logging interceptor is created
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        // The logging interceptor will be added to the http client
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        // The Retrofit builder will have the client attached, in order to get connection logs
        val retrofit = Retrofit.Builder()
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(SERVER_URL)
            .build()
        val service = retrofit.create(SaveRegistrationId::class.java)

        val call = service.post("login", username, password)

        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                BusProvider.getInstance().post(ServerEvent(response.body()))
                Log.e(TAG, "Success")
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                // handle execution failures like no internet connectivity

                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(ErrorEvent(-2, t.message))
            }
        })
    }

}