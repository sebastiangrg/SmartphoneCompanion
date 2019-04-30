package com.project.smartphonecompanionandroid.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

object FCMUtils {

    fun saveMobileTokenToFirebase(mobileToken: String? = null) {
        fun saveToFirebase(token: String) {
            val firebaseDatabase = FirebaseDatabase.getInstance()
            val uid = FirebaseAuth.getInstance().uid

            if (uid != null) {
                Log.d("FCMUtils", uid)
                firebaseDatabase.reference
                    .child("users")
                    .child(uid)
                    .child("mobileToken")
                    .setValue(token)
                    .addOnCanceledListener { Log.d("FCMUtils", "Canceled") }
                    .addOnSuccessListener { Log.d("FCMUtils", "Success") }
                    .addOnFailureListener { Log.d("FCMUtils", it.message) }
            }
        }

        if (mobileToken !== null) {
            saveToFirebase(mobileToken)
        } else {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.d("FCMUtils", "getInstanceId failed", task.exception)
                    } else {
                        val token = task.result?.token

                        token?.let {
                            Log.d("FCMUtils", token)

                            saveToFirebase(it)
                        }
                    }
                }
                .addOnFailureListener { Log.d("FCMUtils", it.message) }
        }
    }
}