# ✒ Metadata
#     - Title: ProGuard Rules (Message Vault Edition - v1.0)
#     - File Name: proguard-rules.pro
#     - Relative Path: app/proguard-rules.pro
#     - Artifact Type: config
#     - Version: 1.0.0
#     - Date: 2026-06-22
#     - Update: Monday, June 22, 2026
#     - Author: Dennis 'dendogg' Smaltz
#     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
#     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
#
# ✒ Description:
#     ProGuard/R8 rules for this personal sideload tool; the release build is optional and these rules keep the data model classes intact so serialization and reflection survive shrinking/obfuscation.
# ---------
-keep class com.digispace.messagevault.data.model.** { *; }
