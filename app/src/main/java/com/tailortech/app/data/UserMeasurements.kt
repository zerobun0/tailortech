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
    val heightCm: Double = 0.0,

    // Head and neck
    val neckCm: Double = 0.0,
    val headFrontToBackCm: Double = 0.0,

    // Torso
    val chestUpperCm: Double = 0.0,
    val chestLowerCm: Double = 0.0,
    val waistNaturalCm: Double = 0.0,
    val waistPantsLevelCm: Double = 0.0,
    val hipCm: Double = 0.0,

    // Shoulders and back
    val shoulderToShoulderCm: Double = 0.0,
    val neckToShoulderCm: Double = 0.0,
    val neckToWaistCm: Double = 0.0,
    val armscyeCm: Double = 0.0,
    val riseCrotchCm: Double = 0.0,

    // Arms
    val bicepFlexedCm: Double = 0.0,
    val bicepRelaxedCm: Double = 0.0,
    val shoulderToElbowCm: Double = 0.0,
    val elbowToWristCm: Double = 0.0,
    val wristCm: Double = 0.0,
    val wristToMiddleFingerCm: Double = 0.0,

    // Legs
    val thighWidestCm: Double = 0.0,
    val kneeCm: Double = 0.0,
    val calfWidestCm: Double = 0.0,
    val ankleCm: Double = 0.0,
    val innerThighToKneeCm: Double = 0.0,
    val kneeToAnkleCm: Double = 0.0,
    val outerThighCm: Double = 0.0,

    // Feet
    val footLengthCm: Double = 0.0,
    val footWidthCm: Double = 0.0
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
