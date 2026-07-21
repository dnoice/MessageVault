/*
 * ✒ Metadata
 *     - Title: Lock Screen (Message Vault Edition - v1.0)
 *     - File Name: LockScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/LockScreen.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: the locked face eases in rather than appearing mid-frame, the mark is labelled for screen readers, a failed authentication now reads in crimson while the resting invitation stays neutral, and the Unlock control uses the shared button treatment and touch target.
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — FOLD FIX: unlock state was plain remember, so every configuration change (rotation, dark-mode switch, and every fold/unfold on the Fold 6) recreated the activity, reset both `unlocked` and `lockedAt`, and re-prompted — the grace window couldn't even soften it because its timestamp died too. `unlocked` / `lockedAt` are now rememberSaveable, surviving a config change but not process death. Added an onLocked callback so the host can drop pending sensitive work when the door closes.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — DEADLOCK FIX: the prompt was triggered by the locked-state change, which fired it while the activity was stopped (where a prompt cannot show). It errored, and since the state hadn't changed on return nothing re-triggered it — leaving an unopenable door. Prompting now happens on ON_RESUME only, the Unlock button force-retries, and a short grace window keeps Share / folder-picker round-trips from re-prompting.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial vault door: biometric / device-credential gate that hides the app's contents until unlocked and re-locks when backgrounded.
 *
 * ✒ Description:
 *     The vault door. When the lock is enabled, nothing inside the app renders until the
 *     user authenticates — the archive is every message they have ever sent, so the gate
 *     wraps the whole UI rather than a single screen. Re-locks whenever the app leaves
 *     the foreground, so an unlocked session cannot be resumed by whoever picks the phone
 *     up next.
 *
 * ✒ Key Features:
 *     - BiometricPrompt with DEVICE_CREDENTIAL fallback: fingerprint/face where available, PIN or pattern where not — never a passphrase this app invented and stores.
 *     - Locks on ON_STOP: leaving for the launcher, the recents switcher, or another app closes the door behind you.
 *     - Content is never composed while locked, so the message list cannot flash on screen before the prompt appears.
 *     - Manual retry: a dismissed prompt leaves an Unlock button rather than an unreachable dead end.
 *
 * ✒ Other Important Information:
 *     - Dependencies: androidx.biometric (BiometricPrompt, BiometricManager); requires a FragmentActivity host; Jetpack Compose.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import android.os.SystemClock
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.Image
import com.digispace.messagevault.R

/** Authenticators we accept: strong/weak biometrics, falling back to the device PIN. */
private const val ALLOWED =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

/**
 * How long the door stays openable after backgrounding. Sharing a run or picking a
 * folder launches another activity, so a zero window would re-prompt on every delivery.
 * Short enough that a phone left on a table still closes itself.
 */
private const val GRACE_MILLIS = 15_000L

/** True when this device can actually prompt for something. */
fun canAuthenticate(context: android.content.Context): Boolean =
    BiometricManager.from(context).canAuthenticate(ALLOWED) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Gates [content] behind authentication when [enabled].
 *
 * While locked the content is not composed at all — the archive must never render
 * behind the prompt, even for a frame.
 *
 * @param onLocked fired when the door closes. The host uses it to drop anything sensitive
 *        it is holding outside the gate (a pending passphrase request, say), so a locked
 *        vault can never resume it.
 */
@Composable
fun VaultGate(enabled: Boolean, onLocked: () -> Unit = {}, content: @Composable () -> Unit) {
    if (!enabled) {
        content()
        return
    }

    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val locked = rememberUpdatedState(onLocked)
    // Saved instance state, not plain remember: MainActivity declares no
    // android:configChanges, so a rotation — or a fold/unfold on the Fold 6 this app is
    // built for — destroys and recreates the activity. With plain remember both `unlocked`
    // and `lockedAt` reset, so the grace window below couldn't even soften it and every
    // unfold threw a fresh prompt. Saved state survives a config change but NOT process
    // death, which is exactly the security boundary we want.
    var unlocked by rememberSaveable { mutableStateOf(false) }
    var lockedAt by rememberSaveable { mutableStateOf(0L) }
    // Deliberately NOT saved: an in-flight prompt does not survive recreation.
    var prompting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    /**
     * @param force ignores the in-flight guard. The manual Unlock button must always be
     *        able to re-prompt: if a prompt were ever left marked in-flight, a guarded-only
     *        path would leave the user with no way back in short of killing the app.
     */
    fun authenticate(force: Boolean = false) {
        if (activity == null) return
        if (prompting && !force) return
        prompting = true
        message = null
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    prompting = false
                    unlocked = true
                }

                override fun onAuthenticationError(code: Int, err: CharSequence) {
                    prompting = false
                    message = err.toString()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Message Vault")
                .setSubtitle("Your archive is locked")
                .setAllowedAuthenticators(ALLOWED)
                .build()
        )
    }

    // Lock when we leave the foreground; prompt only once we're back in it.
    //
    // These MUST be separate events. Re-locking on ON_STOP and prompting off the
    // resulting state change fired the prompt while the activity was stopped, where it
    // cannot be shown — it errored instantly, and because the locked state hadn't
    // *changed* on return there was nothing left to re-trigger it. That was the stuck
    // door. ON_RESUME is the only place a prompt can actually appear.
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (unlocked) lockedAt = SystemClock.elapsedRealtime()
                    unlocked = false
                    locked.value()
                }
                Lifecycle.Event.ON_RESUME -> if (!unlocked) {
                    // Short grace window: the app's own Share sheet and folder picker are
                    // separate activities, so every delivery would otherwise demand a new
                    // unlock the moment you came back.
                    val elapsed = SystemClock.elapsedRealtime() - lockedAt
                    if (lockedAt > 0L && elapsed < GRACE_MILLIS) unlocked = true
                    else authenticate()
                }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    if (unlocked) {
        content()
    } else {
        LockedFace(message = message, onUnlock = { authenticate(force = true) })
    }
}

@Composable
private fun LockedFace(message: String?, onUnlock: () -> Unit) {
    // The door is the first thing seen on a cold start; it settles in rather than
    // snapping into existence.
    val appear = remember { MutableTransitionState(false).apply { targetState = true } }
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visibleState = appear,
                enter = fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 14 }
            ) {
                Column(
                    Modifier.widthIn(max = MvContentWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
                ) {
                    Image(
                        painter = painterResource(R.drawable.mv_mark),
                        contentDescription = "Message Vault",
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        "Message Vault is locked",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    // A failed or cancelled authentication is a genuine error; the resting
                    // invitation is not, and must not borrow crimson.
                    Text(
                        message ?: "Unlock to open your archive.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    MvPrimaryButton("Unlock", Modifier.widthIn(max = 260.dp), onClick = onUnlock)
                }
            }
        }
    }
}

/** BiometricPrompt insists on a FragmentActivity host; unwrap whatever context we have. */
private tailrec fun android.content.Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is android.content.ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
