package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey
    val referenceId: String,
    val userId: String,
    val category: String,
    val title: String,
    val description: String,
    val hasPhoto: Boolean,
    val photoName: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val submittedDate: String,
    val status: String,
    val timelineJson: String,
    val assignedOfficer: String,
    val aiAnalysisJson: String,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
