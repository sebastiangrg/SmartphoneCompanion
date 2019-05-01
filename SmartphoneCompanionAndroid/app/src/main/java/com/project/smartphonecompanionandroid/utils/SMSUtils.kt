package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import java.sql.Timestamp

data class SMSMessage(
    val content: String,
    val phoneNumber: String,
    val datetime: Timestamp,
    val thread: Long,
    val isSender: Boolean
)

object SMSUtils {
    private const val ADDRESS_COLUMN = "address"
    private const val DATETIME_COLUMN = "date"
    private const val CONTENT_COLUMN = "body"
    private const val THREAD_COLUMN = "thread_id"

    fun syncConversations(context: Context) {
        Log.d("SMSUtils", "Synchronizing conversations")

        val conversations = getConversations(context)
        conversations.forEach {
            Log.d("SMSUtils", it.toString())
        }

        Log.d("SMSUtils", "Done synchronizing conversations")
    }

    fun syncReceived(context: Context) {
        Log.d("SMSUtils", "Synchronizing received messages")

        val received = getReceived(context)
        received.forEach {
            Log.d("SMSUtils", it.toString())
        }

        Log.d("SMSUtils", "Done synchronizing received messages")
    }

    fun syncSent(context: Context) {
        Log.d("SMSUtils", "Synchronizing sent messages")

        val received = getSent(context)
        received.forEach {
            Log.d("SMSUtils", it.toString())
        }

        Log.d("SMSUtils", "Done synchronizing sent messages")
    }

    private fun getConversations(context: Context): List<SMSMessage> {
        val list = ArrayList<SMSMessage>()
        val cr = context.contentResolver

        val sent = cr.query(
            Telephony.Sms.Sent.CONTENT_URI,
            arrayOf(ADDRESS_COLUMN, CONTENT_COLUMN, DATETIME_COLUMN, THREAD_COLUMN),
            "1) GROUP BY ($THREAD_COLUMN",
            null,
            Telephony.Sms.Sent.DEFAULT_SORT_ORDER
        )

        val received = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(ADDRESS_COLUMN, CONTENT_COLUMN, DATETIME_COLUMN, THREAD_COLUMN),
            "1) GROUP BY ($THREAD_COLUMN",
            null,
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        val receivedMessages = if (received != null) parseSMSMessages(received) else ArrayList()
        val sentMessages = if (sent != null) parseSMSMessages(sent) else ArrayList()

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

    private fun getSent(context: Context): List<SMSMessage> {
        val cr = context.contentResolver

        val received = cr.query(
            Telephony.Sms.Sent.CONTENT_URI,
            arrayOf(ADDRESS_COLUMN, CONTENT_COLUMN, DATETIME_COLUMN, THREAD_COLUMN),
            null,
            null,
            Telephony.Sms.Sent.DEFAULT_SORT_ORDER
        )

        val list = if (received != null) parseSMSMessages(received) else ArrayList()

        return list.sortedByDescending { it.datetime }
    }

    private fun getReceived(context: Context): List<SMSMessage> {
        val cr = context.contentResolver

        val received = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(ADDRESS_COLUMN, CONTENT_COLUMN, DATETIME_COLUMN, THREAD_COLUMN),
            null,
            null,
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        val list = if (received != null) parseSMSMessages(received) else ArrayList()

        return list.sortedByDescending { it.datetime }
    }

    private fun parseSMSMessages(cursor: Cursor): List<SMSMessage> {
        val list = ArrayList<SMSMessage>()

        cursor.use {
            val address = it.getColumnIndex(ADDRESS_COLUMN)
            val date = it.getColumnIndex(DATETIME_COLUMN)
            val body = it.getColumnIndex(CONTENT_COLUMN)
            val thread = it.getColumnIndex(THREAD_COLUMN)

            for (i in 0 until it.count) {
                it.moveToNext()
                list.add(
                    SMSMessage(
                        it.getString(body),
                        it.getString(address),
                        Timestamp(it.getLong(date)),
                        it.getLong(thread),
                        false
                    )
                )
            }
        }

        return list
    }
}