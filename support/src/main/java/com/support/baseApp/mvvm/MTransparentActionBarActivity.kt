package com.support.baseApp.mvvm

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.ViewDataBinding
import com.support.R
import kotlinx.android.synthetic.main.ma_transparent_header.*


abstract class MTransparentActionBarActivity<B : ViewDataBinding, VM : MBaseViewModel> :
    MActionBarActivity<B, VM>() {

    override fun getBaseLayoutId(): Int {
        return R.layout.ma_transparent_header
    }

    fun getBackgroundImageView(): AppCompatImageView {
        return backgroundImageAppCompatImageView
    }
}
