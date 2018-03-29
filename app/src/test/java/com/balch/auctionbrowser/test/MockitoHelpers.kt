package com.balch.auctionbrowser.test

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

fun <T> uninitialized(): T = null as T

data class CaptorResult<out T>(val verifier: T, val captors: List<ArgumentCaptor<*>>)

/**
 * Workaround for Mockito vs Kotlin null enforcement issues.
 *
 * Adapted from https://stackoverflow.com/a/35366060
 */
fun <T> makeCaptor(mock: T, vararg clazzes: Class<*>): CaptorResult<T> {

    val captors: List<ArgumentCaptor<*>> = List(clazzes.size,
            { idx -> ArgumentCaptor.forClass(clazzes[idx]) })

    val verifier = Mockito.verify(mock)

    captors.forEach({ it.capture() })

    return CaptorResult(verifier, captors)
}

fun <T> anyArg(): T {
    Mockito.any<T>()
    return uninitialized()
}