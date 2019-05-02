package com.project.smartphonecompanionandroid.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.replaceFragment
import com.project.smartphonecompanionandroid.utils.snackbar
import kotlinx.android.synthetic.main.fragment_active_sessions.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class ActiveSessionsFragment : Fragment(), AnkoLogger {
    companion object {
        private const val READ_SMS_PERMISSION_REQUEST_CODE = 2
    }

    private var user: FirebaseUser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_active_sessions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        info("Starting ActiveSessionsFragment")

        user = FirebaseAuth.getInstance().currentUser

        user?.phoneNumber?.let {
            intro5TextView.text = HtmlCompat.fromHtml(
                getString(R.string.intro_5_placeholder, it),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        addButton.setOnClickListener { addSession() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(Array(READ_SMS_PERMISSION_REQUEST_CODE) { Manifest.permission.READ_SMS }, 1)
            } else {
                info("Permission granted")
            }
        }
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            READ_SMS_PERMISSION_REQUEST_CODE ->
                // if the request is cancelled, the result arrays are empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    snackbar("Permission granted")
                } else {
                    snackbar("You declined to allow the app to access your camera")
                }
        }
    }
}
