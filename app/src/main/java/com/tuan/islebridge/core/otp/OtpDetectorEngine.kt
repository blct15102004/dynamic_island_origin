package com.tuan.islebridge.core.otp

import com.tuan.islebridge.core.notifications.AppFilterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.regex.Pattern

object OtpDetectorEngine {

    // Keywords signaling an authentication / OTP message
    private val OTP_KEYWORD_PATTERNS = listOf(
        Pattern.compile("(?i)(?:mã\\s+xác\\s+(?:thực|nhận)|mã\\s+otp|otp|xác\\s+minh|verification\\s+code|security\\s+code|auth\\s+code|one[-\\s]time\\s+password|passcode|code\\s+is|is\\s+your\\s+(?:verification\\s+)?code)")
    )

    // Regex for 4 to 8 digits bounded by word boundaries or punctuation
    private val CODE_PATTERN = Pattern.compile("(?:\\b|(?<=\\D))([0-9]{4,8})(?:\\b|(?=\\D))")

    // Filter out common dates/years (1950 - 2099)
    private val YEAR_PATTERN = Pattern.compile("^(?:19[5-9][0-9]|20[0-9][0-9])$")

    // Filter out phone number shapes (e.g. 09xx, 03xx, 07xx, 08xx, 10-11 digits)
    private val PHONE_NUMBER_PATTERN = Pattern.compile("^(?:0|\\+?84)[1-9][0-9]{8,9}$")

    // Price patterns like 50.000, 100,000, 50k, 100k, 500đ
    private val CURRENCY_PATTERN = Pattern.compile("(?i)[0-9]+[.,][0-9]{3}|[0-9]+\\s*(?:k|đ|vnd|usd|\\$|dong)")

    // Volatile in-memory OTP events list (NEVER saved to disk or DataStore)
    private val _volatileOtpEvents = MutableStateFlow<List<OtpEvent>>(emptyList())
    val volatileOtpEvents: StateFlow<List<OtpEvent>> = _volatileOtpEvents.asStateFlow()

    fun extractOtp(sourcePackage: String, title: String, content: String): String? {
        // Enforce financial security rule: reject blocked/financial applications immediately
        if (AppFilterManager.isFinancialApp(sourcePackage)) {
            return null
        }

        val fullText = "$title $content".trim()
        if (fullText.isBlank()) return null

        // Must match at least one OTP trigger keyword
        val hasOtpKeyword = OTP_KEYWORD_PATTERNS.any { it.matcher(fullText).find() }
        if (!hasOtpKeyword) {
            return null
        }

        val matcher = CODE_PATTERN.matcher(fullText)
        val candidates = mutableListOf<String>()

        while (matcher.find()) {
            val candidate = matcher.group(1) ?: continue

            // False positive checks
            if (isFalsePositive(candidate, fullText)) {
                continue
            }
            candidates.add(candidate)
        }

        return candidates.firstOrNull()
    }

    private fun isFalsePositive(code: String, fullText: String): Boolean {
        // 1. Length must be 4 to 8 digits
        if (code.length < 4 || code.length > 8) return true

        // 2. Reject if matches standard 4-digit years (2020..2030)
        if (code.length == 4 && YEAR_PATTERN.matcher(code).matches()) {
            // Check context: if immediately followed by "năm" or preceded by "year" or "tháng"
            val index = fullText.indexOf(code)
            if (index >= 0) {
                val surrounding = fullText.substring((index - 10).coerceAtLeast(0), (index + code.length + 10).coerceAtMost(fullText.length))
                if (surrounding.contains("năm", ignoreCase = true) || surrounding.contains("year", ignoreCase = true)) {
                    return true
                }
            }
        }

        // 3. Reject phone numbers
        if (PHONE_NUMBER_PATTERN.matcher(code).matches()) {
            return true
        }

        // 4. Reject currencies (e.g. 50000 VND)
        if (CURRENCY_PATTERN.matcher(fullText).find()) {
            val match = CURRENCY_PATTERN.matcher(fullText)
            while (match.find()) {
                if (match.group().contains(code)) return true
            }
        }

        // 5. Reject trivial repetitive digits like 0000, 1111, 9999
        val allSameChar = code.all { it == code[0] }
        if (allSameChar) return true

        return false
    }

    fun recordOtpEvent(sourcePackage: String, code: String, sender: String, timeoutMs: Long = 30000L): OtpEvent {
        val event = OtpEvent(
            id = UUID.randomUUID().toString(),
            sourcePackage = sourcePackage,
            code = code,
            senderOrTitle = sender,
            timestamp = System.currentTimeMillis(),
            timeoutMs = timeoutMs
        )
        // Prepend to volatile list and limit size to 10 in memory
        val updated = (listOf(event) + _volatileOtpEvents.value).take(10)
        _volatileOtpEvents.value = updated
        return event
    }

    fun clearVolatileHistory() {
        _volatileOtpEvents.value = emptyList()
    }
}

