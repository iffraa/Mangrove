package com.app.mangrove.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlin.properties.Delegates

class SingleLiveEvent<T> : MutableLiveData<T>() {

    var curUser: Boolean by Delegates.vetoable(false) { property, oldValue, newValue ->
        newValue != oldValue
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer<T> { t ->
            if (curUser) {
                observer.onChanged(t)
                curUser = false
            }
        })
    }

    override fun setValue(t: T?) {

        curUser = true
        super.setValue(t)
    }

    fun call() {
        postValue(null)
    }

}