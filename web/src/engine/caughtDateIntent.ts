// CaughtDateIntentParser — TypeScript port of Android CaughtDateIntentParser.kt
// Parses natural-language caught/acquired date queries into official Pokémon GO syntax (yearYYYY, ageN-M).

export interface CaughtDateMatch {
  tokens: string[]
  explanationKey: string
  explanation: string
  limitationKeys: string[]
  limitations: string[]
  canBuild: boolean
}

interface MonthDef {
  month: number
  enName: string
  trName: string
  regex: RegExp
}

const caughtContextRegex = /\b(?:caught|acquired|obtained|yakala\w*)\b/i

const yearRegex = /(?<!\d)(20\d{2})(?:['’]?(?:te|de|ten|den|deki|teki|ye|e|yılında|yilinda))?(?!\d)/i

const months: MonthDef[] = [
  { month: 1, enName: 'January', trName: 'Ocak', regex: /(?:^|\s)(?:january|jan|ocak(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 2, enName: 'February', trName: 'Şubat', regex: /(?:^|\s)(?:february|feb|(?:şubat|subat)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 3, enName: 'March', trName: 'Mart', regex: /(?:^|\s)(?:march|mar|mart(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 4, enName: 'April', trName: 'Nisan', regex: /(?:^|\s)(?:april|apr|nisan(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 5, enName: 'May', trName: 'Mayıs', regex: /(?:^|\s)(?:may|(?:mayıs|mayis)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 6, enName: 'June', trName: 'Haziran', regex: /(?:^|\s)(?:june|jun|haziran(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 7, enName: 'July', trName: 'Temmuz', regex: /(?:^|\s)(?:july|jul|temmuz(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 8, enName: 'August', trName: 'Ağustos', regex: /(?:^|\s)(?:august|aug|(?:ağustos|agustos)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 9, enName: 'September', trName: 'Eylül', regex: /(?:^|\s)(?:september|sept|sep|(?:eylül|eylul)(?:['’]?(?:de|den|deki|te|ten|teki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 10, enName: 'October', trName: 'Ekim', regex: /(?:^|\s)(?:october|oct|ekim(?:['’]?(?:de|den|deki|te|ten|teki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 11, enName: 'November', trName: 'Kasım', regex: /(?:^|\s)(?:november|nov|(?:kasım|kasim)(?:['’]?(?:da|dan|daki|ta|tan|taki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
  { month: 12, enName: 'December', trName: 'Aralık', regex: /(?:^|\s)(?:december|dec|(?:aralık|aralik)(?:['’]?(?:ta|tan|taki|da|dan|daki))?(?:\s+ay(?:ı|i)nda)?)(?=\s|$)/i },
]

const turkishDetectionRegex = /[şŞğĞüÜöÖçÇ]|\b(?:yakala\w*|nisan\w*|ocak\w*|şubat\w*|subat\w*|mart\w*|mayıs\w*|mayis\w*|haziran\w*|temmuz\w*|ağustos\w*|agustos\w*|eylül\w*|eylul\w*|ekim\w*|kasım\w*|kasim\w*|aralık\w*|aralik\w*|bul)\b/i

// DST-safe days between two UTC dates
function daysBetween(from: { year: number; month: number; day: number }, to: { year: number; month: number; day: number }): number {
  const utc1 = Date.UTC(from.year, from.month - 1, from.day)
  const utc2 = Date.UTC(to.year, to.month - 1, to.day)
  return Math.round((utc2 - utc1) / 86400000)
}

function getDaysInMonth(year: number, month: number): number {
  return new Date(Date.UTC(year, month, 0)).getUTCDate()
}

export function parseCaughtDateIntent(text: string, today: Date = new Date()): CaughtDateMatch | null {
  if (!caughtContextRegex.test(text)) {
    return null
  }

  const todayYear = today.getFullYear()
  const todayMonth = today.getMonth() + 1
  const todayDay = today.getDate()
  const todayDateObj = { year: todayYear, month: todayMonth, day: todayDay }

  const isTurkish = turkishDetectionRegex.test(text)

  const yearMatch = text.match(yearRegex)
  const year = yearMatch && yearMatch[1] ? parseInt(yearMatch[1], 10) : null

  const matchedMonth = months.find(m => m.regex.test(text)) ?? null

  // Bare caught request (no month and no year)
  if (year === null && matchedMonth === null) {
    const expl = isTurkish
      ? "Bir ay veya yıl belirtin; örneğin: 'Nisan 2025'te yakalanan Pokémonları bul' veya '2025'te yakalanan Pokémonları bul'."
      : "Specify a month, year, or date range, for example: 'caught in April 2025' or 'caught in 2025'."
    return {
      tokens: [],
      explanationKey: 'search_intent_expl_caught_bare',
      explanation: expl,
      limitationKeys: [],
      limitations: [],
      canBuild: false,
    }
  }

  // Future year validation
  if (year !== null && year > todayYear) {
    const expl = isTurkish
      ? `Gelecekteki tarihler aranamaz. Lütfen geçmiş veya geçerli bir yıl belirtin (${todayYear} ve öncesi).`
      : `Cannot search for future dates. Please specify a past or current year (up to ${todayYear}).`
    return {
      tokens: [],
      explanationKey: 'search_intent_expl_caught_future',
      explanation: expl,
      limitationKeys: [],
      limitations: [],
      canBuild: false,
    }
  }

  const rollingAgeLimitation = isTurkish
    ? 'Pokémon GO yaş filtreleri 24 saatlik pencereler kullanır; bu nedenle ayın ilk veya son gününe denk gelenler için kısa bir manuel kontrol gerekebilir.'
    : 'Pokémon GO age filters use rolling 24-hour windows, so matches near the first or last day of the month may require a quick manual check.'

  // Month + Year OR Month only
  if (matchedMonth !== null) {
    const effectiveYear = year !== null ? year : (matchedMonth.month <= todayMonth ? todayYear : todayYear - 1)
    const monthName = isTurkish ? matchedMonth.trName : matchedMonth.enName

    if (effectiveYear > todayYear || (effectiveYear === todayYear && matchedMonth.month > todayMonth)) {
      const expl = isTurkish
        ? 'Gelecekteki tarihler aranamaz. Lütfen geçmiş veya geçerli bir ay belirtin.'
        : 'Cannot search for future dates. Please specify a past or current month.'
      return {
        tokens: [],
        explanationKey: 'search_intent_expl_caught_future',
        explanation: expl,
        limitationKeys: [],
        limitations: [],
        canBuild: false,
      }
    }

    const startObj = { year: effectiveYear, month: matchedMonth.month, day: 1 }
    const daysInMonth = getDaysInMonth(effectiveYear, matchedMonth.month)
    const endDay = (effectiveYear === todayYear && matchedMonth.month === todayMonth) ? todayDay : daysInMonth
    const endObj = { year: effectiveYear, month: matchedMonth.month, day: endDay }

    const younger = daysBetween(endObj, todayDateObj)
    const older = daysBetween(startObj, todayDateObj)

    const ageToken = younger === older ? `age${younger}` : `age${younger}-${older}`
    const tokens = [`year${effectiveYear}`, ageToken]

    const expl = year !== null
      ? (isTurkish
          ? `${monthName} ${effectiveYear} tarihinde yakalanan Pokémonları bulur (year${effectiveYear} ve ${ageToken} kullanarak).`
          : `Finds Pokémon caught in ${monthName} ${effectiveYear} (using year${effectiveYear} and ${ageToken}).`)
      : (isTurkish
          ? `${monthName} ${effectiveYear} tarihinde yakalanan Pokémonları bulur (en son ${monthName}, year${effectiveYear} ve ${ageToken} kullanarak).`
          : `Finds Pokémon caught in ${monthName} ${effectiveYear} (most recent ${monthName}, using year${effectiveYear} and ${ageToken}).`)

    return {
      tokens,
      explanationKey: 'search_intent_expl_caught_month',
      explanation: expl,
      limitationKeys: ['search_intent_lim_age_rolling_window'],
      limitations: [rollingAgeLimitation],
      canBuild: true,
    }
  }

  // Year only
  const expl = isTurkish
    ? `${year} yılında yakalanan Pokémonları bulur (year${year} kullanarak).`
    : `Finds Pokémon caught in ${year} (using year${year}).`

  return {
    tokens: [`year${year}`],
    explanationKey: 'search_intent_expl_caught_year',
    explanation: expl,
    limitationKeys: [],
    limitations: [],
    canBuild: true,
  }
}