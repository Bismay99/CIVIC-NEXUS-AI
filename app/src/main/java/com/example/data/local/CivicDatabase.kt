package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ComplaintEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CivicDatabase : RoomDatabase() {

    abstract fun complaintDao(): ComplaintDao

    companion object {
        @Volatile
        private var INSTANCE: CivicDatabase? = null

        fun getInstance(context: Context): CivicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CivicDatabase::class.java,
                    "civic_nexus_complaints.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
