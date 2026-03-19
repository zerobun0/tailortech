package com.tailortech.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UserMeasurementsTest {
    @Test
    fun calculatesInseamAndTotalArmLength() {
        val m = UserMeasurements(
            innerThighToKneeCm = 35.4,
            kneeToAnkleCm = 41.6,
            shoulderToElbowCm = 33.9,
            elbowToWristCm = 30.0
        )

        assertEquals(77.0, m.inseamCm, 0.0001)
        assertEquals(63.9, m.totalArmLengthCm, 0.0001)
    }

    @Test
    fun calculatesDrop() {
        val m = UserMeasurements(
            chestUpperCm = 93.5,
            waistNaturalCm = 83.5
        )

        assertEquals(10.0, m.dropCm, 0.0001)
    }
}
