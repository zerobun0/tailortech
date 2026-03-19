package com.tailortech.app.domain

import com.tailortech.app.data.UserMeasurements

data class FitInsight(
    val title: String,
    val detail: String
)

object FitInsightsEngine {
    fun buildInsights(measurements: UserMeasurements): List<FitInsight> {
        val insights = mutableListOf<FitInsight>()

        val drop = measurements.dropCm
        if (drop in 8.0..12.0) {
            insights += FitInsight(
                title = "Drop: ${"%.1f".format(drop)} cm",
                detail = "Regular or A-Fit proportions are likely your best baseline."
            )
        }

        if (measurements.riseCrotchCm >= 62.0) {
            insights += FitInsight(
                title = "Rise Alert",
                detail = "Avoid low-rise cuts; Mid-to-High rise will fit your proportions better."
            )
        }

        if (measurements.thighWidestCm >= 50.0) {
            insights += FitInsight(
                title = "Thigh Alert",
                detail = "Skinny fits may be tight; Slim-Straight or Tapered recommended."
            )
        }

        return insights
    }
}
