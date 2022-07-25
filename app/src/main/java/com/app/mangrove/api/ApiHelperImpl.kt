package com.mindorks.kotlinFlow.data.api

import com.app.mangrove.model.*
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart

class ApiHelperImpl(private val apiService: ApiService) : ApiHelper {

    override fun login(
        user_name: String?,
        password: String?,
        type: String?
    ) = flow { emit(apiService.login(user_name, password, type)) }

    override fun getProfile(token: String) = flow {
        emit(apiService.getProfile("Bearer " + token))
    }

    override fun getVisitorPackages(
        token: String,
        date_time: String,
        gender: String,
        resort_id: String
    ) = flow {
        emit(apiService.getVisitorPackages("Bearer " + token, date_time, gender, resort_id))
    }

    override fun addVisitor(token: String, visitor: VisitorRequest) = flow {
        emit(apiService.addVisitor("Bearer " + token, visitor))
    }

    override fun addUnitMember(request: RegisterMemberRequest) = flow {
        emit(apiService.addUnitMember(request))
    }

    override fun getMemberPackages(service_id: String, role_id: String) = flow {
        emit(apiService.getMemberPackages(service_id, role_id))
    }

    override fun getGuestPackages(service_id: String) = flow {
        emit(apiService.getGuestPackages(service_id))
    }

    override fun getServices(resort_id: String) = flow {
        emit(apiService.getServices(resort_id))
    }

    override fun getUserRoles() = flow {
        emit(apiService.getUserRoles())
    }

    override fun addGuestReservation(token: String, visitor: GHReservationRequest) = flow {
        emit(apiService.addGuestReservation("Bearer " + token, visitor))
    }

    override fun getAvailableUnits(
        token: String,
        resort_id: String,
        reservation_date: String,
        check_out_date: String
    ) = flow {
        emit(
            apiService.getAvailableUnits(
                "Bearer " + token,
                resort_id,
                reservation_date,
                check_out_date
            )
        )
    }

    override fun getGuestResorts() = flow {
        emit(apiService.getGuestResorts())
    }

    override fun getCustomerResorts(token: String) = flow {
        emit(apiService.getCustomerResorts("Bearer " + token))
    }

    override fun postRequest(token: String, request: RequestBody) = flow {
        emit(apiService.postServiceRequest("Bearer " + token, request))
    }

    override fun getFacilityServices(token:String) = flow {
        emit(apiService.getFacilityServices("Bearer " + token))
    }

    override fun getFacilityPackages(token:String, serviceId:String) = flow {
        emit(apiService.getFacilityPackages("Bearer " + token, serviceId))
    }

    override fun getAllServices(
        token: String,
        per_page: String,
        page: String)
            = flow {
        emit(apiService.getAllServices("Bearer " + token, per_page, page))
    }

    override fun getVisitors(token: String) = flow {
        emit(apiService.getVisitors("Bearer " + token))
    }

    override fun getNoOfVisitors(token: String,visiting_date_time: String,resort_id: String) = flow {
        emit(apiService.getNoOfVisitors("Bearer " + token, visiting_date_time, resort_id))
    }

}