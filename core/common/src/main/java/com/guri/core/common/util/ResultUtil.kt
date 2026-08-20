package com.guri.core.common.util

import com.guri.core.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Utility functions for working with Result objects
 */
object ResultUtil {
    
    /**
     * Converts a Flow<T> to a Flow<Result<T>> by handling exceptions and adding loading state
     */
    fun <T> Flow<T>.asResult(): Flow<Result<T>> {
        return this
            .map<T, Result<T>> { Result.success(it) }
            .onStart { emit(Result.Loading) }
            .catch { emit(Result.error(it)) }
    }
    
    /**
     * Executes a suspending operation and wraps the result in a Result object
     */
    suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 