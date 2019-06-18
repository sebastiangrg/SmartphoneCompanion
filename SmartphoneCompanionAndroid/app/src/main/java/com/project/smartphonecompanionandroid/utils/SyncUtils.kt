package com.project.smartphonecompanionandroid.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.project.smartphonecompanionandroid.services.SyncWorker
import org.jetbrains.anko.AnkoLogger
import java.util.concurrent.TimeUnit

object SyncUtils : AnkoLogger {
    private const val SMS_SYNC_DELAY_MILLIS = 1000L

    fun syncCallLog() {
        val data = Data.Builder()
        data.putInt("operation", SyncWorker.OPERATION_SYNC_CALL_LOG)

        val work = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance().enqueue(work)
    }

    fun syncLastMessages() {
        val data = Data.Builder()
        data.putInt("operation", SyncWorker.OPERATION_SYNC_LAST_MESSAGES)

        val work = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance().enqueue(work)
    }

    fun syncContacts() {
        val data = Data.Builder()
        data.putInt("operation", SyncWorker.OPERATION_SYNC_CONTACTS)

        val work = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance().enqueue(work)
    }

    fun syncConversation(thread: Long) {
        val data = Data.Builder()
        data.putInt("operation", SyncWorker.OPERATION_SYNC_CONVERSATION)
        data.putLong("thread", thread)

        val work = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance().enqueue(work)
    }

    fun syncAllMessages() {
        val data = Data.Builder()
        data.putInt("operation", SyncWorker.OPERATION_SYNC_ALL_MESSAGES)

        val work = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance().enqueue(work)
    }

    fun syncConversationsSince(time: Long) {
        val data = Data.Builder()
        data.putInt("operation", SyncWorker.OPERATION_SYNC_CONVERSATIONS_SINCE)
        data.putLong("time", time)

        val work = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(data.build())
            .setInitialDelay(SMS_SYNC_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance().enqueue(work)
    }

    fun sendNewMessageNotification(phoneNumber: String, content: String) {
        val data = Data.Builder()
        data.putInt("operation", SyncWorker.OPERATION_SEND_NEW_MESSAGE_NOTIFICATION)
        data.putString("phoneNumber", phoneNumber)
        data.putString("content", content)

        val work = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance().enqueue(work)
    }

    fun sendSMSMessage(phoneNumber: String, content: String) {
        // sending an sms
        val smsData = Data.Builder()
        smsData.putInt("operation", SyncWorker.OPERATION_SEND_SMS)
        smsData.putString("phoneNumber", phoneNumber)
        smsData.putString("content", content)

        val smsWork = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(smsData.build())
            .build()

        // waiting and synchronizing the sent sms
        val syncConversationSinceData = Data.Builder()
        syncConversationSinceData.putInt("operation", SyncWorker.OPERATION_SYNC_CONVERSATIONS_SINCE)
        syncConversationSinceData.putLong("time", System.currentTimeMillis())

        val syncConversationsSinceWork = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInitialDelay(SMS_SYNC_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .setInputData(syncConversationSinceData.build())
            .build()

        // synchronizing last messages
        val syncLastMessagesData = Data.Builder()
        syncLastMessagesData.putInt("operation", SyncWorker.OPERATION_SYNC_LAST_MESSAGES)

        val syncLastMessagesWork = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(syncLastMessagesData.build())
            .build()

        WorkManager.getInstance()
            .beginWith(smsWork)
            .then(syncConversationsSinceWork)
            .then(syncLastMessagesWork)
            .enqueue()
    }
}