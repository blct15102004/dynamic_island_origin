package com.tuan.islebridge

import com.tuan.islebridge.core.updater.SecurityVerifier
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SecurityVerifierTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSha256ComputationAndVerification() {
        val testFile = tempFolder.newFile("sample.apk")
        testFile.writeText("ISLEBRIDGE_SECURE_BINARY_CONTENT")

        val computedSha = SecurityVerifier.computeFileSha256(testFile)
        assertNotNull(computedSha)
        assertEquals(64, computedSha.length) // SHA-256 is 64 hex characters

        // Correct SHA passes
        assertTrue(SecurityVerifier.verifySha256(testFile, computedSha))

        // Tampered / incorrect SHA fails
        val fakeSha = "0000000000000000000000000000000000000000000000000000000000000000"
        assertFalse(SecurityVerifier.verifySha256(testFile, fakeSha))
    }

    @Test
    fun testSha256_RejectsBlank() {
        val testFile = tempFolder.newFile("empty.apk")
        testFile.writeText("content")
        assertFalse(SecurityVerifier.verifySha256(testFile, ""))
        assertFalse(SecurityVerifier.verifySha256(testFile, "   "))
    }
}

