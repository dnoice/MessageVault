/*
 * ✒ Metadata
 *     - Title: Notifications (Message Vault Edition - v1.0)
 *     - File Name: Notifications.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/util/Notifications.kt
 *     - Artifact Type: library
 *     - Version: 1.0.1
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — complete() takes an optional PendingIntent, so a job that finishes while the app is backgrounded can hand the user a tappable action instead of silently dropping the one it couldn't launch.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial channel setup plus ongoing/complete notification helpers, so long zips report progress instead of looking frozen.
 *
 * ✒ Description:
 *     Thin wrapper over the platform notification APIs for the app's long-running
 *     jobs. Zipping a run with hundreds of attachments can take a while, and until
 *     the share sheet appears there is nothing on screen to say the tap worked — a
 *     user can easily assume it failed and leave. These helpers put a persistent,
 *     honest status in the shade that survives leaving the app.
 *
 * ✒ Key Features:
 *     - One low-importance channel: progress belongs in the shade, not buzzing the phone.
 *     - ongoing(): non-dismissable indeterminate progress while work runs.
 *     - complete(): replaces the progress entry with a final, dismissable result.
 *     - Permission-safe: POST_NOTIFICATIONS may be denied (API 33+), so every post is
 *       wrapped — a missing permission must degrade silently, never crash the job it reports on.
 *
 * ✒ Other Important Information:
 *     - Dependencies: androidx.core NotificationCompat / NotificationManagerCompat; android.app.NotificationChannel/Manager; R.drawable.ic_notification.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.digispace.messagevault.R

object Notifications {

    /** Progress/results for delivery work (zip, copy). Low importance: no sound. */
    const val CHANNEL_DELIVERY = "delivery"

    /** The export run itself — carries the foreground service. */
    const val CHANNEL_EXPORT = "exports"

    /** Stable ids so an update replaces the previous entry instead of stacking. */
    const val ID_DELIVERY = 2001

    /** The foreground/progress notification for a run. */
    const val ID_EXPORT = 2002

    /**
     * The run's RESULT. Deliberately a different id from [ID_EXPORT]: the progress entry
     * is the service's foreground notification, and stopForeground(STOP_FOREGROUND_REMOVE)
     * deletes whatever carries that id — a result posted under it would vanish instantly.
     */
    const val ID_EXPORT_DONE = 2003

    /** Safe to call repeatedly; creating an existing channel is a no-op. */
    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DELIVERY,
                "Export delivery",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Progress while zipping, sharing, or copying an export" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EXPORT,
                "Export runs",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Progress while messages are being archived" }
        )
    }

    /**
     * The foreground notification for a running export. Determinate once a total is
     * known, indeterminate while counting. Built (not posted) so the service can hand
     * it straight to startForeground().
     */
    fun exportProgress(
        context: Context,
        phase: String,
        processed: Int,
        total: Int,
        cancelIntent: PendingIntent?
    ): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_EXPORT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Archiving messages")
            .setContentText(if (total > 0) "$phase · $processed of $total" else phase)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total.coerceAtLeast(0), processed, total <= 0)
        if (cancelIntent != null) {
            builder.addAction(0, "Cancel", cancelIntent)
        }
        return builder.build()
    }

    /** Indeterminate, ongoing progress — the job is still running. */
    fun ongoing(context: Context, id: Int, title: String, text: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_DELIVERY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
        post(context, id, n)
    }

    /**
     * Final state — dismissable, no progress bar.
     *
     * @param contentIntent optional tap target. Android blocks background activity starts,
     *        so when a job finishes while the app is not in the foreground the only honest
     *        way to hand the user a chooser is to park it here and let them tap it.
     */
    fun complete(
        context: Context,
        id: Int,
        title: String,
        text: String,
        contentIntent: PendingIntent? = null
    ) {
        val n = NotificationCompat.Builder(context, CHANNEL_DELIVERY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setAutoCancel(true)
            .apply { if (contentIntent != null) setContentIntent(contentIntent) }
            .build()
        post(context, id, n)
    }

    /** Final state for an export run, on the export channel. */
    fun exportComplete(context: Context, title: String, text: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_EXPORT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
        post(context, ID_EXPORT_DONE, n)
    }

    fun cancel(context: Context, id: Int) {
        runCatching { NotificationManagerCompat.from(context).cancel(id) }
    }

    /**
     * POST_NOTIFICATIONS (API 33+) may be denied, and notify() throws SecurityException
     * when it is. Reporting on a job must never be able to break the job.
     */
    private fun post(context: Context, id: Int, notification: android.app.Notification) {
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }
}
