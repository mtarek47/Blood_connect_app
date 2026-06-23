package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.User

@Database(
    entities = [User::class, BloodRequest::class, Donation::class],
    version = 2,
    exportSchema = false
)
abstract class BloodDatabase : RoomDatabase() {
    abstract val bloodDao: BloodDao

    companion object {
        @Volatile
        private var INSTANCE: BloodDatabase? = null

        fun getDatabase(context: Context): BloodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BloodDatabase::class.java,
                    "blood_donation_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
