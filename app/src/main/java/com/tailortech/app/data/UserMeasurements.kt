package com.tailortech.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UnitSystem {
    CM,
    INCH
}

@Entity(tableName = "user_measurements")
data class UserMeasurements(
    @PrimaryKey val id: Int = 1,
    val unitSystem: UnitSystem = UnitSystem.CM,
    val heightCm: Double = 170.0,

    // Head and neck
    val neckCm: Double = 37.7,
    val headFrontToBackCm: Double = 55.6,

    // Torso
    val chestUpperCm: Double = 93.5,
    val chestLowerCm: Double = 82.6,
    val waistNaturalCm: Double = 83.5,
    val waistPantsLevelCm: Double = 82.7,
    val hipCm: Double = 89.5,

    // Shoulders and back
    val shoulderToShoulderCm: Double = 44.0,
    val neckToShoulderCm: Double = 17.5,
    val neckToWaistCm: Double = 44.0,
    val armscyeCm: Double = 43.0,
    val riseCrotchCm: Double = 64.0,

    // Arms
    val bicepFlexedCm: Double = 32.0,
    val bicepRelaxedCm: Double = 28.6,
    val shoulderToElbowCm: Double = 33.9,
    val elbowToWristCm: Double = 30.0,
    val wristCm: Double = 16.5,
    val wristToMiddleFingerCm: Double = 21.2,

    // Legs
    val thighWidestCm: Double = 51.2,
    val kneeCm: Double = 37.2,
    val calfWidestCm: Double = 33.6,
    val ankleCm: Double = 23.6,
    val innerThighToKneeCm: Double = 35.4,
    val kneeToAnkleCm: Double = 41.6,
    val outerThighCm: Double = 54.8,

    // Feet
    val footLengthCm: Double = 26.0,
    val footWidthCm: Double = 9.8
) {
    val inseamCm: Double
        get() = innerThighToKneeCm + kneeToAnkleCm

    val totalArmLengthCm: Double
        get() = shoulderToElbowCm + elbowToWristCm

    val dropCm: Double
        get() = chestUpperCm - waistNaturalCm
}

fun Double.toInches(): Double = this / 2.54

fun Double.round1(): Double = kotlin.math.round(this * 10.0) / 10.0
