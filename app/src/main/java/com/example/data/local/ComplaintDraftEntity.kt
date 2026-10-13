package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaint_drafts")
data class ComplaintDraftEntity(
    @PrimaryKey
    val id: String = "ACTIVE_CITIZEN_DRAFT",
    val title: String = "",
    val description: String = "",
    val category: String = "Potholes & Roads",
    val address: String = "",
    val latitude: Double = 20.3533,
    val longitude: Double = 85.8172,
    val hasPhoto: Boolean = false,
    val photoUri: String = "",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
