package com.caglar.pokequery.domain.assist

import java.util.Calendar
import java.util.TimeZone

data class LocalDate(
    val year: Int,
    val monthValue: Int,
    val dayOfMonth: Int
) : Comparable<LocalDate> {

    override fun compareTo(other: LocalDate): Int {
        val y = year.compareTo(other.year)
        if (y != 0) return y
        val m = monthValue.compareTo(other.monthValue)
        if (m != 0) return m
        return dayOfMonth.compareTo(other.dayOfMonth)
    }

    fun isAfter(other: LocalDate): Boolean = this > other

    companion object {
        fun of(year: Int, month: Int, dayOfMonth: Int): LocalDate =
            LocalDate(year, month, dayOfMonth)

        fun now(): LocalDate {
            val cal = Calendar.getInstance()
            return LocalDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun daysBetween(from: LocalDate, to: LocalDate): Int {
            val cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(from.year, from.monthValue - 1, from.dayOfMonth)
            }
            val cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(to.year, to.monthValue - 1, to.dayOfMonth)
            }
            val diffMs = cal2.timeInMillis - cal1.timeInMillis
            return (diffMs / 86400000L).toInt()
        }

        fun daysInMonth(year: Int, month: Int): Int {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(year, month - 1, 1)
            }
            return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
    }
}

data class CaughtDateMatch(
    val tokens: List<String>,
    val explanation: String,
    val limitations: List<String>,
    val canBuild: Boolean
)

object CaughtDateIntentParser {

    private val caughtContextRegex = Regex(
        """\b(?:caught|acquired|obtained|yakala\w*)\b""",
        RegexOption.IGNORE_CASE
    )

    private val yearRegex = Regex(
        """(?<!\d)(20\d{2})(?:['’]?(?:te|de|ten|den|deki|teki|ye|e|yılında|yilinda))?(?!\d)""",
        RegexOption.IGNORE_CASE
    )

    private data class MonthDef(
        val month: Int,
        val enName: String,
        val trName: String,
        val regex: Regex
    )

