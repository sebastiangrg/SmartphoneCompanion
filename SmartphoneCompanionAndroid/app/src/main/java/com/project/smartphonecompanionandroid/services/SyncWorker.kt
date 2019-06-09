package com.project.smartphonecompanionandroid.services

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.smartphonecompanionandroid.utils.SyncUtils.Contact
import com.project.smartphonecompanionandroid.utils.SyncUtils.SMSMessage
import com.project.smartphonecompanionandroid.utils.SyncUtils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.jetbrains.anko.warn
import java.sql.Timestamp

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams),
    AnkoLogger {

    override fun doWork(): Result {
        when (val operation = inputData.getString("operation")) {
            SyncUtils.SYNC_LAST_MESSAGES -> syncLastMessages(this.applicationContext)
            SyncUtils.SYNC_CONVERSATION -> {
                val thread = inputData.getString("thread")

                thread?.let {
                    syncConversation(this.applicationContext, thread.toLong())
                }
            }
            SyncUtils.SYNC_CONTACTS -> syncContacts(this.applicationContext)
            SyncUtils.SEND_SMS -> {
                val phoneNumber = inputData.getString("phoneNumber")
                val content = inputData.getString("content")

                if (phoneNumber != null && content != null) {
                    sendSMSMessage(phoneNumber, content)
                }
            }
            SyncUtils.SYNC_ALL -> syncAll(applicationContext)
            SyncUtils.SYNC_CONVERSATIONS_SINCE -> {
                val time = inputData.getString("time")

                time?.let {
                    syncConversationsSince(this.applicationContext, time)
                }
            }
            else -> debug("Invalid operation $operation")
        }

        return Result.success()
    }

    @SuppressLint("UseSparseArrays")
    private fun syncConversationsSince(context: Context, time: String) {
        info("Synchronizing conversations since $time")

        val messages = getMessagesSince(context, time.toLong())
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
        val smsManager = SmsManager.getDefault() as SmsManager
        smsManager.sendTextMessage(phoneNumber, null, content, null, null)
    }

    private fun syncAll(context: Context) {
        info("SyncAll")
        syncLastMessages(context)
        syncContacts(context)
        getLastMessages(context).forEach { syncConversation(context, it.thread) }
    }

    private fun syncLastMessages(context: Context) {
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

    private fun syncConversation(context: Context, thread: Long) {
        info("Synchronizing conversation $thread")

        val received = getReceivedForThread(context, thread)
        val sent = getSentForThread(context, thread)

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

    private fun getMessagesSince(context: Context, timeMillis: Long): List<SMSMessage> {
        val list = ArrayList<SMSMessage>()
        val cr = context.contentResolver

        val sent = cr.query(
            Telephony.Sms.Sent.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "${Telephony.Sms.DATE} > ?",
            arrayOf(timeMillis.toString()),
            Telephony.Sms.Sent.DEFAULT_SORT_ORDER
        )

        val received = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.THREAD_ID),
            "${Telephony.Sms.DATE} > ?",
            arrayOf(timeMillis.toString()),
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

    private fun syncContacts(context: Context) {
        info("Synchronizing contacts")

        val contacts = getContacts(context)
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