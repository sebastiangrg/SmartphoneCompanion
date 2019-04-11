package com.project.smartphonecompanionandroid.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.clearBackStack
import com.project.smartphonecompanionandroid.utils.replaceWith
import kotlinx.android.synthetic.main.fragment_active_sessions.*


class ActiveSessionsFragment : Fragment() {
    private var user: FirebaseUser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_active_sessions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = FirebaseAuth.getInstance().currentUser

        user?.phoneNumber?.let {
            userTextView.text = it
        }

        addButton.setOnClickListener { addSession() }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_active_sessions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.signOutMenuItem -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addSession() {
        val fragment = QRCodeScannerFragment()
        requireActivity().replaceWith(fragment)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        goToSignInFlow()
    }

    private fun goToSignInFlow() {
        if (requireActivity().supportFragmentManager.fragments.find { f -> f is PhoneNumberFragment } != null) {
            requireActivity().clearBackStack()
        }
        val fragment = PhoneNumberFragment()
        requireActivity().replaceWith(fragment)
    }
}
