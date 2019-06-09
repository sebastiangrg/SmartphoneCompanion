package com.project.smartphonecompanionandroid.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.project.smartphonecompanionandroid.services.SyncWorker
import org.jetbrains.anko.AnkoLogger
import java.sql.Timestamp
import java.util.concurrent.TimeUnit

object SyncUtils : AnkoLogger {
    const val SYNC_LAST_MESSAGES = "SYNC_LAST_MESSAGES"
    const val SYNC_CONVERSATION = "SYNC_CONVERSATION"
    const val SYNC_CONTACTS = "SYNC_CONTACTS"
    const val SEND_SMS = "SEND_SMS"
    const val SYNC_ALL = "SYNC_ALL"
    const val SYNC_CONVERSATIONS_SINCE = "SYNC_CONVERSATIONS_SINCE"

    data class SMSMessage(
        val content: String,
        val phoneNumber: String,
        val datetime: Timestamp,
        val thread: Long,
        val isSender: Boolean
    )

    data class Contact(
        val name: String,
        val phoneNumber: String
    )

    fun startWorker(parameters: Map<String, String>, delayMillis: Long = 0) {
        val data = Data.Builder()

        parameters.forEach {
            data.putString(it.key, it.value)
        }

        val smsWork = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance().enqueue(smsWork)
    }
}