package com.guri.core.model

/**
 * A generic class that holds a value or an error
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Throwable): Result<Nothing> = Error(exception)
        fun <T> loading(): Result<T> = Loading
    }
} 