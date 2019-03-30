package com.project.smartphonecompanionandroid.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.project.smartphonecompanionandroid.R
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
        if (fragment is VerificationCodeFragment) {
            super.onBackPressed()
        } else {
            finish()
        }
    }
}
