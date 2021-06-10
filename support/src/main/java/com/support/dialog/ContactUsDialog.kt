package com.support.dialog

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.support.databinding.DAboutUsBinding
import com.support.supportBaseClass.MBaseDialog
import com.support.utills.openURL

class ContactUsDialog(
    private val activity: FragmentActivity
) : MBaseDialog(activity) {

    private lateinit var binding: DAboutUsBinding

    override fun getView(): View {
        binding = DAboutUsBinding.inflate(layoutInflater)
        return binding.root.rootView
    }

    override fun prepareUi(view: View) {
        binding.requestSomethingAppCompatTextView.setOnClickListener {
            openURL(activity,"www.example.com")
        }
    }

}