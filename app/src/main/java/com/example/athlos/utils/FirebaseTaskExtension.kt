package com.example.athlos.utils

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// Aguarda uma Task do Google em uma coroutine, retornando o resultado ou lançando a exceção.
suspend fun <T> Task<T>.await(): T {
    return suspendCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}