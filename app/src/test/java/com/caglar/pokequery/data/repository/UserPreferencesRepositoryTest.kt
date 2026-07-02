package com.caglar.pokequery.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {
    @Test
    fun `event guide settings persist locally`() = runBlocking {
        val file = File.createTempFile("pokequery-test-prefs", ".preferences_pb").also { it.delete() }
        val store = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { file }
        )
        val repository = UserPreferencesRepository(store)

        assertTrue(repository.userPreferencesFlow.first().eventGuideUpdatesEnabled)
        repository.setBooleanSetting(UserPreferencesRepository.EVENT_GUIDE_UPDATES_ENABLED, false)

        assertFalse(repository.userPreferencesFlow.first().eventGuideUpdatesEnabled)
    }
}
