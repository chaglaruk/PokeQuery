/** Reference templates explain syntax; they are not ready-to-paste tokens. */
export function canCopyKnowledgeToken(syntax: string): boolean {
  return syntax.trim().length > 0 && !/[<>|[\]]/.test(syntax)
}
