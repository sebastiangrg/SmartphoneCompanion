package com.project.smartphonecompanionandroid.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.sql.Timestamp

data class SMSMessage(
    val content: String,
    val phoneNumber: String,
    val datetime: Timestamp,
    val thread: Long,
    val isSender: Boolean
)

object SMSUtils {
    private const val TAG = "SMSUtils"

    @SuppressLint("UseSparseArrays")
    fun syncLastMessages(context: Context) {
        Log.d(TAG, "Synchronizing last messages")

        val lastMessages = getLastMessages(context)
        val lastMessagesMap = HashMap<String, SMSMessage>()
        lastMessages.forEach {
            Log.d(TAG, it.toString())
            lastMessagesMap[it.thread.toString()] = it
        }

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            Log.d(TAG, uid)
            firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("lastMessages")
                .updateChildren(lastMessagesMap as Map<String, Any>)
        }

        Log.d(TAG, "Done synchronizing last messages")
    }

    fun syncConversation(context: Context, thread: Long) {
        Log.d(TAG, "Synchronizing conversation $thread")

        val received = getReceivedForThread(context, thread)
        val sent = getSentForThread(context, thread)

        val messages = (received + sent).sortedByDescending { it.datetime }
        val messagesMap = HashMap<String, SMSMessage>()

        messages.forEach {
            Log.d(TAG, it.toString())
            messagesMap[it.hashCode().toString()] = it
        }

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            Log.d(TAG, uid)
            firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("messages")
                .child(thread.toString())
                .updateChildren(messagesMap as Map<String, Any>)
        }

        Log.d(TAG, "Done synchronizing conversation $thread")
    }

    private fun getLastMessages(context: Context): List<SMSMessage> {
        val list = ArrayList<SMSMessage>()
        val cr = context.contentResolver

        val sent = cr.query(
            Telephony.Sms.Sent.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "1) GROUP BY (${Telephony.Sms.THREAD_ID}",
            null,
            Telephony.Sms.Sent.DEFAULT_SORT_ORDER
        )

        val received = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "1) GROUP BY (${Telephony.Sms.THREAD_ID}",
            null,
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        val receivedMessages = if (received != null) parseSMSMessages(received, false) else ArrayList()
        val sentMessages = if (sent != null) parseSMSMessages(sent, true) else ArrayList()

        receivedMessages.forEach { list.add(it) }

        sentMessages.forEach {
            val m = list.find { m -> m.thread == it.thread }
            if (m != null) {
                if (m.datetime < it.datetime) {
                    list.remove(m)
                    list.add(it)
                }
            } else {
                list.add(it)
            }
        }

        return list.sortedByDescending { it.datetime }
    }

    private fun getSentForThread(context: Context, thread: Long): List<SMSMessage> {
        val cr = context.contentResolver

        val sent = cr.query(
            Telephony.Sms.Sent.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(thread.toString()),
            Telephony.Sms.Sent.DEFAULT_SORT_ORDER
        )

        return if (sent != null) parseSMSMessages(sent, true) else ArrayList()
    }

    private fun getReceivedForThread(context: Context, thread: Long): List<SMSMessage> {
        val cr = context.contentResolver

        val received = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(thread.toString()),
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        return if (received != null) parseSMSMessages(received, false) else ArrayList()
    }

    private fun parseSMSMessages(cursor: Cursor, isSender: Boolean): List<SMSMessage> {
        val list = ArrayList<SMSMessage>()

        cursor.use {
            val address = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val date = it.getColumnIndex(Telephony.Sms.DATE)
            val body = it.getColumnIndex(Telephony.Sms.BODY)
            val thread = it.getColumnIndex(Telephony.Sms.THREAD_ID)

            for (i in 0 until it.count) {
                it.moveToNext()
                list.add(
                    SMSMessage(
                        it.getString(body),
                        it.getString(address),
                        Timestamp(it.getLong(date)),
                        it.getLong(thread),
                        isSender
                    )
                )
            }
        }

        return list
    }
}