package com.example.smarthydro.aidailyanomaly

// represent a device (tent) we want to check daily
data class Device(val label: String, val mac: String)

object AiDailyConfig {
    // ✅ REPLACE with your public HTTPS API (no LAN IPs)
    const val SERVER_BASE_URL = "https://api.yourdomain.com"

    // ✅ REPLACE with your Koog details
    const val KOOG_BASE_URL   = "https://api.koog.ai"
    const val KOOG_API_KEY    = "REPLACE_ME"

    // (Legacy single-device fallback; still used if you don’t pass input to the Worker)
    const val DEVICE_MAC      = "AB:CD:EF:12:34:56"

    // list both tents here (REPLACE MACs and labels)
    val DEVICES = listOf(
        Device(label = "Vegetable Tent", mac = "AA:BB:CC:DD:EE:01"),
        Device(label = "Fodder Tent",    mac = "AA:BB:CC:DD:EE:02"),
    )
}

fun hasRealServer(): Boolean =
    AiDailyConfig.SERVER_BASE_URL.startsWith("https://") &&
            !AiDailyConfig.SERVER_BASE_URL.contains("yourdomain", ignoreCase = true)

fun devicesConfigured(): Boolean =
    AiDailyConfig.DEVICES.isNotEmpty()
