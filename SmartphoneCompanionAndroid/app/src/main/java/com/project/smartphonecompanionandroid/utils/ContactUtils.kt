package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

data class Contact(
    val name: String,
    val phoneNumber: String
)

object ContactUtils {
    private const val TAG = "ContactUtils"

    fun syncContacts(context: Context) {
        Log.d(TAG, "Synchronizing contacts")

        val contacts = getContacts(context)
        val contactsMap = HashMap<String, String>()
        contacts.forEach {
            Log.d(TAG, it.toString())
            contactsMap[it.phoneNumber] = it.name
        }

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            Log.d(TAG, uid)
            firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("contacts")
                .updateChildren(contactsMap as Map<String, Any>)
        }

        Log.d(TAG, "Done synchronizing contacts")
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