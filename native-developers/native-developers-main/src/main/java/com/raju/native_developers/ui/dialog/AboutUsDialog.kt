package com.raju.native_developers.ui.dialog

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.raju.native_developers.databinding.DAboutUsBinding
import com.raju.domain.models.DeveloperModel
import com.support.supportBaseClass.MBaseDialog
import com.support.utills.openURL

class AboutUsDialog(
    private val activity: FragmentActivity,
    private val appName:String,
    private val developerModel: DeveloperModel,
    private val contactUs: () -> Unit
) : MBaseDialog(activity) {

    private lateinit var binding: DAboutUsBinding

    override fun getView(): View {
        binding = DAboutUsBinding.inflate(layoutInflater)
        return binding.root.rootView
    }

    override fun prepareUi(view: View) {
        intiListener()
        initUi()
    }

    private fun intiListener() {
        binding.profileFrameLayout.setOnClickListener {
            openURL(activity, developerModel.remote_dev_profile_link)
        }
        binding.requestSomethingAppCompatTextView.setOnClickListener {
            contactUs()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun initUi() {
        binding.developerNameAppCompatTextView.text = developerModel.remote_dev_developer_name
        binding.organisationNameAppCompatTextView.text = developerModel.remote_dev_organisation_name
        binding.appNameAppCompatTextView.text = appName
    }
}