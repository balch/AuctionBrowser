package com.balch.auctionbrowser.test

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

fun <T> uninitialized(): T = null as T

data class CaptorResult<S, out T>(val verifier: T, val argumentCaptor: ArgumentCaptor<S>)

/**
 * Workaround for Mockito vs Kotlin null enforcement issues.
 *
 * Adapted from https://stackoverflow.com/a/35366060
 */
fun <S,T> makeCaptor(mock: T, clazz: Class<S> ): CaptorResult<S,T> {
    val captor = ArgumentCaptor.forClass(clazz)

    val verifier = Mockito.verify(mock)
    captor.capture()

    return CaptorResult(verifier, captor)
}

fun <T> anyArg(): T {
    Mockito.any<T>()
    return uninitialized()
}