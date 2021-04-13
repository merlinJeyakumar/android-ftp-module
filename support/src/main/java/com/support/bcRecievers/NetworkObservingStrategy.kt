package com.support.bcRecievers

import android.content.Context
import io.reactivex.rxjava3.core.Observable

interface NetworkObservingStrategy {

    fun observeNetworkConnectivity(context: Context): Observable<Connectivity>

    fun onError(message: String, exception: Exception)
}