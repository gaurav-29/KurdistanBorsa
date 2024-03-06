package net.comelite.kurdistanborsa.api

import net.comelite.kurdistanborsa.model.UserProfile
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("webservices.php?")
    fun getUser(@Query("function") func: String, @Query("id") id: String): Call<UserProfile>
}