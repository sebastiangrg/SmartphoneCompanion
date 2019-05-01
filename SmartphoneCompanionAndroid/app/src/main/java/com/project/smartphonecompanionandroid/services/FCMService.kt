package com.project.smartphonecompanionandroid.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.SMSUtils

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCMService", remoteMessage.data["sync"])

        // sync message
        val sync = remoteMessage.data["sync"]?.split("|")
        sync?.forEach {
            when (it) {
                "conversations" -> SMSUtils.syncConversations(this.applicationContext)
                "received" -> SMSUtils.syncReceived(this.applicationContext)
                "sent" -> SMSUtils.syncSent(this.applicationContext)
                //"contacts" -> ContactUtils.syncContacts()
                else -> Log.d("FMCService", "Invalid operation $it")
            }
        }
    }

    override fun onNewToken(token: String?) {
        FCMUtils.saveMobileTokenToFirebase(token)
    }
}