    private val months = listOf(
        MonthDef(1, "January", "Ocak", Regex("""\b(?:january|jan|ocak(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(2, "February", "Şubat", Regex("""\b(?:february|feb|(?:şubat|subat)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(3, "March", "Mart", Regex("""\b(?:march|mar|mart(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(4, "April", "Nisan", Regex("""\b(?:april|apr|nisan(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(5, "May", "Mayıs", Regex("""\b(?:may|(?:mayıs|mayis)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(6, "June", "Haziran", Regex("""\b(?:june|jun|haziran(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(7, "July", "Temmuz", Regex("""\b(?:july|jul|temmuz(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(8, "August", "Ağustos", Regex("""\b(?:august|aug|(?:ağustos|agustos)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(9, "September", "Eylül", Regex("""\b(?:september|sept|sep|(?:eylül|eylul)(?:['’]?(?:de|den|deki|te|ten|teki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(10, "October", "Ekim", Regex("""\b(?:october|oct|ekim(?:['’]?(?:de|den|deki|te|ten|teki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(11, "November", "Kasım", Regex("""\b(?:november|nov|(?:kasım|kasim)(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE)),
        MonthDef(12, "December", "Aralık", Regex("""\b(?:december|dec|(?:aralık|aralik)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)\b""", RegexOption.IGNORE_CASE))
    )

    private val turkishDetectionRegex = Regex("""[şŞğĞüÜöÖçÇ]|\b(?:yakala\w*|nisan\w*|ocak\w*|şubat\w*|subat\w*|mart\w*|mayıs\w*|mayis\w*|haziran\w*|temmuz\w*|ağustos\w*|agustos\w*|eylül\w*|eylul\w*|ekim\w*|kasım\w*|kasim\w*|aralık\w*|aralik\w*|bul)\b""", RegexOption.IGNORE_CASE)

    fun parse(text: String, today: LocalDate = LocalDate.now()): CaughtDateMatch? {
        if (!caughtContextRegex.containsMatchIn(text)) {
            return null
        }

        val isTurkish = turkishDetectionRegex.containsMatchIn(text)

        val yearMatch = yearRegex.find(text)
        val year = yearMatch?.groups?.get(1)?.value?.toIntOrNull()

        val matchedMonth = months.firstOrNull { it.regex.containsMatchIn(text) }

        // Bare caught request (no month and no year)
        if (year == null && matchedMonth == null) {
            val expl = if (isTurkish) {
                "Bir ay veya yıl belirtin; örneğin: 'Nisan 2025'te yakalanan Pokémonları bul' veya '2025'te yakalanan Pokémonları bul'."
            } else {
                "Specify a month, year, or date range, for example: 'caught in April 2025' or 'caught in 2025'."
            }
            return CaughtDateMatch(
                tokens = emptyList(),
                explanation = expl,
                limitations = emptyList(),
                canBuild = false
            )
        }

        // Future year validation
        if (year != null && year > today.year) {
            val expl = if (isTurkish) {
                "Gelecekteki tarihler aranamaz. Lütfen geçmiş veya geçerli bir yıl belirtin (${today.year} ve öncesi)."
            } else {
                "Cannot search for future dates. Please specify a past or current year (up to ${today.year})."
            }
            return CaughtDateMatch(
                tokens = emptyList(),
                explanation = expl,
                limitations = emptyList(),
                canBuild = false
            )
        }

        val rollingAgeLimitation = if (isTurkish) {
            "Pokémon GO yaş filtreleri 24 saatlik pencereler kullanır; bu nedenle ayın ilk veya son gününe denk gelenler için kısa bir manuel kontrol gerekebilir."
        } else {
            "Pokémon GO age filters use rolling 24-hour windows, so matches near the first or last day of the month may require a quick manual check."
        }

        // Month + Year OR Month only
        if (matchedMonth != null) {
            val effectiveYear = year ?: if (matchedMonth.month <= today.monthValue) today.year else today.year - 1
            val monthName = if (isTurkish) matchedMonth.trName else matchedMonth.enName

            val start = LocalDate.of(effectiveYear, matchedMonth.month, 1)
            if (start.isAfter(today)) {
                val expl = if (isTurkish) {
                    "Gelecekteki tarihler aranamaz. Lütfen geçmiş veya geçerli bir ay belirtin."
                } else {
                    "Cannot search for future dates. Please specify a past or current month."
                }
                return CaughtDateMatch(
                    tokens = emptyList(),
                    explanation = expl,
                    limitations = emptyList(),
                    canBuild = false
                )
            }

            val maxEndDay = LocalDate.daysInMonth(effectiveYear, matchedMonth.month)
            val endDay = if (effectiveYear == today.year && matchedMonth.month == today.monthValue) today.dayOfMonth else maxEndDay
            val end = LocalDate.of(effectiveYear, matchedMonth.month, endDay)

            val younger = LocalDate.daysBetween(end, today)
            val older = LocalDate.daysBetween(start, today)

            val ageToken = if (younger == older) "age$younger" else "age$younger-$older"
            val tokens = listOf("year$effectiveYear", ageToken)

            val expl = if (year != null) {
                if (isTurkish) {
                    "$monthName $effectiveYear tarihinde yakalanan Pokémonları bulur (year$effectiveYear ve $ageToken kullanarak)."
                } else {
                    "Finds Pokémon caught in $monthName $effectiveYear (using year$effectiveYear and $ageToken)."
                }
            } else {
                if (isTurkish) {
                    "$monthName $effectiveYear tarihinde yakalanan Pokémonları bulur (en son $monthName, year$effectiveYear ve $ageToken kullanarak)."
                } else {
                    "Finds Pokémon caught in $monthName $effectiveYear (most recent $monthName, using year$effectiveYear and $ageToken)."
                }
            }

            return CaughtDateMatch(
                tokens = tokens,
                explanation = expl,
                limitations = listOf(rollingAgeLimitation),
                canBuild = true
            )
        }

        // Year only
        val expl = if (isTurkish) {
            "$year yılında yakalanan Pokémonları bulur (year$year kullanarak)."
        } else {
            "Finds Pokémon caught in $year (using year$year)."
        }

        return CaughtDateMatch(
            tokens = listOf("year$year"),
            explanation = expl,
            limitations = emptyList(),
            canBuild = true
        )
    }
}