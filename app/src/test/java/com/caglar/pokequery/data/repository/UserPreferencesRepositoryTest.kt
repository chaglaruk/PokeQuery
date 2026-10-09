package com.caglar.pokequery.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {
    @Test
    fun textScaleChoicesAreStableAndSafe() {
        assertEquals(4, AppTextScale.OPTIONS.distinct().size)
        assertEquals(0.90f, AppTextScale.multiplier(AppTextScale.SMALL), 0.0001f)
        assertEquals(1.00f, AppTextScale.multiplier(AppTextScale.DEFAULT), 0.0001f)
        assertEquals(1.15f, AppTextScale.multiplier(AppTextScale.LARGE), 0.0001f)
        assertEquals(1.30f, AppTextScale.multiplier(AppTextScale.EXTRA_LARGE), 0.0001f)
        assertEquals(1.00f, AppTextScale.multiplier("unknown"), 0.0001f)
    }
}
