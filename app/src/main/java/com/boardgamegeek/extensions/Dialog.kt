@file:JvmName("DialogUtils")

package com.boardgamegeek.extensions

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.boardgamegeek.R

fun FragmentActivity.showAndSurvive(dialog: DialogFragment) {
    val fragmentManager = supportFragmentManager
    showAndSurvive(dialog, fragmentManager)
}

fun Fragment.showAndSurvive(dialog: DialogFragment) {
    val fragmentManager = fragmentManager
    showAndSurvive(dialog, fragmentManager)
}

private fun showAndSurvive(dialog: DialogFragment, fragmentManager: FragmentManager?) {
    if (fragmentManager == null) return
    val tag = "dialog"

    val ft = fragmentManager.beginTransaction()
    val prev = fragmentManager.findFragmentByTag(tag)
    if (prev != null) ft.remove(prev)
    ft.addToBackStack(null)

    dialog.show(ft, tag)
}

// TODO - use showAndSurvive instead
fun FragmentActivity.showFragment(fragment: DialogFragment, tag: String) {
    val ft = supportFragmentManager.beginTransaction()
    val prev = supportFragmentManager.findFragmentByTag(tag)
    if (prev != null) ft.remove(prev)
    ft.addToBackStack(null)
    fragment.show(ft, tag)
}

interface OnDiscardListener {
    fun onDiscard()
}

@JvmOverloads
fun createDiscardDialog(
        activity: Activity,
        @StringRes objectResId: Int,
        isNew: Boolean, finishActivity: Boolean = true,
        @StringRes positiveButtonResId: Int = R.string.keep_editing,
        discardListener: OnDiscardListener? = null): Dialog {
    val messageFormat = activity.getString(if (isNew)
        R.string.discard_new_message
    else
        R.string.discard_changes_message)
    return activity.createThemedBuilder()
            .setMessage(String.format(messageFormat, activity.getString(objectResId).toLowerCase()))
            .setPositiveButton(positiveButtonResId, null)
            .setNegativeButton(R.string.discard) { _, _ ->
                discardListener?.onDiscard()
                if (finishActivity) {
                    activity.setResult(Activity.RESULT_CANCELED)
                    activity.finish()
                }
            }
            .setCancelable(true)
            .create()
}

fun Context.createThemedBuilder(): AlertDialog.Builder {
    return AlertDialog.Builder(this, R.style.Theme_bgglight_Dialog_Alert)
}

fun Context.showClickableAlertDialog(@StringRes titleResId: Int, @StringRes messageResId: Int, vararg formatArgs: Any) {
    val spannableMessage = SpannableString(getString(messageResId, *formatArgs))
    showClickableAlertDialog(spannableMessage, titleResId)
}

fun Context.showClickableAlertDialogPlural(@StringRes titleResId: Int, @PluralsRes messageResId: Int, quantity: Int, vararg formatArgs: Any) {
    val spannableMessage = SpannableString(resources.getQuantityString(messageResId, quantity, *formatArgs))
    showClickableAlertDialog(spannableMessage, titleResId)
}

private fun Context.showClickableAlertDialog(spannableMessage: SpannableString, titleResId: Int) {
    Linkify.addLinks(spannableMessage, Linkify.WEB_URLS)
    val dialog = AlertDialog.Builder(this)
            .setTitle(titleResId)
            .setMessage(spannableMessage)
            .show()
    dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
}
