# PokeQuery Spec Summary

**Goal**: Convert simple goals into safe Pokemon GO search strings.

**Constraints**:
- Offline first, no login.
- Generate text only.
- Never use `|` for OR (use `,`).
- Handle `count` limitations explicitly (warn about forms/shinies).
- Default protect: shiny, legendary, mythical, ultrabeast, costume, background, shadow, purified, favorite, lucky, traded, 4*, 0*.

**MVP Goals**:
1. Safe Cleanup
2. 2x Candy Prep
3. Trade Fodder
4. 4★ / Hundo Check
5. Tagged / Untagged Cleanup

**Data**: Local `knowledgebase.json` based on T1/T2 researched syntax.
