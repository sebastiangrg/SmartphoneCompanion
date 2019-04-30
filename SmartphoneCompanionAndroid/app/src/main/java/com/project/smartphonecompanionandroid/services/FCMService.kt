package com.project.smartphonecompanionandroid.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.smartphonecompanionandroid.utils.FCMUtils

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCMService", remoteMessage.data.toString())
    }

    override fun onNewToken(token: String?) {
        FCMUtils.saveMobileTokenToFirebase(token)
    }
}