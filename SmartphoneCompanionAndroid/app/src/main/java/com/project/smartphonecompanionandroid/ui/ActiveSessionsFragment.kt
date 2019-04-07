package com.project.smartphonecompanionandroid.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.clearBackStack
import com.project.smartphonecompanionandroid.utils.replaceWith
import kotlinx.android.synthetic.main.fragment_active_sessions.*


class ActiveSessionsFragment : Fragment() {
    private var user: FirebaseUser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_active_sessions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = FirebaseAuth.getInstance().currentUser

        user?.phoneNumber?.let {
            userTextView.text = it
        }

        logOutButton.setOnClickListener { logOut() }

        addButton.setOnClickListener{ addSession() }
    }

    private fun addSession() {
        val fragment = QRCodeScannerFragment()
        requireActivity().replaceWith(fragment)
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
        goToLogInFlow()
    }

    private fun goToLogInFlow() {
        if (requireActivity().supportFragmentManager.fragments.find { f -> f is PhoneNumberFragment } != null) {
            requireActivity().clearBackStack()
        }
        val fragment = PhoneNumberFragment()
        requireActivity().replaceWith(fragment)
    }
}
