package com.project.smartphonecompanionandroid.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.SyncUtils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info

class FCMService : FirebaseMessagingService(), AnkoLogger {
    companion object {
        const val FCM_OPERATION_SYNC_LAST_MESSAGES = "1"
        const val FCM_OPERATION_SYNC_CONVERSATION = "2"
        const val FCM_OPERATION_SYNC_CONTACTS = "3"
        const val FCM_OPERATION_SEND_SMS = "4"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        info("Received message ${remoteMessage.data}")

        when (val operation = remoteMessage.data["operation"]) {
            FCM_OPERATION_SYNC_LAST_MESSAGES -> SyncUtils.syncLastMessages()
            FCM_OPERATION_SYNC_CONVERSATION -> {
                val thread = remoteMessage.data["thread"]

                thread?.let {
                    SyncUtils.syncConversation(thread.toLong())
                }
            }
            FCM_OPERATION_SYNC_CONTACTS -> SyncUtils.syncContacts()
            FCM_OPERATION_SEND_SMS -> {
                val phoneNumber = remoteMessage.data["phoneNumber"]
                val content = remoteMessage.data["content"]

                if (phoneNumber != null && content != null) {
                    SyncUtils.sendSMSMessage(phoneNumber, content)
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