package com.app.mangrove.model

data class APIResult<out T>(val status: Status, val api_data: T?, val message: String?) {

    companion object {

        fun <T> success(data: T?): APIResult<T> {
            return APIResult(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): APIResult<T> {
            return APIResult(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): APIResult<T> {
            return APIResult(Status.LOADING, data, null)
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
