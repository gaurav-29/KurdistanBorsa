package net.comelite.kurdistanborsa.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager

object RetrofitClient {

    //Staging URL :

   // private const val BASE_URL = "http://68.183.240.162/staging_currency_exchange/"
//    private const val BASE_URL = "http://68.183.240.162/staging_currency_exchange/"

//>>>>>>> 1e5183d67a832cdc6ce25831ca8cc58c8b615338

    //Production URL :
    private const val BASE_URL = "http://68.183.240.162/currency_exchange/"

    @Volatile private var retrofit: Retrofit? = null

    fun getInstance(): Retrofit =
        retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okClient)
                .build().also { retrofit = it }
        }

    /*private fun createOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts: Array<TrustManager> = arrayOf(NSTManager())
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory)
                .hostnameVerifier { hostname: String?, session: SSLSession? -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }*/

    private val okClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

}