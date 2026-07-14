package com.caglar.pokequery.domain.engine

import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.engine.StringBuilderEngine.DEFAULT_PROTECTIONS
import com.caglar.pokequery.domain.lint.ExpertCopyPolicy
import com.caglar.pokequery.domain.lint.Linter
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cross-platform parity test — reads the same golden-corpus.json consumed by
 * web/src/__tests__/golden-corpus.test.ts (Vitest) and asserts that the
 * Android Kotlin engine produces byte-identical output on every test case.
 *
 * The JSON lives at app/src/test/resources/golden-corpus.json and must be
 * kept byte-identical with web/src/parity/golden-corpus.json.  Byte-identity
 * is enforced by `npm run check:golden-corpus` in the web directory, which
 * runs in CI before any tests.  Do not regenerate one without the other;
 * use `npm run sync:golden-corpus` to copy web → Android.
 *
 * If any case fails, the assertion message includes the test case id so the
 * failing scenario can be located in either the web or Android test runner.
 */
class GoldenCorpusParityTest {

    @Test
    fun `every golden corpus case produces parity output`() {
        val corpus = loadCorpus()
        val testCases = corpus.getJSONArray("testCases")
        var failures = mutableListOf<String>()

        for (i in 0 until testCases.length()) {
            val tc = testCases.getJSONObject(i)
            val id = tc.getString("id")
            val category = tc.getString("category")
            val input = tc.getJSONObject("input")
            val expected = tc.optJSONObject("expected") ?: JSONObject()

            try {
                when (category) {
                    "buildGoal" -> checkBuildGoal(input, expected)
                    "buildString" -> checkBuildString(input, expected)
                    "lint" -> checkLint(input, expected)
                    "expertCopyPolicy" -> checkExpertCopyPolicy(input, expected)
                    "goalStringBuilder" -> checkGoalStringBuilder(input, expected)
                    else -> throw AssertionError("Unknown category '$category' for case '$id'")
                }
            } catch (e: AssertionError) {
                failures += "[$id] ${e.message}"
            }
        }

        assertTrue(
            "Parity failures (${failures.size}):\n${failures.joinToString("\n")}",
            failures.isEmpty()
        )
    }

    private fun loadCorpus(): JSONObject {
        val resource = javaClass.classLoader!!.getResourceAsStream("golden-corpus.json")
            ?: throw AssertionError("golden-corpus.json not found on test classpath")
        val text = resource.bufferedReader().use { it.readText() }
        return JSONObject(text)
    }

    private fun checkBuildGoal(input: JSONObject, expected: JSONObject) {
        val goalId = input.getString("goalId")
        val config = input.optString("config", "")
        val customQuery = input.optString("customQuery", "")
        val language = input.optString("language", "English")

        val result = StringBuilderEngine.buildGoal(goalId, config, customQuery, language)
        checkExpected(result, expected)
    }

    private fun checkBuildString(input: JSONObject, expected: JSONObject) {
        val baseQuery = input.getString("baseQuery")
        val protections = when (val p = input.opt("protections")) {
            "EMPTY" -> emptyList<String>()
            "DEFAULT", null -> DEFAULT_PROTECTIONS
            is JSONArray -> p.toStringList()
            else -> DEFAULT_PROTECTIONS
        }
        val explanation = input.optString("explanation", "")
        val riskLevel = input.optString("riskLevel", "Low").let { name ->
            RiskLevel.valueOf(name)
        }
        val goalId = input.optString("goalId", "custom")
        val title = input.optString("title", "Custom Search")
        val language = input.optString("language", "English")

        val result = StringBuilderEngine.buildString(
            baseQuery = baseQuery,
            protections = protections,
            explanation = explanation,
            riskLevel = riskLevel,
            goalId = goalId,
            title = title,
            language = language
        )
        checkExpected(result, expected)
    }

