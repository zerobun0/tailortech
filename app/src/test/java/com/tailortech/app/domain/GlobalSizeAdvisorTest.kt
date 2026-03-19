package com.tailortech.app.domain

import com.tailortech.app.data.UserMeasurements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalSizeAdvisorTest {
    @Test
    fun returnsExpectedCoreSizesForKnownProfile() {
        val measurements = UserMeasurements()

        val result = GlobalSizeAdvisor.advise(measurements)

        assertEquals("M", result.ukUs.tops)
        assertEquals("W33 L30", result.ukUs.bottoms)
        assertEquals("7", result.ukUs.shoes)
        assertEquals("15\"", result.ukUs.collar)
        assertEquals("36R", result.ukUs.suit)

        assertEquals("48", result.eu.tops)
        assertEquals("41", result.eu.shoes)

        assertEquals("170/96A", result.chinaAsia.tops)
        assertEquals("170/82A", result.chinaAsia.bottoms)
        assertEquals("260mm", result.chinaAsia.shoes)

        assertTrue(result.brandNotes.any { it.contains("SHEIN") })
        assertTrue(result.brandNotes.any { it.contains("Nike") })
        assertTrue(result.brandNotes.any { it.contains("New Balance") })
    }

    @Test
    fun countryLookupReturnsSelectedCountry() {
        val result = GlobalSizeAdvisor.advise(UserMeasurements())

        val selected = result.sizeForCountry("JP")

        assertEquals("Japan", selected.label)
        assertEquals("26", selected.size.shoes)
    }
}
