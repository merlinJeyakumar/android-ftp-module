package com.support.rxJava

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 * Provides different types of schedulers.
 */
object Scheduler { // Prevent direct instantiation.

    fun computation(): @NonNull io.reactivex.rxjava3.core.Scheduler? {
        return Schedulers.computation()
    }

    fun io(): @NonNull io.reactivex.rxjava3.core.Scheduler? {
        return Schedulers.io()
    }

    fun ui(): Scheduler? {
        return AndroidSchedulers.mainThread()
    }

}