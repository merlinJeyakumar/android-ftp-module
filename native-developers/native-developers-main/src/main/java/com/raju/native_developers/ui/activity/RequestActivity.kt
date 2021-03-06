package com.raju.native_developers.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.raju.domain.models.DeveloperModel
import com.raju.native_developers.R
import com.raju.native_developers.ui.fragment.RequestFragment
import com.support.supportBaseClass.BaseActivity
import com.support.utills.FragmentUtility.replaceFragment
import kotlinx.android.synthetic.main.a_request.*

class RequestActivity : BaseActivity() { //TODO: REMOTE_CONFIG_REQUIRED

    companion object {
        fun getActivity(
            context: Context,
            name: String,
            developerModel: DeveloperModel,
            isInformation: Boolean
        ): Intent {
            return Intent(context,RequestActivity::class.java).apply {
                this.putExtra(
                    RequestFragment.ARG_REQUEST_MODEL,
                    RequestFragment.RequestModel(
                        name = name,
                        developerModel = developerModel,
                        requestType = (if (isInformation) RequestFragment.RequestType.INFORM else RequestFragment.RequestType.REPORT).name
                    )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_request)

        initToolbar()
        initFragment()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Contact us"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initFragment() {
        replaceFragment(
            RequestFragment.newInstance(intent.getParcelableExtra(RequestFragment.ARG_REQUEST_MODEL)!!),
            R.id.container
        )
    }

    override fun onNetworkStatusChanged(isConnected: Boolean) {
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        finish()
    }
}