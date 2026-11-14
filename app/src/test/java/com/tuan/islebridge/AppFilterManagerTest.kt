package com.tuan.islebridge

import com.tuan.islebridge.core.notifications.AppFilterManager
import org.junit.Assert.*
import org.junit.Test

class AppFilterManagerTest {

    @Test
    fun testIsFinancialApp_DetectsKnownBanks() {
        assertTrue(AppFilterManager.isFinancialApp("com.vnpay.bidv"))
        assertTrue(AppFilterManager.isFinancialApp("com.VCB"))
        assertTrue(AppFilterManager.isFinancialApp("com.mservice.momotransfer"))
        assertTrue(AppFilterManager.isFinancialApp("com.vng.zalopay"))
        assertTrue(AppFilterManager.isFinancialApp("vn.com.techcombank.bb.app"))
    }

    @Test
    fun testIsFinancialApp_PassesNormalApps() {
        assertFalse(AppFilterManager.isFinancialApp("com.spotify.music"))
        assertFalse(AppFilterManager.isFinancialApp("com.google.android.youtube"))
        assertFalse(AppFilterManager.isFinancialApp("com.facebook.katana"))
    }

    @Test
    fun testIsAllowedForMirroring_BlocksSelf() {
        val allowed = AppFilterManager.isAllowedForMirroring(
            packageName = "com.tuan.islebridge",
            allowedPackages = emptySet(),
            blockedPackages = emptySet()
        )
        assertFalse("IsleBridge itself must never be mirrored to prevent loops", allowed)
    }

    @Test
    fun testIsAllowedForMirroring_BlocksFinancial() {
        val allowed = AppFilterManager.isAllowedForMirroring(
            packageName = "com.vnpay.bidv",
            allowedPackages = emptySet(),
            blockedPackages = emptySet()
        )
        assertFalse("Financial app must be blocked by default", allowed)
    }

    @Test
    fun testIsAllowedForMirroring_RespectsBlocklist() {
        val blocked = setOf("com.noisy.app")
        val allowed = AppFilterManager.isAllowedForMirroring(
            packageName = "com.noisy.app",
            allowedPackages = emptySet(),
            blockedPackages = blocked
        )
        assertFalse(allowed)
    }

    @Test
    fun testIsAllowedForMirroring_RespectsAllowlist() {
        val allowlist = setOf("com.spotify.music", "com.whatsapp")
        assertTrue(AppFilterManager.isAllowedForMirroring("com.spotify.music", allowlist, emptySet()))
        assertFalse(AppFilterManager.isAllowedForMirroring("com.telegram.messenger", allowlist, emptySet()))
    }
}

