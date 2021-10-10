package com.app.mangrove.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.mangrove.R
import com.app.mangrove.model.*
import com.app.mangrove.util.showAlertDialog
import com.mindorks.kotlinFlow.data.api.ApiHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class UnitApplicationViewModel(private val apiHelper: ApiHelper, application: Application): BaseViewModel(application) {

    private val pckgResponse = MutableLiveData<APIResult<PackageResponse>>()
    private val services = MutableLiveData<APIResult<ResortResponse>>()
    private val roles = MutableLiveData<APIResult<ResortResponse>>()
    private val resorts = MutableLiveData<APIResult<ResortResponse>>()
    private val registration = MutableLiveData<APIResult<APIResponse>>()


    fun getUserRoles() {
        viewModelScope.launch {
            roles.postValue(APIResult.loading(null))
            apiHelper.getUserRoles()
                .catch { e ->
                    roles.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    roles.postValue(APIResult.success(it))
                }
        }
    }

    fun addUnitMember(request: RegisterMemberRequest) {
        viewModelScope.launch {
            try {
                registration.postValue(APIResult.loading(null))
                apiHelper.addUnitMember(request)

                    .collect {
                        registration.postValue(APIResult.success(it))
                    }
            } catch (e: HttpException) {
                val jObjError = JSONObject(e.response()?.errorBody()?.string())

                if (jObjError.has("errors")) {

                    if(jObjError.has("errors")) {
                        registration.postValue(
                            APIResult.error(
                                jObjError.getJSONObject("errors").toString(),
                                null
                            )
                        )
                    }
                }
                else{
                    registration.postValue(
                        APIResult.error(e.message(),
                            null
                        )
                    )
                }

            }
        }
    }
        fun getGuestResorts() {
            viewModelScope.launch {
                resorts.postValue(APIResult.loading(null))
                apiHelper.getGuestResorts()
                    .catch { e ->
                        resorts.postValue(APIResult.error(e.toString(), null))
                    }
                    .collect {
                        resorts.postValue(APIResult.success(it))
                    }
            }
        }

        fun getResortServices(resortId: String) {
            viewModelScope.launch {
                services.postValue(APIResult.loading(null))
                apiHelper.getServices(resortId)
                    .catch { e ->
                        services.postValue(APIResult.error(e.toString(), null))
                    }
                    .collect {
                        services.postValue(APIResult.success(it))
                    }
            }
        }


        fun getPackages(resortId: String, roleId: String) {
            viewModelScope.launch {
                pckgResponse.postValue(APIResult.loading(null))
                apiHelper.getMemberPackages(resortId, roleId)
                    .catch { e ->
                        pckgResponse.postValue(APIResult.error(e.toString(), null))
                    }
                    .collect {
                        pckgResponse.postValue(APIResult.success(it))

                    }

            }
        }

    fun getAddMemberResponse(): LiveData<APIResult<APIResponse>> {
        return registration
    }

    fun getPackages(): LiveData<APIResult<PackageResponse>> {
        return pckgResponse
    }

    fun getRoles(): LiveData<APIResult<ResortResponse>> {
        return roles
    }

    fun getResorts(): LiveData<APIResult<ResortResponse>> {
        return resorts
    }

    fun getServices(): LiveData<APIResult<ResortResponse>> {
        return services
    }
}