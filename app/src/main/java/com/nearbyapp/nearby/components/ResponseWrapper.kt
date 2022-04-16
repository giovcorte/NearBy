package com.nearbyapp.nearby.components

sealed class ResponseWrapper<out T> {
    data class Success<out T>(val value: T): ResponseWrapper<T>()
    data class Error(val error: Status): ResponseWrapper<Nothing>()
}
