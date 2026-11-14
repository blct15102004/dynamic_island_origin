package com.tuan.islebridge.core.updater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object SecurityVerifier {

    fun computeFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun verifySha256(file: File, expectedSha256: String): Boolean {
        if (expectedSha256.isBlank()) return false
        val actualSha256 = computeFileSha256(file)
        return actualSha256.equals(expectedSha256.trim(), ignoreCase = true)
    }

    fun verifyPackageIdentity(context: Context, apkFile: File, expectedPackage: String): Boolean {
        return try {
            val pm = context.packageManager
            val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
            archiveInfo?.packageName == expectedPackage
        } catch (_: Exception) {
            false
        }
    }

    fun verifyApkCertificate(context: Context, apkFile: File, expectedCertSha256: String?): Boolean {
        if (expectedCertSha256.isNullOrBlank() || expectedCertSha256 == "PINNED_CERT_SHA256_PLACEHOLDER") {
            // In debug/development mode if no pinned cert is configured, require identical signature to installed package
            return verifyMatchingSignatureWithInstalled(context, apkFile)
        }

        return try {
            val certFingerprint = getApkCertFingerprint(context, apkFile) ?: return false
            certFingerprint.equals(expectedCertSha256.trim(), ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    private fun verifyMatchingSignatureWithInstalled(context: Context, apkFile: File): Boolean {
        return try {
            val pm = context.packageManager
            val installedInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            val archiveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
            }

            if (archiveInfo == null) return false

            val installedDigest = computeSignaturesDigest(installedInfo)
            val archiveDigest = computeSignaturesDigest(archiveInfo)

            installedDigest == archiveDigest
        } catch (_: Exception) {
            false
        }
    }

    private fun getApkCertFingerprint(context: Context, apkFile: File): String? {
        return try {
            val pm = context.packageManager
            val archiveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
            } ?: return null

            computeSignaturesDigest(archiveInfo)
        } catch (_: Exception) {
            null
        }
    }

    private fun computeSignaturesDigest(pkgInfo: PackageInfo): String? {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            pkgInfo.signatures
        } ?: return null

        if (signatures.isEmpty()) return null

        val digest = MessageDigest.getInstance("SHA-256")
        for (sig in signatures) {
            digest.update(sig.toByteArray())
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

