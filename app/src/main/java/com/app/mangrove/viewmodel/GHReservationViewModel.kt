package com.app.mangrove.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.mangrove.R
import com.app.mangrove.model.*
import com.app.mangrove.util.Constants
import com.app.mangrove.util.showAlertDialog
import com.mindorks.kotlinFlow.data.api.ApiHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GHReservationViewModel(private val apiHelper: ApiHelper, application: Application): BaseViewModel(application) {

    private val availUnitsResponse = MutableLiveData<APIResult<UnitsResponse>>()
    private val ghResponse = MutableLiveData<APIResult<APIResponse>>()
    private val resortsResponse = MutableLiveData<APIResult<ResortResponse>>()

    fun getCustomerResorts(token: String) {
        viewModelScope.launch {
            resortsResponse.postValue(APIResult.loading(null))
            apiHelper.getCustomerResorts(token)
                .catch { e ->
                    resortsResponse.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    resortsResponse.postValue(APIResult.success(it))
                }
        }
    }




    fun getAvailableUnits(token: String, resort_id: String, reservation_date: String,chk_out: String)
    {
        viewModelScope.launch {
            availUnitsResponse.postValue(APIResult.loading(null))
            apiHelper.getAvailableUnits(token,resort_id,reservation_date,chk_out)
                .catch { e ->
                    availUnitsResponse.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    availUnitsResponse.postValue(APIResult.success(it))

                }
        }
    }


    fun addGHReservation(token: String,
                         guests: GHReservationRequest,context: Context
    ) {

        viewModelScope.launch {
            ghResponse.postValue(APIResult.loading(null))
            apiHelper.addGuestReservation(token,guests)
                .catch { e ->
                    ghResponse.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    ghResponse.postValue(APIResult.success(it))

                }
        }
    }

    fun getUnits(): LiveData<APIResult<UnitsResponse>> {
        return availUnitsResponse
    }

    fun getGHResponse(): LiveData<APIResult<APIResponse>> {
        return ghResponse
    }

    fun getResorts(): LiveData<APIResult<ResortResponse>> {
        return resortsResponse
    }
}