package com.project.smartphonecompanionandroid.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

object FCMUtils {
    private const val TAG = "FCMUtils"

    fun saveMobileTokenToFirebase(mobileToken: String? = null) {
        fun saveToFirebase(token: String) {
            val firebaseDatabase = FirebaseDatabase.getInstance()
            val uid = FirebaseAuth.getInstance().uid

            if (uid != null) {
                Log.d(TAG, uid)
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
                        Log.d(TAG, "getInstanceId failed", task.exception)
                    } else {
                        val token = task.result?.token

                        token?.let {
                            Log.d(TAG, token)

                            saveToFirebase(it)
                        }
                    }
                }
                .addOnFailureListener { Log.d(TAG, it.message) }
        }
    }
}