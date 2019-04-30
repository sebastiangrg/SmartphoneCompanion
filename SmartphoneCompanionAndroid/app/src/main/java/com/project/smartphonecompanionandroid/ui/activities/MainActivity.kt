package com.project.smartphonecompanionandroid.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.ui.fragments.ActiveSessionsFragment
import com.project.smartphonecompanionandroid.ui.fragments.PhoneNumberFragment
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.replaceFragment

class MainActivity : AppCompatActivity() {
    private lateinit var mRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            val phoneNumberFragment = PhoneNumberFragment()
            this.replaceFragment(phoneNumberFragment, clearBackStack = true)
        } else {
            val activeSessionsFragment = ActiveSessionsFragment()
            this.replaceFragment(activeSessionsFragment, clearBackStack = true)

            FCMUtils.saveMobileTokenToFirebase()
        }
    }
}
