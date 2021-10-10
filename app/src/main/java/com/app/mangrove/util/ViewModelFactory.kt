package com.app.mangrove.util

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.mangrove.viewmodel.*
import com.mindorks.kotlinFlow.data.api.ApiHelper

class ViewModelFactory(private val apiHelper: ApiHelper, private val application: Application) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(apiHelper, application) as T
        }

        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(apiHelper, application) as T
        }

        if (modelClass.isAssignableFrom(VisitorInviteViewModel::class.java)) {
            return VisitorInviteViewModel(apiHelper, application) as T
        }

        if (modelClass.isAssignableFrom(UnitApplicationViewModel::class.java)) {
            return UnitApplicationViewModel(apiHelper, application) as T
        }

        if (modelClass.isAssignableFrom(GHReservationViewModel::class.java)) {
            return GHReservationViewModel(apiHelper, application) as T
        }

        if (modelClass.isAssignableFrom(CreateServiceViewModel::class.java)) {
            return CreateServiceViewModel(apiHelper, application) as T
        }

        if (modelClass.isAssignableFrom(ServicesViewModel::class.java)) {
            return ServicesViewModel(apiHelper, application) as T
        }

        if (modelClass.isAssignableFrom(VisitorsViewModel::class.java)) {
            return VisitorsViewModel(apiHelper, application) as T
        }

        throw IllegalArgumentException("Unknown class name")
    }

}