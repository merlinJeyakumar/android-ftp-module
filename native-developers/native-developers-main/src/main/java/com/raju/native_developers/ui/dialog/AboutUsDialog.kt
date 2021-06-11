package com.raju.native_developers.ui.dialog

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.raju.native_developers.databinding.DAboutUsBinding
import com.raju.domain.models.DeveloperModel
import com.support.supportBaseClass.MBaseDialog
import com.support.utills.openURL

class AboutUsDialog(
    private val activity: FragmentActivity,
    private val developerModel: com.raju.domain.models.DeveloperModel,
    private val contactUs: () -> Unit
) : MBaseDialog(activity) {

    private lateinit var binding: DAboutUsBinding

    override fun getView(): View {
        binding = DAboutUsBinding.inflate(layoutInflater)
        return binding.root.rootView
    }

    override fun prepareUi(view: View) {
        binding.profileFrameLayout.setOnClickListener {
            openURL(activity, developerModel.developerLink)
        }
        binding.requestSomethingAppCompatTextView.setOnClickListener {
            contactUs()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.developerAppCompatTextView.text = developerModel.developerName
    }
}