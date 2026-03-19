package com.tailortech.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserMeasurements::class], version = 1, exportSchema = false)
@TypeConverters(UnitSystemConverter::class)
abstract class TailorTechDatabase : RoomDatabase() {
    abstract fun userMeasurementsDao(): UserMeasurementsDao

    companion object {
        @Volatile
        private var INSTANCE: TailorTechDatabase? = null

        fun get(context: Context): TailorTechDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TailorTechDatabase::class.java,
                    "tailortech.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
