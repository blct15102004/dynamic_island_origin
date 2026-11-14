package com.tuan.islebridge.core.originisland

import com.tuan.islebridge.core.common.Constants

enum class SuperIslandOperation(val code: Int) {
    MOUNT(Constants.SuperXProtocol.OP_MOUNT),
    UNMOUNT(Constants.SuperXProtocol.OP_UNMOUNT),
    UPDATE(Constants.SuperXProtocol.OP_UPDATE)
}

enum class IslandScene(val sceneName: String) {
    BLUETOOTH("bluetooth"),
    OTP("otp"),
    MEDIA("media"),
    PROGRESS("progress"),
    PRIVACY("privacy"),
    APP_MIRROR("app_mirror"),
    SYSTEM("system")
}

data class CapsulePayload(
    val iconResId: Int? = null,
    val iconUri: String? = null,
    val text: String,
    val subText: String? = null,
    val accentColor: Int? = null
)

data class BaseInfoItem(
    val key: String,
    val value: String
)

data class SuperIslandPayload(
    val eventId: String,
    val operation: SuperIslandOperation = SuperIslandOperation.MOUNT,
    val templateId: Int = Constants.SuperXProtocol.TEMPLATE_BASE_INFO,
    val scene: IslandScene = IslandScene.SYSTEM,
    val title: String,
    val content: String,
    val capsule: CapsulePayload,
    val showNotify: Boolean = true,
    val isIsland: Boolean = true,
    val progress: Int? = null,
    val maxProgress: Int? = null,
    val baseInfos: List<BaseInfoItem> = emptyList(),
    val infos: List<String> = emptyList(),
    val shortInfos: String = content,
    val timeoutMs: Long = Constants.DEFAULT_TIMEOUT_NOTIF_MS
)

