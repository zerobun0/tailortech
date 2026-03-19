package com.tailortech.app.data

import androidx.room.TypeConverter

class UnitSystemConverter {
    @TypeConverter
    fun fromUnitSystem(value: UnitSystem): String = value.name

    @TypeConverter
    fun toUnitSystem(value: String): UnitSystem = UnitSystem.valueOf(value)
}
