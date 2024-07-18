package com.simplemobiletools.musicplayer.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.simplemobiletools.musicplayer.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.musicplayer.compose.alert_dialog.dialogBorder
import com.simplemobiletools.musicplayer.compose.alert_dialog.dialogContainerColor
import com.simplemobiletools.musicplayer.compose.alert_dialog.dialogElevation
import com.simplemobiletools.musicplayer.compose.alert_dialog.dialogShape
import com.simplemobiletools.musicplayer.compose.alert_dialog.dialogTextColor
import com.simplemobiletools.musicplayer.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.musicplayer.compose.components.LinkifyTextComponent
import com.simplemobiletools.musicplayer.compose.extensions.MyDevices
import com.simplemobiletools.musicplayer.compose.extensions.getActivity
import com.simplemobiletools.musicplayer.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.musicplayer.compose.theme.AppThemeSurface
import com.simplemobiletools.musicplayer.compose.theme.SimpleTheme
import com.simplemobiletools.musicplayer.R
import com.simplemobiletools.musicplayer.databinding.DialogDonateBinding
import com.simplemobiletools.musicplayer.extensions.applyColorFilter
import com.simplemobiletools.musicplayer.extensions.fromHtml
import com.simplemobiletools.musicplayer.extensions.getAlertDialogBuilder
import com.simplemobiletools.musicplayer.extensions.getProperTextColor
import com.simplemobiletools.musicplayer.extensions.launchViewIntent
import com.simplemobiletools.musicplayer.extensions.setupDialogStuff

class DonateDialog(val activity: Activity) {
    init {
        val view = DialogDonateBinding.inflate(activity.layoutInflater, null, false).apply {
            dialogDonateImage.applyColorFilter(activity.getProperTextColor())
            dialogDonateText.text = Html.fromHtml(activity.getString(R.string.donate_short))
            dialogDonateText.movementMethod = LinkMovementMethod.getInstance()
            dialogDonateImage.setOnClickListener {
                activity.launchViewIntent(R.string.thank_you_url)
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.purchase) { _, _ -> activity.launchViewIntent(R.string.thank_you_url) }
            .setNegativeButton(R.string.later, null)
            .apply {
                activity.setupDialogStuff(view.root, this, cancelOnTouchOutside = false)
            }
    }
}

@Composable
fun DonateAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier
) {
    val localContext = LocalContext.current.getActivity()
    val donateIntent = {
        localContext.launchViewIntent(R.string.thank_you_url)
    }
    androidx.compose.material3.AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
        onDismissRequest = {},
        shape = dialogShape,
        tonalElevation = dialogElevation,
        dismissButton = {
            TextButton(onClick = {
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.later))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                donateIntent()
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.purchase))
            }
        },
        title = {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier
                        .size(SimpleTheme.dimens.icon.large)
                        .clickable(
                            indication = null,
                            interactionSource = rememberMutableInteractionSource(),
                            onClick = {
                                donateIntent()
                                alertDialogState.hide()
                            }
                        ),
                    colorFilter = ColorFilter.tint(dialogTextColor)
                )
            }
        },
        text = {
            val source = stringResource(id = R.string.donate_short)
            LinkifyTextComponent(
                fontSize = 16.sp,
                removeUnderlines = false,
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER,
                modifier = Modifier.fillMaxWidth()
            ) {
                source.fromHtml()
            }
        }
    )
}

@Composable
@MyDevices
private fun DonateAlertDialogPreview() {
    AppThemeSurface {
        DonateAlertDialog(alertDialogState = rememberAlertDialogState())
    }
}


