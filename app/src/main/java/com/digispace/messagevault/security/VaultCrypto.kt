/*
 * ✒ Metadata
 *     - Title: Vault Crypto (Message Vault Edition - v1.0)
 *     - File Name: VaultCrypto.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/security/VaultCrypto.kt
 *     - Artifact Type: library
 *     - Version: 1.0.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial passphrase-based AES-256-GCM envelope for exports leaving the device, with a documented, tool-independent file format.
 *
 * ✒ Description:
 *     Encrypts a finished export so the copy that leaves the device — shared, or synced
 *     into OneDrive — is unreadable without the passphrase. Deliberately PASSPHRASE-based
 *     rather than Android-Keystore-based: a Keystore key dies with the phone, and an
 *     archive you cannot open in ten years is not an archive. Anything encrypted here can
 *     be decrypted by any tool that implements the documented format below.
 *
 * ✒ THE FILE FORMAT (.mvault) — write this down somewhere that is not this phone:
 *     Everything is big-endian. Header is plaintext; only the payload is encrypted.
 *
 *         offset  size  meaning
 *         0       8     magic, ASCII "MVAULT01"
 *         8       1     KDF id — 0x01 = PBKDF2WithHmacSHA256
 *         9       4     iteration count (int)
 *         13      16    salt
 *         29      12    GCM nonce (IV)
 *         41      ...   AES-256-GCM ciphertext, 16-byte auth tag appended by GCM
 *
 *     Key = PBKDF2-HMAC-SHA256(passphrase, salt, iterations, 256 bits).
 *     Plaintext = the run's .zip. tools/decrypt_mvault.py in this repo implements it.
 *
 * ✒ Key Features:
 *     - AES-256-GCM: authenticated encryption, so a corrupted or tampered archive fails loudly instead of decrypting to garbage.
 *     - PBKDF2-HMAC-SHA256 at a high iteration count, with a random per-file salt, so a weak passphrase is expensive to attack and two identical archives never produce identical files.
 *     - Random per-file nonce: reusing a nonce under one key is catastrophic for GCM, so it is generated fresh from SecureRandom every time.
 *     - Streaming: encrypts through CipherOutputStream, so a multi-hundred-MB archive never lands in memory — the same flat-memory rule the exporters follow.
 *     - Self-describing header: salt, nonce, and KDF parameters travel with the file, so nothing about decryption depends on this app's defaults.
 *
 * ✒ Other Important Information:
 *     - Dependencies: javax.crypto (AES/GCM, PBKDF2), java.security.SecureRandom.
 *     - The passphrase is never stored: it is requested at encryption time, used, and cleared.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.security

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object VaultCrypto {

    const val EXTENSION = "mvault"

    private const val MAGIC = "MVAULT01"
    private const val KDF_PBKDF2_SHA256: Int = 1
    private const val ITERATIONS = 250_000
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private const val KEY_BITS = 256

    /** Encrypts [source] into [dest] under [passphrase]. Streams; never buffers the file. */
    fun encrypt(source: File, dest: File, passphrase: CharArray) {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, deriveKey(passphrase, salt, ITERATIONS), GCMParameterSpec(TAG_BITS, iv))
        }

        dest.outputStream().buffered().use { out ->
            writeHeader(out, salt, iv)
            CipherOutputStream(out, cipher).use { encrypted ->
                source.inputStream().buffered().use { it.copyTo(encrypted) }
            }
        }
    }

    /**
     * Decrypts [source] into [dest]. Throws if the passphrase is wrong or the file was
     * tampered with — GCM authenticates, so a bad key fails rather than yielding garbage.
     */
    fun decrypt(source: File, dest: File, passphrase: CharArray) {
        source.inputStream().buffered().use { input ->
            val header = readHeader(input)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(
                    Cipher.DECRYPT_MODE,
                    deriveKey(passphrase, header.salt, header.iterations),
                    GCMParameterSpec(TAG_BITS, header.iv)
                )
            }
            CipherInputStream(input, cipher).use { decrypted ->
                dest.outputStream().buffered().use { decrypted.copyTo(it) }
            }
        }
    }

    private fun writeHeader(out: OutputStream, salt: ByteArray, iv: ByteArray) {
        out.write(MAGIC.toByteArray(Charsets.US_ASCII))
        out.write(KDF_PBKDF2_SHA256)
        out.write(ByteBuffer.allocate(4).putInt(ITERATIONS).array())
        out.write(salt)
        out.write(iv)
    }

    private class Header(val iterations: Int, val salt: ByteArray, val iv: ByteArray)

    private fun readHeader(input: InputStream): Header {
        val magic = ByteArray(MAGIC.length).also { input.readFully(it) }
        require(String(magic, Charsets.US_ASCII) == MAGIC) { "Not a MessageVault archive." }
        val kdf = input.read()
        require(kdf == KDF_PBKDF2_SHA256) { "Unsupported key derivation: $kdf" }
        val iterations = ByteBuffer.wrap(ByteArray(4).also { input.readFully(it) }).int
        val salt = ByteArray(SALT_BYTES).also { input.readFully(it) }
        val iv = ByteArray(IV_BYTES).also { input.readFully(it) }
        return Header(iterations, salt, iv)
    }

    private fun InputStream.readFully(buffer: ByteArray) {
        var read = 0
        while (read < buffer.size) {
            val n = read(buffer, read, buffer.size - read)
            require(n > 0) { "Archive header is truncated." }
            read += n
        }
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray, iterations: Int): SecretKey {
        val spec = PBEKeySpec(passphrase, salt, iterations, KEY_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
