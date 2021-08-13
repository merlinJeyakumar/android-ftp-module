package com.support.baseApp.mvvm


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.support.inline.orElse
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class MBaseViewModel constructor(application: Application) :
    AndroidViewModel(application) {
    private val compositeDisposable = CompositeDisposable()

    var showLoaderDialog: MutableLiveData<Pair<String, Disposable>> = MutableLiveData()
    var showProgressDialog: MutableLiveData<String> = MutableLiveData()
    internal var hideLoader: MutableLiveData<Unit> = MutableLiveData()
    internal var showInformationDialog: MutableLiveData<Pair<String, String>> =
        MutableLiveData() //Title,Message

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

    fun showLoader(message: String = "Loading..", disposable: Disposable? = null) {
        disposable?.let {
            showLoaderDialog.value = message to disposable
        }.orElse {
            showProgressDialog.value = message
        }
    }

    fun showInformationDialog(title: String = "Alert", message: String) {
        showInformationDialog.value = Pair(title, message)
    }

    fun hideLoader() {
        hideLoader.value = Unit
    }

    abstract fun subscribe()
    open fun unsubscribe() {
        clearAllCalls()
    }

    suspend fun runOnUiThread(callback: () -> Unit) {
        withContext(Dispatchers.Main) {
            callback()
        }
    }
}
