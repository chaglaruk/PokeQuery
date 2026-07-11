// Port of com.caglar.pokequery.domain.lint.ExpertCopyPolicy

import { lint } from './linter'

export function canCopy(rawQuery: string): boolean {
  return !lint(rawQuery).some(w => w.isError)
}
