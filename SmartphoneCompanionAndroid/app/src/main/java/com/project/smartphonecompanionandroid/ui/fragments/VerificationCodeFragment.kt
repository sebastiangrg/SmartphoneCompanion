package com.project.smartphonecompanionandroid.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo

import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.replaceWith
import com.project.smartphonecompanionandroid.utils.snackbar
import kotlinx.android.synthetic.main.fragment_verification_code.*
import java.util.concurrent.TimeUnit


class VerificationCodeFragment : Fragment() {
    private lateinit var phoneNumber: String
    private lateinit var verificationId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_verification_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.phoneNumber = arguments?.get("phoneNumber") as String

        sendVerificationCode(this.phoneNumber)

        submitButton.setOnClickListener { verifyCode() }

        verificationCodeEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCode()
                true
            } else {
                false
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
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
                }

                override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
                    val code = credential?.smsCode

                    if (code != null) {
                        if (isVisible)
                            verificationCodeEditText.setText(code)
                    }
                }

                override fun onCodeSent(id: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(id, forceResendingToken)
                    verificationId = id
                }
            })
    }

    private fun verifyCode() {
        val code = verificationCodeEditText.text.toString()
        val credential = PhoneAuthProvider.getCredential(verificationId, code)

        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    goToActiveSessions()
                } else {
                    snackbar("Invalid verification code.")
                }
            }
    }

    private fun goToActiveSessions() {
        val fragment = ActiveSessionsFragment()
        requireActivity().replaceWith(fragment)
    }
}
