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

                    SyncUtils.startWorker(mapOf("operation" to SyncUtils.SYNC_LAST_MESSAGES), 2000)
                    SyncUtils.startWorker(
                        mapOf(
                            "operation" to SyncUtils.SYNC_CONVERSATIONS_SINCE,
                            "time" to it.timestampMillis.toString()
                        ), 2000
                    )
                }
            }
            else -> debug("Unrecognised intent action ${intent.action}")
        }
    }
}