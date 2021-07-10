package com.raju.native_developers.ui.dialog

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository
import com.raju.native_developers.NativeDevelopersInjection
import com.raju.native_developers.databinding.DReferralBinding
import com.support.supportBaseClass.MBaseDialog

class ReferralDialog(
    private val activity: FragmentActivity,
    private val referralButton: () -> Unit
) : MBaseDialog(activity) {
    private val nativeDevelopersAppSettingsRepository: NativeDevelopersAppSettingsRepository
        get() = NativeDevelopersInjection.provideAppDataSource(
            activity
        )

    private lateinit var binding: DReferralBinding

    override fun getView(): View {
        binding = DReferralBinding.inflate(layoutInflater)
        return binding.root.rootView
    }

    override fun prepareUi(view: View) {
        initListener()
        initUi()
    }

    private fun initListener() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.shareReferralLinkAppCompatTextView.setOnClickListener {
            referralButton()
        }
    }

    private fun initUi() {
        val minCount = nativeDevelopersAppSettingsRepository.getMinimumReferralsCount()
        val refCount = nativeDevelopersAppSettingsRepository.getReferralsCount()
        binding.referralCountAppCompatTextView.text = "You referred ${refCount}/${minCount}"
    }
}