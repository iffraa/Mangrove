package com.app.mangrove.viewmodel


import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.mangrove.model.APIResponse
import com.app.mangrove.model.APIResult
import com.app.mangrove.util.Constants
import com.mindorks.kotlinFlow.data.api.ApiHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(private val apiHelper: ApiHelper, application: Application): BaseViewModel(application) {

    private val api_response = MutableLiveData<APIResult<APIResponse>>()

    fun login(user_name: String,
                      password: String) {
        viewModelScope.launch {
            api_response.postValue(APIResult.loading(null))
                apiHelper.login(user_name,password,Constants.customer)
                    .catch { e ->
                        e.message?.let { it1 -> Log.i("excep", it1) }
                        api_response.postValue(APIResult.error(e.toString(), APIResponse(false,e.toString(),null)))
                    }
                    .collect {
                        Log.i("collect", it.message)
                        api_response.postValue(APIResult.success(it))

                    }

        }
    }

    fun getResponse(): LiveData<APIResult<APIResponse>> {
        return api_response
    }

}



