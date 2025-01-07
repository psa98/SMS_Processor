package com.pon.smsprocessor.api


import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

const val bot_admin_login = "admin@ibronevik.ru"
const val bot_admin_password = "p@ssw0rd"
const val bot_admin_type = "e-mail"


interface Api {

    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    )
    @POST("auth/")
    @FormUrlEncoded
    suspend fun authAsAdmin(
        @Field("login") login: String = bot_admin_login,
        @Field("password") password: String = bot_admin_password,
        @Field("type") type: String = bot_admin_type
    ): Response<AuthResp>


    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    )
    @POST("token/authorized")
    @FormUrlEncoded
    suspend fun getTokenHash(@Field("auth_hash") authHash: String): Response<TokenResponse>


    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    )

    @POST("token/")
    @FormUrlEncoded
    suspend fun checkUser(
        @Field("token") token: String,
        @Field("u_hash") uHash: String,
        @Field("u_a_phone") phone: String
    ): Response<IsRegisteredResponse>

    @POST("register/")
    @FormUrlEncoded
    suspend fun registerNewUser(
        @Field("token") token: String,
        @Field("u_hash") uHash: String,
        @Field("u_name") uName: String,
        @Field("u_phone") uPhone: String,
        @Field("u_role") uRole: String="1",
        @Field("st") st: String = "",
    ): Response<RegistrationResponse>


    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    )
    @POST("drive/")
    @FormUrlEncoded
    suspend fun orderTaxi(
        @Field("token") token: String,
        @Field("u_hash") uHash: String,
        @Field("u_a_id") uaId: String,
        @Field("data") data: String,
        @Field("u_a_role") uaRole: String="1",
        @Field("u_check_state") checkState: String="2",
     ): Response<DriveResponse>


    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    )
    @POST("drive/get/{id}")
    @FormUrlEncoded
    suspend fun cancelTaxi(
        @Path("id") id:Int,
        @Field("token") token: String,
        @Field("u_hash") uHash: String,
        @Field("u_a_id") uaId: String,
        @Field("u_a_role") uaRole: String="4",
        @Field("action") action: String="set_cancel_state",
        @Field("reason") reason: String="",
    ): Response<Any>

    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    )
    @POST("drive/get/{id}")
    @FormUrlEncoded
    suspend fun checkDriveState(
        @Path("id") id:Int,
        @Field("token") token: String,
        @Field("u_hash") uHash: String,

    ): Response<OrderReport>



}
