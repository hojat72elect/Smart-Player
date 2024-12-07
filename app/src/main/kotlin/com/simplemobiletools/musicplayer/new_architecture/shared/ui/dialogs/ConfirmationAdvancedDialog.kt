package com.simplemobiletools.musicplayer.new_architecture.shared.ui.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.simplemobiletools.musicplayer.R
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.compose.alert_dialog.dialogBorder
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.compose.alert_dialog.dialogContainerColor
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.compose.alert_dialog.dialogElevation
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.compose.alert_dialog.dialogShape
import com.simplemobiletools.musicplayer.new_architecture.shared.ui.compose.alert_dialog.dialogTextColor
import com.simplemobiletools.musicplayer.databinding.DialogMessageBinding
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.setupDialogStuff

// similar fo ConfirmationDialog, but has a callback for negative button too
class ConfirmationAdvancedDialog(
    activity: Activity,
    message: String = "",
    messageId: Int = R.string.proceed_with_deletion,
    positive: Int = R.string.yes,
    negative: Int = R.string.no,
    private val cancelOnTouchOutside: Boolean = true,
    val callback: (result: Boolean) -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = DialogMessageBinding.inflate(activity.layoutInflater, null, false)
        view.message.text = message.ifEmpty { activity.resources.getString(messageId) }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { _, _ -> positivePressed() }

        if (negative != 0) {
            builder.setNegativeButton(negative) { _, _ -> negativePressed() }
        }

        if (!cancelOnTouchOutside) {
            builder.setOnCancelListener { negativePressed() }
        }

        builder.apply {
            activity.setupDialogStuff(
                view.root,
                this,
                cancelOnTouchOutside = cancelOnTouchOutside
            ) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun positivePressed() {
        dialog?.dismiss()
        callback(true)
    }

    private fun negativePressed() {
        dialog?.dismiss()
        callback(false)
    }
}

@Composable
fun ConfirmationAdvancedAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    message: String = "",
    messageId: Int? = R.string.proceed_with_deletion,
    positive: Int? = R.string.yes,
    negative: Int? = R.string.no,
    cancelOnTouchOutside: Boolean = true,
    callback: (result: Boolean) -> Unit
) {

    androidx.compose.material3.AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder,
        properties = DialogProperties(dismissOnClickOutside = cancelOnTouchOutside),
        onDismissRequest = {
            alertDialogState.hide()
            callback(false)
        },
        shape = dialogShape,
        tonalElevation = dialogElevation,
        dismissButton = {
            if (negative != null) {
                TextButton(onClick = {
                    alertDialogState.hide()
                    callback(false)
                }) {
                    Text(text = stringResource(id = negative))
                }
            }
        },
        confirmButton = {
            if (positive != null) {
                TextButton(onClick = {
                    alertDialogState.hide()
                    callback(true)
                }) {
                    Text(text = stringResource(id = positive))
                }
            }
        },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = message.ifEmpty { messageId?.let { stringResource(id = it) }.orEmpty() },
                fontSize = 16.sp,
                color = dialogTextColor,
            )
        }
    )
}


