package com.project.smartphonecompanionandroid.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.smartphonecompanionandroid.utils.ContactUtils
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.SMSUtils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class FCMService : FirebaseMessagingService(), AnkoLogger {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        info("Received message $remoteMessage")

        remoteMessage.data.entries.forEach {
            when (it.key) {
                "syncLastMessages" -> SMSUtils.syncLastMessages(this.applicationContext)
                "syncConversation" -> SMSUtils.syncConversation(this.applicationContext, it.value.toLong())
                "syncContacts" -> ContactUtils.syncContacts(this.applicationContext)
                else -> Log.d("FCMService", "Invalid operation $it")
            }
        }
    }

    override fun onNewToken(token: String?) {
        info("New token: $token")
        FCMUtils.saveMobileTokenToFirebase(token)
    }
}