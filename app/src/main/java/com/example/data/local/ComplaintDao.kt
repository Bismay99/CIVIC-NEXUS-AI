package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {

    @Query("SELECT * FROM complaints WHERE userId = :userId ORDER BY createdAtTimestamp DESC")
    fun getComplaintsForUser(userId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE userId = :userId ORDER BY createdAtTimestamp DESC")
    suspend fun getComplaintsForUserOnce(userId: String): List<ComplaintEntity>

    @Query("SELECT * FROM complaints ORDER BY createdAtTimestamp DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE referenceId = :referenceId LIMIT 1")
    suspend fun getComplaintById(referenceId: String): ComplaintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)

    @Query("DELETE FROM complaints WHERE referenceId = :referenceId")
    suspend fun deleteComplaint(referenceId: String)

    @Query("DELETE FROM complaints WHERE userId = :userId")
    suspend fun deleteComplaintsForUser(userId: String)

    @Query("DELETE FROM complaints")
    suspend fun clearAll()
}
