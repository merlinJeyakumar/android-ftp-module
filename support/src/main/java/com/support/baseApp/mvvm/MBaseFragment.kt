package com.support.baseApp.mvvm


import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.support.R
import com.support.baseApp.mvvm.dialog.MConfirmationDialog
import com.support.baseApp.mvvm.permission.MEasyPermissions
import com.support.supportBaseClass.CustomProgressDialog
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import org.jetbrains.anko.support.v4.toast


abstract class MBaseFragment<B : ViewDataBinding, VM : MBaseViewModel> : Fragment(), IMBaseView {

    private lateinit var customProgressDialog: CustomProgressDialog
    private val compositeDisposable = CompositeDisposable()
    var alertDialog: AlertDialog? = null


    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    protected abstract fun initializeViewModel(): VM
    protected abstract fun getLayoutResID(): Int
    protected abstract fun setUpUI()
    protected lateinit var inflateView: View

    companion object {
        const val REQ_CONTACT_PERMISSION = 121
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpUI()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        inflateView = inflater.inflate(getLayoutResID(), container, false)
        container?.layoutTransition?.setAnimateParentHierarchy(false);
        binding = DataBindingUtil.bind(inflateView)!!
        viewModel = initializeViewModel()

        readIntent()
        setUpObserver()
        return inflateView
    }

    fun addRxCall(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    private fun clearAllCalls() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.clear()
        }
    }

    private fun setUpObserver() {
        viewModel.showLoaderDialog.observe(getBaseActivity(), Observer {
            showLoaderDialog(it)
        })
        viewModel.hideLoader.observe(getBaseActivity(), Observer {
            hideLoaderDialog()
        })
        viewModel.toastMessage.observe(getBaseActivity(), Observer {
            showToast(it)
        })
        viewModel.toastResMessage.observe(getBaseActivity(), Observer {
            showToast(it)
        })
    }

    private fun showLoaderDialog(it: Pair<String, Disposable>) {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).showLoader(it.first, true, it.second)
        }
    }

    private fun hideLoaderDialog() {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).hideLoader()
        }
    }

    public override fun showProgress() {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).showProgress()
        }
    }

    public override fun hideProgress() {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).hideProgress()
        }
    }

    override fun showToastMessage(message: String) {

    }

    override fun hideSoftKeyboard() {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).hideSoftKeyboard()
        }
    }

    override fun showSoftKeyboard(view: View) {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).showSoftKeyboard(view)
        }
    }

    fun showMessage(@StringRes resId: Int) {
        showMessage(true, getString(resId))
    }

    fun showMessage(message: String) {
        showMessage(true, message)
    }

    fun showMessage(isSuccess: Boolean, @StringRes resId: Int) {
        showMessage(isSuccess, getString(resId))
    }

    fun showMessage(isSuccess: Boolean, message: String) {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).showMessage(isSuccess, message)
        }
    }

    fun showErrorMessage() {
        showMessage(false, R.string.msg_please_try_again)
    }

    fun showErrorMessage(body: String) {
        showMessage(
            false,
            if (TextUtils.isEmpty(body)) getString(R.string.msg_please_try_again) else body
        )
    }

    fun showErrorMessage(@StringRes strResId: Int) {
        val message = getString(strResId)
        showMessage(
            false,
            if (TextUtils.isEmpty(message)) getString(R.string.msg_please_try_again) else message
        )
    }

    fun showToast(msg: String) {
        toast(msg)
    }

    fun showToast(@StringRes resId: Int) {
        toast(resId)
    }

    fun isInternetAvailable(): Boolean {
        return if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).isInternetAvailable()
        } else {
            (activity as MBaseActivity<*, *>).isInternetAvailable()
        }
    }

    fun showNoInternetAvailable() {

        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).showNoInternetAvailable()
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        MEasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun showExplainationDialog(
        @StringRes infoMessage: Int,
        onClickListener: DialogInterface.OnClickListener
    ) {
        if (activity is MBaseActivity<*, *>) {
            (activity as MBaseActivity<*, *>).showExplanationDialog(infoMessage, onClickListener)
        }
    }

    fun showConfirmationDialog(
        title: String = getString(R.string.alert),
        message: String,
        positiveText: String = this.getString(R.string.label_ok),
        negativeText: String = this.getString(R.string.label_cancel),
        isCancellable: Boolean = true,
        dialogInterface: DialogInterface.OnClickListener
    ) {
        getBaseActivity().showConfirmationDialog(title, message, positiveText, negativeText, isCancellable, dialogInterface)
    }

    open fun readIntent() {}

    fun getBaseActivity(): MBaseActivity<*, *> {
        return activity as MBaseActivity<*, *>
    }

    override fun onStop() {
        super.onStop()
        alertDialog?.dismiss()
    }

    override fun onDestroy() {
        clearAllCalls()
        super.onDestroy()
    }

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (!visible) {
            System.gc()
        }
    }
}
