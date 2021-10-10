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
import com.app.mangrove.util.SharedPreferencesHelper
import com.app.mangrove.util.SingleLiveEvent
import com.app.mangrove.util.showAlertDialog
import com.mindorks.kotlinFlow.data.api.ApiHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class VisitorInviteViewModel(private val apiHelper: ApiHelper, application: Application): BaseViewModel(application) {

    private val visitor_response = MutableLiveData<APIResult<APIResponse>>()
    private val packages_response = SingleLiveEvent<APIResult<VisitorPackage>>()

    fun getPackages(token:String,
                    date_time: String,
                    gender: String,
                    resort_id: String) {
        viewModelScope.launch {
            packages_response.postValue(APIResult.loading(null))
            apiHelper.getVisitorPackages(token,
                date_time,
                gender,
                resort_id)
                .catch { e ->
                    e.message?.let { it1 -> Log.i("excep", it1) }
                    packages_response.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    Log.i("collect", it.message)
                    packages_response.postValue(APIResult.success(it))

                }

        }
    }


    fun addVisitors(token:String, visitors: VisitorRequest) {
        viewModelScope.launch {
            visitor_response.postValue(APIResult.loading(null))
            apiHelper.addVisitor(token,visitors)
                .catch { e ->
                    e.message?.let { it1 -> Log.i("excep", it1) }
                    visitor_response.postValue(APIResult.error(e.toString(), null))
                }
                .collect {
                    Log.i("collect", it.message)
                    visitor_response.postValue(APIResult.success(it))

                }

        }
    }

    fun getVisitorResponse(): LiveData<APIResult<APIResponse>> {
        return visitor_response
    }

    fun getPackages(): LiveData<APIResult<VisitorPackage>> {
        return packages_response
    }

}