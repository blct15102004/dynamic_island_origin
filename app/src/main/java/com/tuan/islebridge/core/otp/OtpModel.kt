package com.tuan.islebridge.core.otp

data class OtpEvent(
    val id: String,
    val sourcePackage: String,
    val code: String,
    val senderOrTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeoutMs: Long = 30000L
)

