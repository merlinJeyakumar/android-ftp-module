package com.support.dialog

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.support.R
import com.support.databinding.FRequestBinding
import com.support.device.utility.DeviceUtility
import com.support.device.utility.Utility
import com.support.device.utility.Utility.sendEmail
import com.support.model.DeveloperModel
import com.support.utills.ValidationUtility
import com.support.utills.parseDateFromMilliseconds

class RequestFragment : Fragment() {
    val SMS_MAX_LENGTH = 160
    val EMAIL_MAX_LENGTH = 9999

    private lateinit var argRequestModel: RequestModel
    private lateinit var binding: FRequestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FRequestBinding.inflate(inflater)

        initData()
        initView()
        initListener()
        initPreview()
        return binding.root.rootView
    }

    private fun initData() {
        arguments?.let {
            argRequestModel = it.getParcelable<RequestModel>(ARG_REQUEST_MODEL)!!
        }
    }

    private fun initView() {

    }

    private fun initListener() {
        binding.communicationRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.phoneMaterialRadioButton -> {
                    binding.emailTextInputLayout.visibility = View.GONE
                    binding.phoneTextInputLayout.visibility = View.VISIBLE
                    binding.messageTextInputLayout.editText?.filters =
                        arrayOf<InputFilter>(InputFilter.LengthFilter(SMS_MAX_LENGTH))
                    binding.messageTextInputLayout.counterMaxLength = SMS_MAX_LENGTH
                }
                R.id.emailMaterialRadioButton -> {
                    binding.emailTextInputLayout.visibility = View.VISIBLE
                    binding.phoneTextInputLayout.visibility = View.GONE
                    binding.messageTextInputLayout.editText?.filters =
                        arrayOf<InputFilter>(InputFilter.LengthFilter(EMAIL_MAX_LENGTH))
                    binding.messageTextInputLayout.counterMaxLength = -1
                }
            }
        }
        binding.submitMaterialButton.setOnClickListener {
            if (isInputsValid()) {
                if (binding.emailMaterialRadioButton.isChecked) {
                    sendEmail(
                        activity,
                        getDeveloperEmail(),
                        getEmailSubject(),
                        getEmailBody()
                    )
                } else if (binding.phoneMaterialRadioButton.isChecked) {
                    DeviceUtility.sendSms(
                        activity!!,
                        getDeveloperPhone(),
                        binding.messageTextInputLayout.editText?.text.toString()
                    )
                }
            }
        }
    }

    private fun isInputsValid(): Boolean {
        val nameInput = binding.nameTextInputLayout.editText?.text.toString()
        val emailInput = binding.emailTextInputLayout.editText?.text.toString()
        val phoneInput = binding.phoneTextInputLayout.editText?.text.toString()
        val messageInput = binding.messageTextInputLayout.editText?.text.toString()

        binding.nameTextInputLayout.error = if (nameInput.length < 3) {
            "invalid name"
        } else {
            null
        }

        binding.emailTextInputLayout.error = if (!ValidationUtility.isValidEmail(emailInput)) {
            "invalid email"
        } else {
            null
        }

        binding.phoneTextInputLayout.error = if (!ValidationUtility.isValidMobile(phoneInput)) {
            "invalid phone"
        } else {
            null
        }

        binding.messageTextInputLayout.error =
            if (messageInput.length < 3 || (if (binding.emailMaterialRadioButton.isChecked) messageInput.length > EMAIL_MAX_LENGTH else messageInput.length > SMS_MAX_LENGTH)
            ) {
                "invalid message"
            } else {
                null
            }

        return binding.nameTextInputLayout.error == null &&
                binding.emailTextInputLayout.error == null &&
                binding.phoneTextInputLayout.error == null &&
                binding.messageTextInputLayout.error == null
    }

    private fun getEmailSubject(): String {
        return "${if (isSendingInformation()) "Inform" else "Report Bug ${binding.nameTextInputLayout.editText?.text.toString()} "}${
            parseDateFromMilliseconds(
                System.currentTimeMillis(),
                "dd/MMM/yyyy : hh:mm aa"
            )
        } | YOUR_SUBJECT"
    }

    private fun getEmailBody(): String {
        return "${binding.emailTextInputLayout.editText?.text.toString()}\n${binding.messageTextInputLayout.editText?.text.toString()}"
    }

    private fun getDeveloperEmail(): String {
        return argRequestModel.developerModel.developerEmail
    }

    private fun getDeveloperPhone(): String {
        return argRequestModel.developerModel.developerPhone
    }

    private fun isSendingInformation(): Boolean {
        return RequestType.valueOf(argRequestModel.requestType) == RequestType.INFORM
    }

    private fun initPreview() {
        binding.emailTextInputLayout.visibility = View.GONE
        binding.messageTextInputLayout.counterMaxLength = SMS_MAX_LENGTH
    }

    companion object {
        const val ARG_REQUEST_MODEL = "arg_request_model"
        fun newInstance(requestModel: RequestModel) =
            RequestFragment().apply {
                arguments = Bundle().apply { this.putParcelable(ARG_REQUEST_MODEL, requestModel) }
            }
    }

    data class RequestModel(
        var name: String,
        var developerModel: DeveloperModel,
        var requestType: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readParcelable<DeveloperModel>(DeveloperModel::class.java.classLoader)!!,
            parcel.readString()!!
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeParcelable(developerModel, flags)
            parcel.writeString(requestType)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<RequestModel> {
            override fun createFromParcel(parcel: Parcel): RequestModel {
                return RequestModel(parcel)
            }

            override fun newArray(size: Int): Array<RequestModel?> {
                return arrayOfNulls(size)
            }
        }
    }

    enum class RequestType {
        INFORM,
        REPORT
    }
}