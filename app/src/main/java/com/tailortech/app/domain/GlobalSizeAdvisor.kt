package com.tailortech.app.domain

import com.tailortech.app.data.UserMeasurements
import kotlin.math.roundToInt

data class RegionalSize(
    val tops: String,
    val bottoms: String,
    val shoes: String,
    val collar: String,
    val suit: String
)

data class GlobalSizeProfile(
    val ukUs: RegionalSize,
    val eu: RegionalSize,
    val chinaAsia: RegionalSize,
    val countryMappings: List<CountrySizeMapping>,
    val brandNotes: List<String>
)

data class CountrySizeMapping(
    val code: String,
    val label: String,
    val size: RegionalSize
)

fun GlobalSizeProfile.sizeForCountry(code: String): CountrySizeMapping {
    return countryMappings.firstOrNull { it.code == code } ?: countryMappings.first()
}

object GlobalSizeAdvisor {
    fun advise(measurements: UserMeasurements): GlobalSizeProfile {
        val topsUk = if (measurements.chestUpperCm < 92.0) "S" else "M"
        val waistIn = measurements.waistPantsLevelCm / 2.54
        val inseamIn = measurements.inseamCm / 2.54
        val w = waistIn.roundToInt()
        val l = inseamIn.roundToInt()
        val bottomsUk = "W${w} L${l}"

        val shoesUk = if (measurements.footLengthCm <= 26.0) "7" else "7.5"
        val collarIn = (measurements.neckCm / 2.54).roundToInt().toString() + "\""

        val ukUs = RegionalSize(
            tops = topsUk,
            bottoms = bottomsUk,
            shoes = shoesUk,
            collar = collarIn,
            suit = "36R"
        )

        val us = RegionalSize(
            tops = topsUk,
            bottoms = bottomsUk,
            shoes = "7.5-8",
            collar = collarIn,
            suit = "36R"
        )

        val eu = RegionalSize(
            tops = "48",
            bottoms = "48",
            shoes = "41",
            collar = "38",
            suit = "46"
        )

        val chinaAsia = RegionalSize(
            tops = "170/96A",
            bottoms = "170/82A",
            shoes = "260mm",
            collar = "38",
            suit = "170/92A"
        )

        val countryMappings = listOf(
            CountrySizeMapping("GB", "United Kingdom", ukUs),
            CountrySizeMapping("US", "United States", us),
            CountrySizeMapping("DE", "Germany (EU)", eu),
            CountrySizeMapping("FR", "France (EU)", eu),
            CountrySizeMapping("IT", "Italy (EU)", RegionalSize("48", "46-48", "41", "38", "46")),
            CountrySizeMapping("CN", "China", chinaAsia),
            CountrySizeMapping("JP", "Japan", RegionalSize("M", bottomsUk, "26", "38", "46")),
            CountrySizeMapping("KR", "Korea", RegionalSize("95", "32", "260", "38", "46")),
            CountrySizeMapping("RU", "Russia", RegionalSize("48", "48", "41", "38", "46"))
        )

        return GlobalSizeProfile(
            ukUs = ukUs,
            eu = eu,
            chinaAsia = chinaAsia,
            countryMappings = countryMappings,
            brandNotes = listOf(
                "SHEIN: Size up to L for oversized silhouettes.",
                "Nike: Runs small for some silhouettes, check chart or half-size up in slim cuts.",
                "New Balance: Standard D width should fit your foot profile."
            )
        )
    }
}
