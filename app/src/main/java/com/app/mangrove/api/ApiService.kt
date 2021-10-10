package com.mindorks.kotlinFlow.data.api

import com.app.mangrove.model.*
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("customer/login?")
    suspend fun login(
        @Field("user_name") user_name: String?,
        @Field("password") password: String?,
        @Field("user_type") type: String?

    ): APIResponse


    @Headers("Accept: application/json")
    @POST("customer/me?")
    suspend fun getProfile(@Header("Authorization") token: String): APIResponse

    @Headers("Accept: application/json")
    @GET("customer/visitor/packages?")
    suspend fun getVisitorPackages(
        @Header("Authorization") token: String,
        @Query("visiting_date_time") date_time: String,
        @Query("visitor_gender") gender: String,
        @Query("resort_id") resort_id: String
    ): VisitorPackage


    @Headers("Accept: application/json")
    @POST("customer/invite/visitors?")
    suspend fun addVisitor(
        @Header("Authorization") token: String,
        @Body visitor: VisitorRequest
    ): APIResponse


    @Headers("Accept: application/json")
    @POST(" guest/member/register?")
    suspend fun addUnitMember(
        @Body request: RegisterMemberRequest
    ): APIResponse

    @GET("guest/member/services/{service_id}/packages/{role_id}")
    suspend fun getMemberPackages(
        @Path("service_id") service_id: String,
        @Path("role_id") role_id: String
    ): PackageResponse

    @GET("guest/packages/{service_id}")
    suspend fun getGuestPackages(@Path("service_id") service_id: String): PackageResponse

    @GET("guest/services/{resort_id}")
    suspend fun getServices(@Path("resort_id") resort_id: String): ResortResponse

    @Headers("Accept: application/json")
    @GET("guest/member/reg/roles?")
    suspend fun getUserRoles(): ResortResponse

    @Headers("Accept: application/json")
    @POST("customer/guest/reservation?")
    suspend fun addGuestReservation(
        @Header("Authorization") token: String,
        @Body visitor: GHReservationRequest,
    ): APIResponse

    @Headers("Accept: application/json")
    @GET("customer/available/units/{resort_id}")
    suspend fun getAvailableUnits(
        @Header("Authorization") token: String,
        @Path("resort_id") resort_id: String,
        @Query("reservation_date") reservation_date: String,
        @Query("check_out_date") check_out_date: String,

        ): UnitsResponse

    @GET("guest/member/reg/facilities?")
    suspend fun getGuestResorts(): ResortResponse

    @Headers("Accept: application/json")
    @GET("customer/facilities?")
    suspend fun getCustomerResorts(@Header("Authorization") token: String): ResortResponse

    @Headers("Accept: application/json")
    @POST("customer/service/requests")
    suspend fun postServiceRequest(
        @Header("Authorization") token: String,
        @Body request: ServiceRequest
    ): APIResponse

    @Headers("Accept: application/json")
    @GET("customer/services/request_service")
    suspend fun getFacilityServices(@Header("Authorization") token: String): ResortResponse

    @Headers("Accept: application/json")
    @GET("customer/service/request/packages/{service_id}")
    suspend fun getFacilityPackages(
        @Header("Authorization") token: String,
        @Path("service_id") service_id: String
    ): PackageResponse

    @Headers("Accept: application/json")
    @GET("customer/service/requests?page=1&per_page=10")
    suspend fun getAllServices(
        @Header("Authorization") token: String,
        @Query("per_page") per_page: String,
        @Query("page") page: String

        ): ServiceResponse

    @Headers("Accept: application/json")
    @GET("customer/invite/visitors")
    suspend fun getVisitors(@Header("Authorization") token: String): VisitorListResponse
}