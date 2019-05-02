package com.project.smartphonecompanionandroid.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

object FCMUtils : AnkoLogger {

    fun saveMobileTokenToFirebase(mobileToken: String? = null) {
        fun saveToFirebase(token: String) {
            val firebaseDatabase = FirebaseDatabase.getInstance()
            val uid = FirebaseAuth.getInstance().uid

            if (uid != null) {
                info("Firebase UID: $uid")
                firebaseDatabase.reference
                    .child("users")
                    .child(uid)
                    .child("mobileToken")
                    .setValue(token)
            }
        }

        if (mobileToken !== null) {
            saveToFirebase(mobileToken)
        } else {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        warn("getInstanceId failed ${task.exception}")
                    } else {
                        val token = task.result?.token

                        token?.let {
                            info("Token: $token")
                            saveToFirebase(it)
                        }
                    }
                }
                .addOnFailureListener { warn("Error getting the token ${it.message})") }
        }
    }
}