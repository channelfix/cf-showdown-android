package com.example.cf.channelsd.Interfaces

import com.example.cf.channelsd.Data.User
import retrofit2.Call;
import retrofit2.http.*

/**
 * Created by CF on 3/6/2018.
 */
interface LoginInterface {
    @GET("/text")
    @FormUrlEncoded
    fun sendUserInfo(@Field("username") username: String,
                     @Field("email")email: String,
                     @Field("password")password: String,
                     @Field("userType")userType: String): Call<User>
}