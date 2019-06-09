package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.provider.ContactsContract
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

object ContactUtils : AnkoLogger {
    data class Contact(
        val name: String,
        val phoneNumber: String
    )

    fun getContacts(context: Context): List<Contact> {
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