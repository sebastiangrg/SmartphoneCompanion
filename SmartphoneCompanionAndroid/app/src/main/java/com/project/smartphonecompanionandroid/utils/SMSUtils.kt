package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import org.jetbrains.anko.AnkoLogger
import java.sql.Timestamp

object SMSUtils : AnkoLogger {
    data class SMSMessage(
        val content: String,
        val phoneNumber: String,
        val datetime: Timestamp,
        val thread: Long,
        val isSender: Boolean
    )

    fun getMessagesSince(context: Context, timeMillis: Long): List<SMSMessage> {
        val cr = context.contentResolver

        val sent = cr.query(
            Telephony.Sms.Sent.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(timeMillis.toString()),
            Telephony.Sms.Sent.DEFAULT_SORT_ORDER
        )

        val received = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(timeMillis.toString()),
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        val receivedMessages = if (received != null) parseSMSMessages(received, false) else ArrayList()
        val sentMessages = if (sent != null) parseSMSMessages(sent, true) else ArrayList()

        return (receivedMessages + sentMessages).sortedByDescending { it.datetime }
    }


    fun getLastMessages(context: Context): List<SMSMessage> {
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

        list.addAll(receivedMessages)

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

    fun getSentForThread(context: Context, thread: Long): List<SMSMessage> {
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

    fun getReceivedForThread(context: Context, thread: Long): List<SMSMessage> {
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