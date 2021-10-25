package com.app.mangrove.viewmodel


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.mangrove.model.*
import com.mindorks.kotlinFlow.data.api.ApiHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException

class CreateServiceViewModel(private val apiHelper: ApiHelper, application: Application): BaseViewModel(application) {

    private val pckgResponse = MutableLiveData<APIResult<PackageResponse>>()
    private val serviceResponse = MutableLiveData<APIResult<ResortResponse>>()
    private val addServiceResponse = MutableLiveData<APIResult<APIResponse>>()

    fun addService(token: String, request: RequestBody) {
        viewModelScope.launch {
            try {
                addServiceResponse.postValue(APIResult.loading(null))
                apiHelper.postRequest(token, request)
                    .collect {
                        addServiceResponse.postValue(APIResult.success(it))
                    }
            } catch (e: HttpException) {
                val jObjError = JSONObject(e.response()?.errorBody()?.string())

                if(jObjError.has("errors")) {
                    addServiceResponse.postValue(
                        APIResult.error(
                            jObjError.getJSONObject("errors").toString(),
                            null
                        )
                    )
                }
            }
        }
    }


    fun getPackages(token: String, serviceId: String) {
        viewModelScope.launch {
            pckgResponse.postValue(APIResult.loading(null))
            apiHelper.getFacilityPackages(token,serviceId)
                .catch { e ->
                    pckgResponse.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    pckgResponse.postValue(APIResult.success(it))

                }

        }
    }

    fun getServices(token: String) {
        viewModelScope.launch {
            serviceResponse.postValue(APIResult.loading(null))
            apiHelper.getFacilityServices(token)
                .catch { e ->
                    serviceResponse.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    serviceResponse.postValue(APIResult.success(it))

                }

        }
    }

    fun getServices(): LiveData<APIResult<ResortResponse>> {
        return serviceResponse
    }

    fun getPackages(): LiveData<APIResult<PackageResponse>> {
        return pckgResponse
    }

    fun getServiceCreationResponse(): LiveData<APIResult<APIResponse>> {
        return addServiceResponse
    }
}