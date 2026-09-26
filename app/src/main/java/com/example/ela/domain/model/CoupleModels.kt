package com.example.ela.domain.model

import java.util.Date

data class Couple(
    val coupleId: String = "",
    val partner1Id: String = "",
    val partner2Id: String = "",
    val createdAt: Date = Date(),
    val sharingSettings: SharingSettings = SharingSettings()
)

data class SharingSettings(
    val shareCycleData: Boolean = false,
    val shareNotes: Boolean = false,
    val shareHealthMetrics: Boolean = false
)

data class Invite(
    val inviteId: String = "",
    val senderId: String = "",
    val receiverEmail: String = "",
    val status: InviteStatus = InviteStatus.PENDING,
    val createdAt: Date = Date()
)

enum class InviteStatus {
    PENDING, ACCEPTED, DECLINED
}
