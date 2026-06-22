# AI Feasibility Assessment

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Status as of v0.5.2:** Research / documentation only. No AI is shipped, active, or
networked. The offline-first, no-login, no-network, no-tracking guarantees are unchanged.

This document evaluates whether and how PokeQuery could ever add AI assistance **without**
user API keys and **without** compromising the app's privacy model.

---

## 1. The hard constraints (non-negotiable)

PokeQuery's privacy contract is the product. Any AI approach must satisfy ALL of these:

- ❌ No user API keys (no Gemini/​OpenAI/​Anthropic key entry).
- ❌ No network calls in the steady state. The app stays offline-first and zero-permission.
- ❌ No login / account sync.
- ❌ No analytics / telemetry about user queries.
- ❌ No Pokémon GO account access, screen scanning, OCR, or automation.
- ❌ No "safe to delete" / "execute cleanup" / auto-transfer / auto-trade decisions.

If a candidate technology breaks any of these, it is rejected.

---

## 2. Candidate 1 — On-device generative AI (Gemini Nano via AICore / ML Kit GenAI)

This is the most promising path and the only one consistent with the privacy contract.

**What it is:** Android's `AICore` system service hosts Google's **Gemini Nano** model
on-device and exposes it through the **ML Kit GenAI** APIs. Inference happens locally on the
device's NPU/GPU; no prompt leaves the phone.

**Feasibility for PokeQuery:**
- ✅ Privacy-compatible: on-device, no API key, no network for inference.
- ✅ Keeps the zero-INTERNET-permission stance in steady state (AICore is a system service,
  not an app-level network dependency).
- ⚠️ **Severe device-coverage limitation (the blocker).** Gemini Nano / AICore is only
  available on a narrow, high-end device set. As of the latest public information it is
  present on recent Pixel (8 Pro / 9 / 10 series) and Samsung Galaxy S24/S25 series, and is
  NOT available on the Android emulator. The vast majority of PokeQuery's install base
  (arbitrary mid/low-range devices, minSdk 24) would have **no AI at all**.
- ⚠️ **API-level gate.** AICore targets ~API 31+; PokeQuery supports API 24+, so AI would be
  a gated, optional feature — never a core dependency.
- ⚠️ AICore availability can change and is OEM-dependent; it must be detected at runtime,
  never assumed.

**Required runtime checks before ever enabling it:**
1. `minSdk` / `Build.VERSION.SDK_INT >= 31`.
2. Check that the AICore / ML Kit GenAI provider is installed and capable (capability probe).
3. Graceful disable everywhere the capability is absent — the app must be fully functional
   without it (as it is today).

**Verdict:** Technically feasible and privacy-aligned, but **device coverage is too low to be
a primary feature**. It can only ever be an optional enhancement for a small subset of
devices. Not shippable as a general feature in v0.5.x.

---

## 3. Candidate 2 — Server-side "free" AI (cloud LLM with no key)

Examples: free-tier hosted LLM endpoints, "bring your own backend" proxies.

- ❌ Requires the INTERNET permission and steady-state network access — violates the
  zero-permission / offline-first contract.
- ❌ Sends user query content off-device — a tracking/privacy regression.
- ❌ "Free" server tiers are rate-limited, unreliable, and change terms without notice —
  unsuitable for a production feature users rely on.
- ❌ Implies the developer running/​paying for a backend — unsustainable and a liability.

**Verdict:** Rejected. Server-side free AI is **not reliable for production** and breaks the
privacy model. It will not be used.

---

## 4. Candidate 3 — On-device classical ML / rules (non-generative)

Small, bundled, deterministic models (e.g. a compact tokenizer, a scoring heuristic) that
ship inside the APK and run fully offline.

- ✅ Fully offline, no permissions, deterministic and testable.
- ✅ Works on the whole install base, including low-end devices.
- ⚠️ This is not "AI" in the generative sense — it is enhanced rules. But it is the most
  robust way to deliver features like "explain this query" using the existing
  `StringBuilderEngine` / `KnowledgeBaseRepository` data, with zero new risk.

**Verdict:** Safe and already partially aligned with the app's direction (the KB + engine
already provide deterministic explanations). Any "smart" hints should prefer this path.

---

## 5. Conclusion

- The **only** privacy-compatible generative path is on-device (Gemini Nano / AICore), and
  its device coverage is currently too low to be a general feature.
- Server-side free AI is rejected outright.
- Non-generative on-device logic is safe and preferred for any "smart" feature.

Therefore **v0.5.2 ships no active AI**. A clearly-disabled "AI Assistant — Coming later"
entry point documents the intent without implying capability. See
`AI_ASSISTANT_ROADMAP.md` for the conditional future plan.

## Sources

- [Gemini Nano | AI – Android Developers](https://developer.android.com/ai/gemini-nano)
- [ML Kit GenAI APIs Overview](https://developers.google.com/ml-kit/genai)
- [Gemini Nano experimental access (Google blog, Oct 2024)](https://android-developers.googleblog.com/2024/10/gemini-nano-experimental-access-available-on-android.html)
- [Gemini Nano on-device AI discussion (Google AI forum)](https://discuss.ai.google.dev/t/how-to-deploy-aicore-gemini-nano-on-pixel-other-devices/93284)
