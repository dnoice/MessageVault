/*
 * ✒ Metadata
 *     - Title: Passphrase Dialog (Message Vault Edition - v1.0)
 *     - File Name: PassphraseDialog.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/PassphraseDialog.kt
 *     - Artifact Type: library
 *     - Version: 1.0.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial: asks for the export passphrase at delivery time, with confirmation and an unambiguous warning that it is unrecoverable.
 *
 * ✒ Description:
 *     Collects the passphrase used to seal an export, at the moment of sealing. The
 *     passphrase is never persisted anywhere — this dialog hands it straight to the
 *     encryptor, which wipes it after deriving the key. Requires the passphrase twice,
 *     because a typo in a write-only secret produces an archive nobody can ever open.
 *
 * ✒ Key Features:
 *     - Confirm field with live mismatch feedback; Seal stays disabled until both match and meet a minimum length.
 *     - PasswordVisualTransformation keeps the secret off-screen for shoulder-surfers and screen recordings.
 *     - Blunt warning copy: there is no recovery path, and pretending otherwise would be dishonest.
 *     - Hands back a CharArray (not a String) so the caller can zero it after use — Strings are immutable and linger in the heap.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose Material3 (AlertDialog, OutlinedTextField).
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private const val MIN_LENGTH = 8

@Composable
fun PassphraseDialog(
    onDismiss: () -> Unit,
    onConfirm: (CharArray) -> Unit
) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }

    val longEnough = first.length >= MIN_LENGTH
    val matches = first == second
    val ready = longEnough && matches

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seal this export") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "This archive will be encrypted with a passphrase you choose. " +
                        "It is never stored — if you lose it, the archive can never be opened again, " +
                        "by anyone, including you.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = first,
                    onValueChange = { first = it },
                    label = { Text("Passphrase") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = second,
                    onValueChange = { second = it },
                    label = { Text("Confirm passphrase") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (first.isNotEmpty() && !longEnough) {
                    Text(
                        "Use at least $MIN_LENGTH characters.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (second.isNotEmpty() && !matches) {
                    Text(
                        "The two entries don't match.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = ready,
                onClick = { onConfirm(first.toCharArray()) }
            ) { Text("Seal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
