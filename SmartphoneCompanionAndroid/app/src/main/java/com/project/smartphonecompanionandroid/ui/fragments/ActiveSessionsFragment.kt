package com.project.smartphonecompanionandroid.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.utils.replaceFragment
import kotlinx.android.synthetic.main.fragment_active_sessions.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import android.content.Context
import com.project.smartphonecompanionandroid.utils.ContactUtils
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.SMSUtils
import java.util.ArrayList


class ActiveSessionsFragment : Fragment(), AnkoLogger {
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

        intro7cardView.setOnClickListener {
            requestSMSAndContactsPermissions()
        }

        requestSMSAndContactsPermissions()

        FCMUtils.saveMobileTokenToFirebase()
        ContactUtils.syncContacts(requireContext())
        SMSUtils.syncAll(requireContext())
    }

    private fun requestSMSAndContactsPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )

        Permissions.check(requireContext(), permissions, null, null, object : PermissionHandler() {
            override fun onGranted() {
                intro7cardView.visibility = View.GONE
            }

            override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                intro7cardView.visibility = View.VISIBLE
            }
        })
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
        Permissions.check(
            requireContext(),
            Manifest.permission.CAMERA,
            R.string.camera_permission_rationale,
            object : PermissionHandler() {
                override fun onGranted() {
                    val fragment = QRCodeScannerFragment()
                    requireActivity().replaceFragment(fragment)
                }
            })
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
