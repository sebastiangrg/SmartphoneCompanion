package com.project.smartphonecompanionandroid.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.replaceWith

import kotlinx.android.synthetic.main.fragment_phone_number.*

class PhoneNumberFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextButton.setOnClickListener { submitPhoneNumber() }

        phoneNumberEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                submitPhoneNumber()
                true
            } else {
                false
            }
        }
    }

    private fun submitPhoneNumber() {
        val phoneNumber = phoneNumberEditText.text.toString()
        if (phoneNumber.isEmpty() || phoneNumber.length < 10) {
            phoneNumberEditText.error = "Enter a valid phone number"
            phoneNumberEditText.requestFocus()
            return
        }
        goToVerification(phoneNumber)
    }

    private fun goToVerification(phoneNumber: String) {
        val fragment = VerificationCodeFragment().apply {
            val bundle = Bundle()
            bundle.putString("phoneNumber", phoneNumber)
            arguments = bundle
        }

        requireActivity().replaceWith(fragment)
    }
}
