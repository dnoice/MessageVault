#!/usr/bin/env python3
"""
✒ Metadata
    - Title: Archive Decryptor (Message Vault Edition - v1.0)
    - File Name: decrypt_mvault.py
    - Relative Path: tools/decrypt_mvault.py
    - Artifact Type: script
    - Version: 1.0.1
    - Date: 2026-07-20
    - Update: Monday, July 20, 2026
    - Author: Dennis 'dendogg' Smaltz
    - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
    - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!

✒ Changelog:
    - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Header correction: Title now carries the required project-edition suffix, a Key Features section was added, Compatible platforms was added to Other Important Information, and the wire format moved under Other Important Information instead of an ad-hoc section.
    - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial standalone decryptor for the .mvault format.

✒ Description:
    Decrypts a MessageVault .mvault archive back into the plain .zip, WITHOUT the app.
    This script is the whole point of choosing a passphrase over an Android Keystore key:
    the archive must outlive the phone, the app, and the vendor. If MessageVault vanished
    tomorrow, this file plus your passphrase is enough to get your messages back.

✒ Key Features:
    - Chunked streaming decrypt: a multi-hundred-MB archive is decrypted without ever being
      read into memory.
    - Self-describing input: the PBKDF2-HMAC-SHA256 salt and iteration count are read from
      the archive's own plaintext header, so nothing has to be remembered but the passphrase.
    - Authenticated output: AES-256-GCM verification means a wrong passphrase or a tampered
      file fails loudly at the end instead of silently producing garbage.
    - Zero coupling: depends on neither the app nor the phone — Python plus one package is
      the entire recovery path.

✒ Usage Instructions:
    pip install cryptography
    python decrypt_mvault.py 20260720_185150.mvault
    python decrypt_mvault.py archive.mvault -o restored.zip

✒ Other Important Information:
    - Dependencies: Python 3.8+, the `cryptography` package. hashlib/struct are stdlib.
    - Compatible platforms: Any OS with Python 3.8+ (Windows, macOS, Linux).
    - Wire format (all integers big-endian; header is plaintext, payload is encrypted):

        offset  size  meaning
        0       8     magic, ASCII "MVAULT01"
        8       1     KDF id — 0x01 = PBKDF2WithHmacSHA256
        9       4     iteration count (int)
        13      16    salt
        29      12    GCM nonce (IV)
        41      ...   AES-256-GCM ciphertext; final 16 bytes are the auth tag

      key = PBKDF2-HMAC-SHA256(passphrase, salt, iterations, dklen=32)
---------
"""

import argparse
import getpass
import hashlib
import struct
import sys
from pathlib import Path

try:
    from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
except ImportError:  # pragma: no cover
    sys.exit("This script needs the 'cryptography' package:  pip install cryptography")

MAGIC = b"MVAULT01"
KDF_PBKDF2_SHA256 = 1
HEADER_LEN = 41
TAG_LEN = 16
CHUNK = 1024 * 1024


def parse_header(raw: bytes):
    if len(raw) < HEADER_LEN or raw[:8] != MAGIC:
        sys.exit("Not a MessageVault archive (bad magic).")
    kdf = raw[8]
    if kdf != KDF_PBKDF2_SHA256:
        sys.exit(f"Unsupported key derivation id: {kdf}")
    iterations = struct.unpack(">I", raw[9:13])[0]
    salt = raw[13:29]
    iv = raw[29:41]
    return iterations, salt, iv


def main() -> None:
    ap = argparse.ArgumentParser(description="Decrypt a MessageVault .mvault archive.")
    ap.add_argument("archive", type=Path, help="the .mvault file")
    ap.add_argument("-o", "--out", type=Path, help="output .zip (default: alongside input)")
    args = ap.parse_args()

    src: Path = args.archive
    if not src.is_file():
        sys.exit(f"No such file: {src}")
    dest: Path = args.out or src.with_suffix(".zip")

    total = src.stat().st_size
    payload_len = total - HEADER_LEN - TAG_LEN
    if payload_len < 0:
        sys.exit("Archive is truncated.")

    with src.open("rb") as f:
        iterations, salt, iv = parse_header(f.read(HEADER_LEN))

        # The GCM tag is the final 16 bytes; GCM needs it up front to verify.
        f.seek(total - TAG_LEN)
        tag = f.read(TAG_LEN)
        f.seek(HEADER_LEN)

        passphrase = getpass.getpass("Passphrase: ").encode("utf-8")
        key = hashlib.pbkdf2_hmac("sha256", passphrase, salt, iterations, dklen=32)

        decryptor = Cipher(algorithms.AES(key), modes.GCM(iv, tag)).decryptor()
        remaining = payload_len
        with dest.open("wb") as out:
            while remaining > 0:
                block = f.read(min(CHUNK, remaining))
                if not block:
                    break
                remaining -= len(block)
                out.write(decryptor.update(block))
            try:
                out.write(decryptor.finalize())
            except Exception:
                out.close()
                dest.unlink(missing_ok=True)
                sys.exit("Decryption failed: wrong passphrase, or the archive was modified.")

    print(f"Decrypted -> {dest}")


if __name__ == "__main__":
    main()
