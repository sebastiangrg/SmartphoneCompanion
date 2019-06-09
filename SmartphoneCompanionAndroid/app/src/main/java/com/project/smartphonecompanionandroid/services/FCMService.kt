package com.project.smartphonecompanionandroid.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.smartphonecompanionandroid.utils.ContactUtils
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.SMSUtils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info

class FCMService : FirebaseMessagingService(), AnkoLogger {
    companion object {
        private const val SYNC_LAST_MESSAGES = "SYNC_LAST_MESSAGES"
        private const val SYNC_CONVERSATION = "SYNC_CONVERSATION"
        private const val SYNC_CONTACTS = "SYNC_CONTACTS"
        private const val SEND_SMS = "SEND_SMS"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        info("Received message ${remoteMessage.data}")

        when (val operation = remoteMessage.data["operation"]) {
            SYNC_LAST_MESSAGES -> SMSUtils.syncLastMessages(this.applicationContext)
            SYNC_CONVERSATION -> {
                val thread = remoteMessage.data["thread"]

                thread?.let {
                    SMSUtils.syncConversation(this.applicationContext, thread.toLong())
                }
            }
            SYNC_CONTACTS -> ContactUtils.syncContacts(this.applicationContext)
            SEND_SMS -> {
                val phoneNumber = remoteMessage.data["phoneNumber"]
                val content = remoteMessage.data["content"]

                if (phoneNumber != null && content != null) {
                    SMSUtils.sendSMSMessage(phoneNumber, content)
                }
            }
            else -> debug("Invalid operation $operation")
        }
    }

    override fun onNewToken(token: String?) {
        info("New token: $token")
        FCMUtils.saveMobileTokenToFirebase(token)
    }
}