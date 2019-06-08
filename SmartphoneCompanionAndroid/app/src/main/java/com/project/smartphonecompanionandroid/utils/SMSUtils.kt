package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.telephony.SmsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn
import java.sql.Timestamp

data class SMSMessage(
    val content: String,
    val phoneNumber: String,
    val datetime: Timestamp,
    val thread: Long,
    val isSender: Boolean
)

object SMSUtils : AnkoLogger {
    fun sendSMSMessage(context: Context, phoneNumber: String, content: String) {
        info("Sending SMS to $phoneNumber with content $content")
        val smsManager = SmsManager.getDefault() as SmsManager
        smsManager.sendTextMessage(phoneNumber, null, content, null, null)
    }

    fun syncAll(context: Context) {
        info("SyncAll")
        syncLastMessages(context)
        getLastMessages(context).forEach { syncConversation(context, it.thread) }
    }

    fun syncLastMessages(context: Context) {
        info("Synchronizing last messages")

        val lastMessages = getLastMessages(context)
        val lastMessagesMap = HashMap<String, SMSMessage>()

        lastMessages.forEach {
            info("Message: $it")
            lastMessagesMap[it.thread.toString()] = it
        }

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            info("Firebase UID: $uid")
            firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("lastMessages")
                .updateChildren(lastMessagesMap as Map<String, Any>)
                .addOnFailureListener { warn("Synchronizing last messages to Firebase failed") }
                .addOnCanceledListener { warn("Synchronizing last messages to Firebase canceled") }
                .addOnCompleteListener { info("Done synchronizing last messages") }
        }
    }

    fun syncConversation(context: Context, thread: Long) {
        info("Synchronizing conversation $thread")

        val received = getReceivedForThread(context, thread)
        val sent = getSentForThread(context, thread)

        val messages = (received + sent).sortedByDescending { it.datetime }
        val messagesMap = HashMap<String, SMSMessage>()

        messages.forEach {
            info("Message: $it")
            messagesMap[it.hashCode().toString()] = it
        }

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            info("Firebase UID: $uid")
            firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("messages")
                .child(thread.toString())
                .updateChildren(messagesMap as Map<String, Any>)
                .addOnFailureListener { warn("Synchronizing conversation $thread to Firebase failed") }
                .addOnCanceledListener { warn("Synchronizing conversation $thread to Firebase canceled") }
                .addOnCompleteListener { info("Done synchronizing conversation $thread") }
        }
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
            null,//arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
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
            null,//arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
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
                try {
                    list.add(
                        SMSMessage(
                            it.getString(body),
                            it.getString(address),
                            Timestamp(it.getLong(date)),
                            it.getLong(thread),
                            isSender
                        )
                    )
                } catch (_: Exception) {
                }
            }
        }

        return list
    }
}