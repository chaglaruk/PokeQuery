package com.caglar.pokequery.data.repository

import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.SavedTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SavedTemplateCodecTest {
    @Test
    fun `saved template round trips delimiter characters safely`() {
        val template = SavedTemplate("id", "Name | test", "count2-&!costume", "candy_prep", RiskLevel.Medium, 123L)
        assertEquals(template, SavedTemplateCodec.decode(SavedTemplateCodec.encode(template)))
    }

    @Test
    fun `broken saved template is ignored`() {
        assertNull(SavedTemplateCodec.decode("not-a-record"))
    }
}
