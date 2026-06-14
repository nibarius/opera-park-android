package se.barsk.park.mainui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import se.barsk.park.R

/**
 * A dialog shown to the user when starting the app the first time that asks the user to
 * opt in to crash report collection. It is non-cancelable; the user must choose an option.
 */
@Composable
fun PrivacyOnboardingDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val url = stringResource(id = R.string.url_privacy_statement)
    // The message string contains an <a href> link to the privacy statement; parse the HTML
    // into an AnnotatedString so the link is clickable.
    val message = AnnotatedString.fromHtml(
        htmlString = stringResource(id = R.string.ppo_dialog_message, url),
        linkStyles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        )
    )
    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.92f),
        onDismissRequest = { /* non-cancelable, handled via DialogProperties */ },
        title = { Text(text = stringResource(id = R.string.ppo_dialog_title)) },
        text = {
            // Make the message scrollable so it stays readable when the dialog is short,
            // e.g. in landscape orientation.
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = message)
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(id = R.string.ppo_dialog_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(id = R.string.ppo_dialog_negative))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            // Opt out of the platform's narrow default width so the dialog can use more of
            // the available screen width (noticeable in landscape); width is set above.
            usePlatformDefaultWidth = false
        )
    )
}
