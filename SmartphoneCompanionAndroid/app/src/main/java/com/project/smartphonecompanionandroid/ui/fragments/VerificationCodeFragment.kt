package com.project.smartphonecompanionandroid.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.afterTextChanged
import com.project.smartphonecompanionandroid.utils.clearFocusAndCloseKeyboard
import com.project.smartphonecompanionandroid.utils.replaceWith
import com.project.smartphonecompanionandroid.utils.snackbar
import kotlinx.android.synthetic.main.fragment_verification_code.*
import java.util.concurrent.TimeUnit


class VerificationCodeFragment : Fragment() {
    companion object {
        const val RESEND_SMS_INTERVAL = 60000L
    }

    private lateinit var phoneNumber: String
    private lateinit var verificationId: String
    private lateinit var verificationCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var forceResendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_verification_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVerificationCallbacks()

        this.phoneNumber = arguments?.get("phoneNumber") as String

        this.verifyingNumberTextView.text = getString(R.string.verifying, this.phoneNumber)
        this.intro3TextView.text = HtmlCompat.fromHtml(
            getString(
                R.string.intro_3_placeholder,
                this.phoneNumber
            ),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        sendVerificationCode(this.phoneNumber)

        verificationCodePinView.afterTextChanged { verifyCode(it) }

        wrongNumberTextView.setOnClickListener { goToPhoneNumber() }

        resendButton.setOnClickListener { resendVerificationCode(phoneNumber, forceResendToken) }
    }

    private fun startResendTimer() {
        if (resendButton != null) {
            resendButton.isEnabled = false
        }

        object : CountDownTimer(RESEND_SMS_INTERVAL, 1000) {
            override fun onFinish() {
                if (resendButton != null) {
                    resendButton.isEnabled = true
                    resendButton.text = getString(R.string.resend_sms)
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                if (resendButton != null) {
                    val secondsUntilFinished = millisUntilFinished / 1000
                    val timer =
                        if (secondsUntilFinished >= 10) "0:$secondsUntilFinished" else "0:0$secondsUntilFinished"
                    resendButton.text = getString(R.string.resend_sms_placeholder, timer)
                }
            }

        }.start()
    }

    private fun initVerificationCallbacks() {
        verificationCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationFailed(exception: FirebaseException?) {
                when (exception) {
                    is FirebaseAuthInvalidCredentialsException ->
                        snackbar("Invalid phone number.")
                    is FirebaseApiNotAvailableException ->
                        snackbar("Please install Google Play Services")
                    is FirebaseTooManyRequestsException ->
                        snackbar("Too many requests. Try again later.")
                    else ->
                        snackbar("Check your connection and try again.")
                }

                resendButton.isEnabled = true
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
                val code = credential?.smsCode

                code?.let {
                    if (isVisible)
                        verificationCodePinView.setText(it)
                }
            }

            override fun onCodeSent(id: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(id, forceResendingToken)
                forceResendToken = forceResendingToken
                verificationId = id

                startResendTimer()
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            verificationCallbacks
        )
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        resendButton?.let { it.isEnabled = false; }

        if (token != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                requireActivity(),
                verificationCallbacks,
                token
            )
        } else {
            sendVerificationCode(phoneNumber)
        }
    }

    private fun verifyCode(code: String) {
        if (code.length == 6 && ::verificationId.isInitialized) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)

            clearFocusAndCloseKeyboard()
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    goToActiveSessions()
                } else {
                    snackbar("Invalid verification code.")
                    verificationCodePinView.setText("")
                }
            }
    }

    private fun goToActiveSessions() {
        val fragment = ActiveSessionsFragment()
        requireActivity().replaceWith(fragment)
    }

    private fun goToPhoneNumber() {
        val fragment = PhoneNumberFragment()
        requireActivity().replaceWith(fragment)
    }
}
