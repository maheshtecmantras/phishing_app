
import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


object APIClient {
    private var retrofit: Retrofit? = null
    fun getClient(context: Context?, isPassAuth: Boolean): Retrofit? {
        val client: OkHttpClient

        class TokenAuthenticator : Authenticator {
            @Throws(IOException::class)
            override fun authenticate(route: Route?, response: Response): Request? {
                val sharedPreferences: SharedPreferences = context?.getSharedPreferences("token",
                    Service.MODE_PRIVATE
                )!!
                val token: String = sharedPreferences.getString("token","").toString()
//                val token: String = "eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRlc3QyM0BnbWFpbC5jb20iLCJqdGkiOiIwOTExYjFmNy05NTMwLTRhODktYjc4My1mYzczMzgwZTA4YzQiLCJVc2VybmFtZSI6InRlc3QyMyIsIkVtYWlsIjoidGVzdDIzQGdtYWlsLmNvbSIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL25hbWVpZGVudGlmaWVyIjoiNTQ3MTI0YzYtMWM3Ni00YzFlLWE5MzQtYWFhMmQ0ZTljZmRjIiwiVXNlcklkIjoiNTQ3MTI0YzYtMWM3Ni00YzFlLWE5MzQtYWFhMmQ0ZTljZmRjIiwiaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS93cy8yMDA4LzA2L2lkZW50aXR5L2NsYWltcy9yb2xlIjoiVXNlciIsIkRldmljZUlkIjoiMTA4IiwiZXhwIjoxNjk1MDQyNzc0fQ.JdMCf2CeRW-TPJN_j2p0ZbOwUphKucdU42FZsU1Oaa4"
                Log.d("TAG", "getClient: ...image file....${token}")

                return  response.request.newBuilder().header("Authorization", "Bearer $token")
                    .header("Content-Type","application/json")
                    .build()
//                return if (response.request.header("Authorization") != null) {
//                    null
//                } else response.request.newBuilder().header("Authorization", "Bearer $token")
//                    .build()
            }
        }
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        client = OkHttpClient.Builder().connectionPool(ConnectionPool(5, 60, TimeUnit.MINUTES)).readTimeout(60,TimeUnit.MINUTES).connectTimeout(60,TimeUnit.MINUTES).retryOnConnectionFailure(false)
            .authenticator(TokenAuthenticator()).build()

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //                .baseUrl("https://testratapi.azurewebsites.net/api/Gallery/AddGallery")
//            .baseUrl("https://192.168.64.235:8003/")
            .baseUrl("https://npphaseapi1.azurewebsites.net/")
            .client(client)
            .build()
        return retrofit
    }
}