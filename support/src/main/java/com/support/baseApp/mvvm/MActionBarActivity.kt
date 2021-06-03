package com.support.baseApp.mvvm

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.support.R
import kotlinx.android.synthetic.main.ma_header.*


abstract class MActionBarActivity<B : ViewDataBinding, VM : MBaseViewModel> :
    MBaseActivity<B, VM>() {

    protected abstract fun getLayoutId(): Int
    protected abstract fun setUpUI(savedInstanceState: Bundle?)
    protected abstract fun getHeaderTitle(): String
    protected abstract fun isSupportBackOption(): Boolean

    open fun readIntent() {}


    override fun getBaseLayoutId(): Int {
        return R.layout.ma_header
    }

    override fun getProgressView(): View {
        return mv_header_load_progress
    }

    override fun setUpChildUI(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getHeaderTitle()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (!isSupportBackOption()) {
//            supportActionBar!!.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_top_logo_icon))
        } else {
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
        ablHeader?.elevation = 0f
        supportActionBar?.elevation = 0f
        a_header_layout_content!!.layoutResource = getLayoutId()
        binding = DataBindingUtil.bind(a_header_layout_content!!.inflate())!!
        binding.lifecycleOwner = this
        viewModel = initializeViewModel()
        setUpObserver()

        readIntent()
        setUpUI(savedInstanceState)


    }

    fun setHeaderTitle(headerTitle: String) {
        supportActionBar?.title = headerTitle
    }

    fun getToolbar(): Toolbar? {
        return toolbar
    }

    fun setAppBarLayout(resLayout:Int){
        appBarLayoutViewStub.layoutResource = resLayout
        appBarLayoutViewStub.inflate()
    }

    override fun onNetworkStatusChanged(isConnected: Boolean) {

    }
}
