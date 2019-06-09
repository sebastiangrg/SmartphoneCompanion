package com.project.smartphonecompanionandroid.services


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.smartphonecompanionandroid.utils.FCMUtils
import com.project.smartphonecompanionandroid.utils.SyncUtils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class FCMService : FirebaseMessagingService(), AnkoLogger {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        info("Received message ${remoteMessage.data}")

        SyncUtils.startWorker(remoteMessage.data, 0)
    }

    override fun onNewToken(token: String?) {
        info("New token: $token")
        FCMUtils.saveMobileTokenToFirebase(token)
    }
}