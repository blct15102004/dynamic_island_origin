package com.tuan.islebridge

import com.tuan.islebridge.core.otp.OtpDetectorEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OtpDetectorEngineTest {

    @Before
    fun setup() {
        OtpDetectorEngine.clearVolatileHistory()
    }

    @Test
    fun testValidOtpExtraction_Vietnamese() {
        val message = "Ma xac thuc OTP cua ban la 682914. Vui long khong chia se ma nay voi bat ky ai."
        val otp = OtpDetectorEngine.extractOtp("com.google.android.apps.messaging", "SMS", message)
        assertEquals("682914", otp)
    }

    @Test
    fun testValidOtpExtraction_English() {
        val message = "Your verification code is 5821. It expires in 10 minutes."
        val otp = OtpDetectorEngine.extractOtp("com.google.android.apps.messaging", "Auth", message)
        assertEquals("5821", otp)
    }

    @Test
    fun testValidOtpExtraction_8Digits() {
        val message = "Your one-time passcode: 12345678."
        val otp = OtpDetectorEngine.extractOtp("com.google.android.apps.messaging", "Security", message)
        assertEquals("12345678", otp)
    }

    @Test
    fun testRejection_FinancialApp() {
        val message = "Ma OTP chuyen tien la 481920."
        val otp = OtpDetectorEngine.extractOtp("com.vnpay.bidv", "BIDV SmartBanking", message)
        assertNull("Financial apps must be blocked by default", otp)
    }

    @Test
    fun testRejection_FalsePositive_YearDate() {
        // "Năm 2024" should not be treated as OTP
        val message = "Chuc mung nam 2024 an khang thinh vuong!"
        val otp = OtpDetectorEngine.extractOtp("com.google.android.apps.messaging", "SMS", message)
        assertNull(otp)
    }

    @Test
    fun testRejection_FalsePositive_PhoneNumber() {
        val message = "Ma OTP cua ban lien he 0912345678 de biet them."
        val otp = OtpDetectorEngine.extractOtp("com.google.android.apps.messaging", "Support", message)
        // 0912345678 is a phone number (10 digits) and must not be extracted
        assertNotEquals("0912345678", otp)
    }

    @Test
    fun testRejection_FalsePositive_Currency() {
        val message = "So du cua ban tang 50.000 VND vao ngay 10/11."
        val otp = OtpDetectorEngine.extractOtp("com.google.android.apps.messaging", "Notice", message)
        assertNull(otp)
    }

    @Test
    fun testRejection_RepetitiveDigits() {
        val message = "Ma OTP cua ban la 0000"
        val otp = OtpDetectorEngine.extractOtp("com.google.android.apps.messaging", "SMS", message)
        assertNull("Repetitive digits like 0000 should be rejected", otp)
    }

    @Test
    fun testVolatileHistory_NoPermanentStorage() {
        OtpDetectorEngine.recordOtpEvent("com.messaging", "123456", "SMS")
        assertEquals(1, OtpDetectorEngine.volatileOtpEvents.value.size)
        assertEquals("123456", OtpDetectorEngine.volatileOtpEvents.value[0].code)

        OtpDetectorEngine.clearVolatileHistory()
        assertEquals(0, OtpDetectorEngine.volatileOtpEvents.value.size)
    }
}

