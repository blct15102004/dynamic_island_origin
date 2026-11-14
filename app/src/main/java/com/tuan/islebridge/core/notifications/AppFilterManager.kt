package com.tuan.islebridge.core.notifications

object AppFilterManager {

    private val FINANCIAL_KEYWORDS = listOf(
        "bank", "banking", "wallet", "crypto", "pay", "bidv", "vietcombank", "techcombank",
        "mbbank", "acb", "vpbank", "agribank", "tpbank", "zalopay", "momo", "shopeepay",
        "viettelmoney", "binance", "coinbase"
    )

    private val PROTECTED_PACKAGES = setOf(
        "com.google.android.apps.walletnfcrel",
        "com.android.vending",
        "com.vnpay.bidv",
        "com.vng.zalopay",
        "com.mservice.momotransfer",
        "com.VCB",
        "com.mbmobile",
        "vn.com.techcombank.bb.app",
        "com.acb.mobilebanking"
    )

    fun isFinancialApp(packageName: String): Boolean {
        if (PROTECTED_PACKAGES.contains(packageName)) {
            return true
        }
        val lower = packageName.lowercase()
        return FINANCIAL_KEYWORDS.any { lower.contains(it) }
    }

    fun isAllowedForMirroring(
        packageName: String,
        allowedPackages: Set<String>,
        blockedPackages: Set<String>
    ): Boolean {
        // Hard block on IsleBridge itself to prevent infinite loop mirroring
        if (packageName == "com.tuan.islebridge") return false

        // Check explicit blocklist
        if (blockedPackages.contains(packageName)) return false

        // Check if financial
        if (isFinancialApp(packageName)) return false

        // If allowlist is not empty, must be in allowlist; otherwise allow if not blocked
        return if (allowedPackages.isNotEmpty()) {
            allowedPackages.contains(packageName)
        } else {
            true
        }
    }
}

