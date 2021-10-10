package com.app.mangrove.viewmodel


import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.mangrove.model.APIResult
import com.app.mangrove.model.ServiceEntry
import com.app.mangrove.model.ServiceResponse
import com.mindorks.kotlinFlow.data.api.ApiHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServicesViewModel(private val apiHelper: ApiHelper, application: Application): BaseViewModel(application) {

    private val servicesList = MutableLiveData<APIResult<ServiceResponse>>()

    fun getServices(token: String,
                    per_page: String,
                    page: String) {
        viewModelScope.launch {
            servicesList.postValue(APIResult.loading(null))
                apiHelper.getAllServices(token, per_page, page)
                    .catch { e ->
                        servicesList.postValue(APIResult.error(e.toString(), null))
                    }
                    .collect {
                        servicesList.postValue(APIResult.success(it))

                    }

        }
    }

    fun getServices(): LiveData<APIResult<ServiceResponse>> {
        return servicesList
    }

}



