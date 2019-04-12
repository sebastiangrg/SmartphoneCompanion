package com.project.smartphonecompanionandroid.utils

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.google.android.material.snackbar.Snackbar
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.ui.activities.MainActivity

fun FragmentActivity.replaceWith(fragment: Fragment) {
    val containerId = when (this) {
        is MainActivity -> R.id.main_container
        else -> -1
    }
    supportFragmentManager.beginTransaction()
        .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .replace(containerId, fragment)
        .addToBackStack(fragment::class.java.simpleName)
        .commit()
}

fun Activity.clearBackStack() {
    val manager = (this as AppCompatActivity).supportFragmentManager
    if (manager.backStackEntryCount > 0) {
        val first = manager.getBackStackEntryAt(0)
        try {
            manager.popBackStack(first.id, POP_BACK_STACK_INCLUSIVE)
        } catch (ignored: IllegalStateException) {
        }
    }
}

fun Activity.goBack() {
    val manager = (this as AppCompatActivity).supportFragmentManager
    if (manager.backStackEntryCount > 0) {
        try {
            manager.popBackStackImmediate()
        } catch (ignored: IllegalStateException) {
        }
    }
}

fun Fragment.snackbar(
    message: String,
    action: ((View) -> Unit)? = null,
    actionText: String? = null
) {
    this.view?.let {
        Snackbar.make(it, message, Snackbar.LENGTH_LONG).setAction(actionText, action).show()
    }
}