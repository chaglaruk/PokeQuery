package com.caglar.pokequery.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.caglar.pokequery.domain.scope.InventorySizeProfile
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesRepositoryTest {

    @Test
    fun `inventory size profile defaults and persists locally`() = runTest {
        val file = File.createTempFile("pokequery-test-prefs", ".preferences_pb").also { it.delete() }
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
        val repository = UserPreferencesRepository(store)

        assertEquals(InventorySizeProfile.NOT_SET.name, repository.userPreferencesFlow.first().inventorySizeProfile)

        repository.setSetting(UserPreferencesRepository.INVENTORY_SIZE_PROFILE, InventorySizeProfile.VERY_LARGE.name)

        assertEquals(InventorySizeProfile.VERY_LARGE.name, repository.userPreferencesFlow.first().inventorySizeProfile)
        file.delete()
    }
}
