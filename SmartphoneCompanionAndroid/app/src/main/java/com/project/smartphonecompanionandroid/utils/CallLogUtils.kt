package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.provider.CallLog
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.sql.Timestamp

object CallLogUtils : AnkoLogger {
    data class CallLogEntry(
        val type: String,
        val phoneNumber: String,
        val datetime: Timestamp,
        val duration: Int
    )

    object CallType {
        const val INCOMING = "INCOMING"
        const val OUTGOING = "OUTGOING"
        const val MISSED = "MISSED"
        const val OTHER = "OTHER"
    }

    fun getCallLog(context: Context): List<CallLogEntry> {
        val list = ArrayList<CallLogEntry>()
        val cr = context.contentResolver

        val calls = cr.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DEFAULT_SORT_ORDER
        )

        calls?.use {
            info("Getting call log")
            val type = it.getColumnIndex(CallLog.Calls.TYPE)
            val phoneNumber = it.getColumnIndex(CallLog.Calls.NUMBER)
            val datetime = it.getColumnIndex(CallLog.Calls.DATE)
            val duration = it.getColumnIndex(CallLog.Calls.DURATION)

            for (i in 0 until it.count) {
                it.moveToNext()
                list.add(
                    CallLogEntry(
                        intToCallType(it.getInt(type)),
                        it.getString(phoneNumber),
                        Timestamp(it.getLong(datetime)),
                        it.getInt(duration)
                    )
                )
            }
        }

        return list
    }

    private fun intToCallType(intType: Int) = when (intType) {
        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
        else -> CallType.OTHER
    }
}