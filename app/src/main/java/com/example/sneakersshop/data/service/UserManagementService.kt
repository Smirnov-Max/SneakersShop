package com.example.sneakersshop.data.service

import com.example.sneakersshop.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpnZ3F3Y25zeGFxYmltcW1vdW5lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjkwNzg0ODgsImV4cCI6MjA4NDY1NDQ4OH0.HU7FgG_dO295WX3XVllol0p9B41Oj1FcaFzjnn7NWk8"

data class ProfileDto(
    val id: String?,
    val user_id: String?,
    val photo: String?,
    val firstname: String?,
    val lastname: String?,
    val address: String?,
    val phone: String?
)

data class ProfileCreationRequest(
    val user_id: String,
    val firstname: String = "",
    val lastname: String = "",
    val address: String = "",
    val phone: String = "",
    val photo: String? = null
)

data class FavouriteDto(
    val id: String?,
    val product_id: String?,
    val user_id: String?
)

data class ProductDto(
    val id: String,
    val title: String,
    val category_id: String?,
    val cost: Double,
    val description: String,
    val is_best_seller: Boolean?
)

interface UserManagementService {

    // ---------- Аутентификация ----------

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): Response<SignUpResponse>

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body signInRequest: SignInRequest): Response<SignInResponse>

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/verify")
    suspend fun verifyOTP(@Body verifyOtpRequest: VerifyOtpRequest): Response<VerifyOtpResponse>

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/recover")
    suspend fun recoverPassword(@Body body: Map<String, String>): Response<Any>

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/resend")
    suspend fun resendOTP(@Body body: Map<String, String>): Response<Any>

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @PUT("auth/v1/user")
    suspend fun changePassword(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String>
    ): Response<Any>

    // ---------- Профили ----------

    @Headers("apikey: $API_KEY")
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<ProfileDto>>

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("rest/v1/profiles")
    suspend fun createProfile(
        @Header("Authorization") authHeader: String,
        @Body profile: ProfileCreationRequest
    ): Response<ProfileDto>

    @Headers(
        "apikey: $API_KEY",
        "Content-Type: application/json",
        "Prefer: resolution=merge-duplicates"
    )
    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    // ---------- Загрузка фото в Storage ----------
    @Multipart
    @Headers("apikey: $API_KEY")
    @POST("storage/v1/object/avatars/{fileName}")
    suspend fun uploadAvatar(
        @Header("Authorization") authHeader: String,
        @Path("fileName") fileName: String,
        @Part file: MultipartBody.Part
    ): Response<Any>

    // ---------- Товары ----------

    @Headers("apikey: $API_KEY")
    @GET("rest/v1/products")
    suspend fun getProducts(
        @Header("Authorization") authHeader: String,
        @Query("select") select: String = "*"
    ): List<ProductDto>

    // ---------- Избранное ----------

    @Headers("apikey: $API_KEY")
    @GET("rest/v1/favourite")
    suspend fun getFavourites(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "id,product_id,user_id"
    ): List<FavouriteDto>

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("rest/v1/favourite")
    suspend fun addFavourite(
        @Header("Authorization") authHeader: String,
        @Body body: FavouriteRequest
    ): Response<Unit>

    @Headers("apikey: $API_KEY")
    @DELETE("rest/v1/favourite")
    suspend fun deleteFavourite(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String,
        @Query("product_id") productIdFilter: String
    ): Response<Unit>
}