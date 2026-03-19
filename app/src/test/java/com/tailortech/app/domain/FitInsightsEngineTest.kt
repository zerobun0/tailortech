package com.tailortech.app.domain

import com.tailortech.app.data.UserMeasurements
import org.junit.Assert.assertTrue
import org.junit.Test

class FitInsightsEngineTest {
    @Test
    fun returnsExpectedInsightsForBaselineProfile() {
        val profile = UserMeasurements(
            chestUpperCm = 93.5,
            waistNaturalCm = 83.5,
            riseCrotchCm = 64.0,
            thighWidestCm = 51.2
        )

        val insights = FitInsightsEngine.buildInsights(profile)

        assertTrue(insights.any { it.title.contains("Drop") && it.detail.contains("Regular") })
        assertTrue(insights.any { it.title == "Rise Alert" })
        assertTrue(insights.any { it.title == "Thigh Alert" })
    }
}
