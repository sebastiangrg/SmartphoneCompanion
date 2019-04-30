package com.project.smartphonecompanionandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.hbb20.CountryCodePicker

import kotlinx.android.synthetic.main.fragment_phone_number.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.text.HtmlCompat
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.replaceFragment


class PhoneNumberFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextButton.setOnClickListener { showConfirmationDialog() }

        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText)
        countryCodePicker.setDefaultCountryUsingNameCode("RO")

        countryCodePicker.setPhoneNumberValidityChangeListener(CountryCodePicker.PhoneNumberValidityChangeListener {
            nextButton.isEnabled = it
            intro2TextView.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(ContextThemeWrapper(requireContext(), R.style.Dialog))

        with(builder)
        {
            setTitle(getText(R.string.number_confirmation_title))
            setMessage(
                HtmlCompat.fromHtml(
                    getString(
                        R.string.number_confirmation_message,
                        countryCodePicker.fullNumberWithPlus
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            setPositiveButton(android.R.string.ok) { _, _ -> run { submitPhoneNumber() } }
            setNegativeButton(android.R.string.cancel) { _, _ -> run {} }
        }.show()
    }

    private fun submitPhoneNumber() {
        val phoneNumber = countryCodePicker.fullNumberWithPlus

        if (countryCodePicker.isValidFullNumber) {
            val fragment = VerificationCodeFragment().apply {
                val bundle = Bundle()
                bundle.putString("phoneNumber", phoneNumber)
                arguments = bundle
            }

            requireActivity().replaceFragment(fragment)
        }
    }
}
