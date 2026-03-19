package com.tailortech.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMeasurementsDao {
    @Query("SELECT * FROM user_measurements WHERE id = 1")
    fun observeUserMeasurements(): Flow<UserMeasurements?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(measurements: UserMeasurements)
}
