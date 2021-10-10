package com.app.mangrove.viewmodel


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.mangrove.model.APIResponse
import com.app.mangrove.model.APIResult
import com.mindorks.kotlinFlow.data.api.ApiHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class DashboardViewModel(private val apiHelper: ApiHelper, application: Application): BaseViewModel(application) {

    private val api_response = MutableLiveData<APIResult<APIResponse>>()

    fun getProfile(token: String) {
        viewModelScope.launch {
            api_response.postValue(APIResult.loading(null))
            apiHelper.getProfile(token)
                .catch { e ->
                    api_response.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    api_response.postValue(APIResult.success(it))

                }

        }
    }

    fun getResponse(): LiveData<APIResult<APIResponse>> {
        return api_response
    }


}