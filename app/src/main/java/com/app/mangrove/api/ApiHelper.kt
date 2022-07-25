package com.mindorks.kotlinFlow.data.api

import com.app.mangrove.model.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiHelper {

    fun login(
        user_name: String?,
        password: String?,
        type: String?
    ): Flow<APIResponse>

    fun getProfile(token: String): Flow<APIResponse>

    fun getVisitorPackages(
        token: String,
        date_time: String,
        gender: String,
        resort_id: String
    ): Flow<VisitorPackage>

    fun addVisitor(
        token: String,
        visitor: VisitorRequest,
    ): Flow<APIResponse>


    fun addUnitMember(request: RegisterMemberRequest): Flow<APIResponse>

    fun getMemberPackages(service_id: String, role_id: String): Flow<PackageResponse>

    fun getGuestPackages(id: String): Flow<PackageResponse>

    fun getServices(id: String): Flow<ResortResponse>

    fun getUserRoles(): Flow<ResortResponse>

    fun addGuestReservation(token: String, visitor: GHReservationRequest): Flow<APIResponse>

    fun getAvailableUnits(
        token: String,
        resort_id: String,
        reservation_date: String,
        check_out_date: String
    ): Flow<UnitsResponse>

    fun getGuestResorts(): Flow<ResortResponse>

    fun getCustomerResorts(token: String): Flow<ResortResponse>

    fun postRequest(token: String, request: RequestBody): Flow<APIResponse>

    fun getFacilityServices(token: String): Flow<ResortResponse>

    fun getFacilityPackages(token: String, serviceId: String): Flow<PackageResponse>

    fun getAllServices(
        token: String,
        per_page: String,
        page: String)
        : Flow<ServiceResponse>

    fun getVisitors(token: String) : Flow<VisitorListResponse>

    fun getNoOfVisitors(token: String,visiting_date_time: String,resort_id: String) : Flow<TotalVisitorsResponse>

}