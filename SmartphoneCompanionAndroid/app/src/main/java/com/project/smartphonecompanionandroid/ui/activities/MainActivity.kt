package com.project.smartphonecompanionandroid.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.ui.fragments.ActiveSessionsFragment
import com.project.smartphonecompanionandroid.ui.fragments.PhoneNumberFragment
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.replaceFragment
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MainActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            info("User is not signed in")
            val phoneNumberFragment = PhoneNumberFragment()
            this.replaceFragment(phoneNumberFragment, clearBackStack = true)
        } else {
            info("User is signed in")
            val activeSessionsFragment = ActiveSessionsFragment()
            this.replaceFragment(activeSessionsFragment, clearBackStack = true)

            FCMUtils.saveMobileTokenToFirebase()
        }
    }
}