    private fun checkLint(input: JSONObject, expected: JSONObject) {
        val query = input.getString("query")
        val warnings = Linter.lint(query)

        if (expected.has("hasError")) {
            assertEquals(
                "hasError mismatch",
                expected.getBoolean("hasError"),
                warnings.any { it.isError }
            )
        }
        if (expected.has("warningsContain")) {
            val expectedContains = expected.getJSONArray("warningsContain").toStringList()
            for (sub in expectedContains) {
                assertTrue(
                    "Expected a warning containing '$sub'",
                    warnings.any { it.message.contains(sub) }
                )
            }
        }
        if (expected.has("warningsNotContain")) {
            val expectedNotContains = expected.getJSONArray("warningsNotContain").toStringList()
            for (sub in expectedNotContains) {
                assertFalse(
                    "Did not expect a warning containing '$sub'",
                    warnings.any { it.message.contains(sub) }
                )
            }
        }
    }

    private fun checkExpertCopyPolicy(input: JSONObject, expected: JSONObject) {
        val rawQuery = input.getString("rawQuery")
        val result = ExpertCopyPolicy.canCopy(rawQuery)
        assertEquals(expected.getBoolean("canCopy"), result)
    }

    private fun checkGoalStringBuilder(input: JSONObject, expected: JSONObject) {
        val goalId = input.getString("goalId")
        val config = input.optString("config", "")
        val language = input.optString("language", "English")
        val optionalProtections = input.optJSONArray("optionalProtections")?.toStringList() ?: emptyList()

        val base = StringBuilderEngine.buildGoal(goalId, config, "", language)
        val result = GoalStringBuilder.buildFinal(base, optionalProtections, language)
        checkExpected(result, expected)
    }

    private fun checkExpected(result: GeneratedString, expected: JSONObject) {
        if (expected.has("rawSyntax")) {
            assertEquals(
                "rawSyntax mismatch",
                expected.getString("rawSyntax"),
                result.rawSyntax
            )
        }
        if (expected.has("rawSyntaxContains")) {
            val contains = expected.getJSONArray("rawSyntaxContains").toStringList()
            for (sub in contains) {
                assertTrue(
                    "rawSyntax should contain '$sub'",
                    result.rawSyntax.contains(sub)
                )
            }
        }
        if (expected.has("rawSyntaxNotContains")) {
            val notContains = expected.getJSONArray("rawSyntaxNotContains").toStringList()
            for (sub in notContains) {
                assertFalse(
                    "rawSyntax should NOT contain '$sub'",
                    result.rawSyntax.contains(sub)
                )
            }
        }
        if (expected.has("riskLevel")) {
            val expectedRisk = RiskLevel.valueOf(expected.getString("riskLevel"))
            assertEquals("riskLevel mismatch", expectedRisk, result.riskLevel)
        }
        if (expected.has("scopeBreadth")) {
            assertEquals(
                "scopeBreadth mismatch",
                expected.getString("scopeBreadth"),
                result.scopeBreadth
            )
        }
        if (expected.has("goalId")) {
            assertEquals(
                "goalId mismatch",
                expected.getString("goalId"),
                result.goalId
            )
        }
        if (expected.has("title")) {
            assertEquals(
                "title mismatch",
                expected.getString("title"),
                result.title
            )
        }
        if (expected.has("warningsCount")) {
            assertEquals(
                "warningsCount mismatch",
                expected.getInt("warningsCount"),
                result.warnings.size
            )
        }
        if (expected.has("warningsContains")) {
            val expectedContains = expected.getJSONArray("warningsContains").toStringList()
            for (sub in expectedContains) {
                assertTrue(
                    "Expected a warning containing '$sub'",
                    result.warnings.any { it.contains(sub) }
                )
            }
        }
        if (expected.has("protectedCategories")) {
            val expectedCats = expected.getJSONArray("protectedCategories").toStringList()
            assertEquals(
                "protectedCategories mismatch",
                expectedCats,
                result.protectedCategories
            )
        }
        if (expected.has("includedHighRiskCategories")) {
            val expectedHighRisk = expected.getJSONArray("includedHighRiskCategories").toStringList()
            assertEquals(
                "includedHighRiskCategories mismatch",
                expectedHighRisk,
                result.includedHighRiskCategories
            )
        }
    }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { optString(it) }.filter { it.isNotEmpty() }
}
