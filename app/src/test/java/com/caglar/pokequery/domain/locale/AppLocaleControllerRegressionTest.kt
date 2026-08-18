package com.caglar.pokequery.domain.locale

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLocaleControllerRegressionTest {

    @Test
    fun `system default uses current device locale and clears remembered override`() {
        val before = Locale.getDefault()
        try {
            AppLocaleController.applyProcessLocale("tr", Locale.ENGLISH)
            assertEquals("tr", Locale.getDefault().language)

            AppLocaleController.applyProcessLocale(null, Locale.GERMAN)
            assertEquals("de", Locale.getDefault().language)

            AppLocaleController.applyProcessLocale("fr", Locale.GERMAN)
            assertEquals("fr", Locale.getDefault().language)

            AppLocaleController.applyProcessLocale(null, Locale.ITALIAN)
            assertEquals("it", Locale.getDefault().language)
        } finally {
            AppLocaleController.applyProcessLocale(null, before)
            Locale.setDefault(before)
        }
    }
}
