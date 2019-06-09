package com.project.smartphonecompanionandroid.services

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SmsManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.smartphonecompanionandroid.utils.ContactUtils
import com.project.smartphonecompanionandroid.utils.SMSUtils
import com.project.smartphonecompanionandroid.utils.SyncUtils.SMSMessage
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams),
    AnkoLogger {

    companion object {
        const val OPERATION_SYNC_LAST_MESSAGES = 1
        const val OPERATION_SYNC_CONVERSATION = 2
        const val OPERATION_SYNC_CONTACTS = 3
        const val OPERATION_SEND_SMS = 4
        const val OPERATION_SYNC_ALL_MESSAGES = 5
        const val OPERATION_SYNC_CONVERSATIONS_SINCE = 6
    }

    override fun doWork(): Result {
        when (val operation = inputData.getInt("operation", -1)) {
            OPERATION_SYNC_LAST_MESSAGES -> syncLastMessages()
            OPERATION_SYNC_CONVERSATION -> {
                val thread = inputData.getLong("thread", -1)
                syncConversation(thread)
            }
            OPERATION_SYNC_CONTACTS -> syncContacts()
            OPERATION_SEND_SMS -> {
                val phoneNumber = inputData.getString("phoneNumber")
                val content = inputData.getString("content")

                if (phoneNumber != null && content != null) {
                    sendSMSMessage(phoneNumber, content)
                }
            }
            OPERATION_SYNC_ALL_MESSAGES -> syncAllMessages()
            OPERATION_SYNC_CONVERSATIONS_SINCE -> {
                val time = inputData.getLong("time", -1)
                syncConversationsSince(time)
            }
            else -> debug("Invalid operation $operation")
        }

        return Result.success()
    }

    @SuppressLint("UseSparseArrays")
    private fun syncConversationsSince(time: Long) {
        info("Synchronizing conversations since $time")

        val messages = SMSUtils.getMessagesSince(applicationContext, time)
        val threadToMessageList = HashMap<Long, ArrayList<SMSMessage>>()

        messages.forEach { m ->
            if (threadToMessageList.containsKey(m.thread)) {
                threadToMessageList[m.thread]?.add(m)
            } else {
                threadToMessageList[m.thread] = arrayListOf(m)
            }
        }

        threadToMessageList.forEach {
            syncThreadMessages(it.value, it.key)
        }
    }

    private fun sendSMSMessage(phoneNumber: String, content: String) {
        info("Sending SMS to $phoneNumber with content $content")

        if (FirebaseAuth.getInstance().uid != null) {
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage(phoneNumber, null, content, null, null)
        }
    }

    private fun syncAllMessages() {
        info("SyncAll")
        syncLastMessages()
        SMSUtils.getLastMessages(applicationContext).forEach { syncConversation(it.thread) }
    }

    private fun syncLastMessages() {
        info("Synchronizing last messages")

        val lastMessages = SMSUtils.getLastMessages(applicationContext)
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

    private fun syncConversation(thread: Long) {
        info("Synchronizing conversation $thread")

        val received = SMSUtils.getReceivedForThread(applicationContext, thread)
        val sent = SMSUtils.getSentForThread(applicationContext, thread)

        val messages = (received + sent).sortedByDescending { it.datetime }

        syncThreadMessages(messages, thread)
    }

    private fun syncThreadMessages(messages: List<SMSMessage>, thread: Long) {
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

    private fun syncContacts() {
        info("Synchronizing contacts")

        val contacts = ContactUtils.getContacts(applicationContext)
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
}