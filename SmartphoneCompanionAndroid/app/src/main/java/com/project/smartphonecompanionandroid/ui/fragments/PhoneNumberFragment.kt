package com.project.smartphonecompanionandroid.ui.fragments

import android.opengl.Visibility
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hbb20.CountryCodePicker
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

        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText)
        countryCodePicker.setDefaultCountryUsingNameCode("RO")

        countryCodePicker.setPhoneNumberValidityChangeListener(CountryCodePicker.PhoneNumberValidityChangeListener {
            nextButton.isEnabled = it
            intro2TextView.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun submitPhoneNumber() {
        val phoneNumber = countryCodePicker.fullNumberWithPlus

        if (countryCodePicker.isValidFullNumber) {
            goToVerification(phoneNumber)
        }
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
