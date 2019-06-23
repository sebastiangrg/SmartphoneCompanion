package com.project.smartphonecompanionandroid.utils

import android.content.Context
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.project.smartphonecompanionandroid.R
import com.project.smartphonecompanionandroid.ui.activities.MainActivity
import androidx.core.os.bundleOf

fun FragmentActivity.replaceFragment(fragment: Fragment, clearBackStack: Boolean = false) {
    val containerId = when (this) {
        is MainActivity -> R.id.main_container
        else -> {
            -1
        }
    }

    if (clearBackStack) {
        this.clearBackStack()
    }

    val transaction = supportFragmentManager.beginTransaction()
        .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .replace(containerId, fragment)

    if (!clearBackStack) {
        transaction.addToBackStack(null)
    }

    transaction.commit()
}

fun FragmentActivity.clearBackStack() {
    for (i in 0 until supportFragmentManager.backStackEntryCount) {
        supportFragmentManager.popBackStack()
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

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun Fragment.clearFocusAndCloseKeyboard() {
    val view = requireActivity().currentFocus

    view?.let {
        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            it.windowToken,
            0
        )
    }
}

fun Context.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

inline fun <reified T : Fragment> instanceOf(vararg params: Pair<String, Any>): T = T::class.java.newInstance().apply {
    arguments = bundleOf(*params)
}