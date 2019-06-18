package com.project.smartphonecompanionandroid.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import android.provider.Telephony
import com.project.smartphonecompanionandroid.utils.SyncUtils


class SMSBroadcastReceiver : BroadcastReceiver(), AnkoLogger {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SMS_RECEIVED_ACTION -> {
                info("SMS RECEIVED")
                Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach {
                    info("SMS from ${it.displayOriginatingAddress} - content: ${it.messageBody}")

                    SyncUtils.sendNewMessageNotification(it.displayOriginatingAddress,it.messageBody)
                    SyncUtils.syncLastMessages()
                    SyncUtils.syncConversationsSince(it.timestampMillis)
                }
            }
            else -> debug("Unrecognised intent action ${intent.action}")
        }
    }
}