package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.provider.ContactsContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

data class Contact(
    val name: String,
    val phoneNumber: String
)

object ContactUtils : AnkoLogger {

    fun syncContacts(context: Context) {
        info("Synchronizing contacts")

        val contacts = getContacts(context)
        val contactsMap = HashMap<String, String>()

        info("Contacts:")
        contacts.forEach {
            info(it.toString())
            contactsMap[it.phoneNumber] = it.name
        }

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            info("Firebase UID: $uid")
            firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("contacts")
                .updateChildren(contactsMap as Map<String, Any>)
                .addOnFailureListener { warn("Synchronizing contacts to Firebase failed") }
                .addOnCanceledListener { warn("Synchronizing contacts to Firebase canceled") }
                .addOnCompleteListener { info("Done synchronizing contacts") }
        }
    }

    private fun getContacts(context: Context): List<Contact> {
        val list = ArrayList<Contact>()
        val cr = context.contentResolver

        val contacts = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY
        )

        contacts?.use {
            info("Getting contacts")
            val displayName = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneNumber = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            for (i in 0 until it.count) {
                it.moveToNext()
                list.add(Contact(it.getString(displayName), it.getString(phoneNumber)))
            }
        }

        return list
    }
}