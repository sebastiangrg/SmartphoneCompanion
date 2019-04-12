package com.project.smartphonecompanionandroid.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.ui.fragments.ActiveSessionsFragment
import com.project.smartphonecompanionandroid.ui.fragments.PhoneNumberFragment
import com.project.smartphonecompanionandroid.ui.fragments.QRCodeScannerFragment
import com.project.smartphonecompanionandroid.ui.fragments.VerificationCodeFragment
import com.project.smartphonecompanionandroid.utils.replaceWith

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            val phoneNumberFragment = PhoneNumberFragment()
            this.replaceWith(phoneNumberFragment)
        } else {
            val activeSessionsFragment = ActiveSessionsFragment()
            this.replaceWith(activeSessionsFragment)
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.main_container)
        if (fragment is VerificationCodeFragment || fragment is QRCodeScannerFragment) {
            super.onBackPressed()
        } else {
            finish()
        }
    }
}
