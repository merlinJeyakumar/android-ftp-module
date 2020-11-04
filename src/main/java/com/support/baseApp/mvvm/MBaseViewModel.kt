package com.support.baseApp.mvvm


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class MBaseViewModel constructor(application: Application) :
    AndroidViewModel(application) {
    private val compositeDisposable = CompositeDisposable()

    var showLoaderDialog: MutableLiveData<Pair<String, Disposable>> = MutableLiveData()
    var showProgressDialog:MutableLiveData<String> = MutableLiveData()
    var hideLoader: MutableLiveData<Unit> = MutableLiveData()

    var toastMessage: MutableLiveData<String> = MutableLiveData()

    var toastResMessage: MutableLiveData<Int> = MutableLiveData()

    fun getContext(): Application {
        return getApplication()
    }

    fun addRxCall(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    private fun clearAllCalls() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.clear()
        }
    }

    abstract fun subscribe()
    open fun unsubscribe() {
        clearAllCalls()
    }
}
