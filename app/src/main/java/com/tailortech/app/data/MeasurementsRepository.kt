package com.tailortech.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

class MeasurementsRepository(
    private val dao: UserMeasurementsDao
) {
    fun observeMeasurements(): Flow<UserMeasurements> {
        return dao.observeUserMeasurements()
            .onEach { value ->
                if (value == null) {
                    dao.upsert(UserMeasurements())
                }
            }
            .filterNotNull()
    }

    suspend fun save(measurements: UserMeasurements) {
        dao.upsert(measurements)
    }
}
