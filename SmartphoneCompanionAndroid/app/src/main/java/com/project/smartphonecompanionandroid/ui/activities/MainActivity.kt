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
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        private const val BACK_BUTTON_PRESSING_INTERVAL = 2000L
    }

    private var backButtonPressedTime = 0L

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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // going to the previous fragment
            super.onBackPressed()
        } else {
            // exiting the app
            if (backButtonPressedTime + BACK_BUTTON_PRESSING_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed()
            } else {
                toast("Press again to exit")
                backButtonPressedTime = System.currentTimeMillis()
            }
        }
    }
}
