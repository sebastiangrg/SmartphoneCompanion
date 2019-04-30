package com.project.smartphonecompanionandroid.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.replaceFragment
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
            intro5TextView.text = HtmlCompat.fromHtml(
                getString(R.string.intro_5_placeholder, it),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        addButton.setOnClickListener { addSession() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_active_sessions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signOutMenuItem -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addSession() {
        val fragment = QRCodeScannerFragment()
        requireActivity().replaceFragment(fragment)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        goToSignInFlow()
    }

    private fun goToSignInFlow() {
        val fragment = PhoneNumberFragment()
        requireActivity().replaceFragment(fragment, clearBackStack = true)
    }
}
