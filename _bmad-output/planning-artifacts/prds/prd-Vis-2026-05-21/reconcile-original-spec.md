---
title: Vis PRD — Reconciliation against Original Spec
status: draft
created: 2026-05-22
owner: Gauravprakashshinde
purpose: Identify content from the 2026-05-04 product spec that the Discovery-derived PRD silently dropped — especially qualitative / aesthetic / voice material that does not survive FR-shaped structure.
---

# Reconcile: PRD vs Original Spec (`2026-05-04-vis-design.md`)

## 1. Brief — what the spec is

The original spec (`docs/superpowers/specs/2026-05-04-vis-design.md`, 557 lines, dated 2026-05-04, status "Draft — awaiting final review") is the **pre-Discovery product requirements document** for Vis. It assumes:

- a three-app fitness platform for a single multi-branch gym chain ("vis", 7 branches → 100),
- gym-only path (no solo lifters),
- staff-assigned trainers (not client-chosen),
- single-tenant configuration (`OWNER_NAME = vis`),
- React Native + Spring Boot + Postgres + Firebase Auth + Cloud Run (largely retained),
- and a deeply elaborated **brand / UI design system** (sections 17.1 – 17.7) covering logo concepts, color tokens, typography, Liquid Glass material, animation language, theme strategy, prototype structure, and an orb-as-organism visual metaphor for tension and progression.

Discovery (captured in `.decision-log.md`, decisions 1-45, and the resulting PRD dated 2026-05-21) explicitly superseded the spec on several axes — solo + gym, client-chosen trainers, multi-tenant SaaS, progressive-overload engine, equipment-aware logger, PR cards, six role-contexts. The spec's **business + structural** content is therefore largely obsolete or transformed. Its **qualitative + aesthetic** content was not superseded by Discovery; whether the PRD carries it forward is the central question of this reconciliation.

## 2. Honored — areas the PRD correctly captures or correctly supersedes

These are intentional and complete carry-forwards (or Discovery-driven replacements that fully cover the spec intent):

| Spec section | PRD coverage | Status |
|---|---|---|
| §1 Overview — three-app platform, branch-scoping | PRD §2 personas, §4 Block D/E, Glossary `Brand`/`Branch` | Honored (extended to multi-tenant per Discovery) |
| §2 Personas (Client/Trainer/Staff/Owner) | PRD §2.1 six role-contexts | Honored + extended (solo added) |
| §3 Authentication (Google / Apple / Phone OTP, Firebase, JWT) | FR-38, FR-39 | Honored verbatim |
| §4 Tech stack | PRD §7 NFRs + addendum + inputs frontmatter | Honored |
| §4.1 Phase 0 Milestone | PRD §6 MVP scope + NFR §7.9 | Honored |
| §5 Multi-branch data model (tenant → brand → branch) | Glossary `Tenant`/`Brand`/`Branch`; FR-26 audit log scoping; FR-20-23 Owner surface | Honored + multi-tenant per Discovery |
| §6 CSV / Excel data import | FR-15 | Honored |
| §7.1 Access gate | FR-39 | Honored |
| §7.2 Onboarding flow | FR-27 Questionnaire, FR-25 trainer pick | Honored + extended (10-field intake) |
| §7.3 Home tab — recovery chips, streak, today's stats | FR-35 Muscle Recovery, FR-36 Progress | Honored (streak/visit-time integrated in FR-37) |
| §7.4 Workout tab + Active Session | FR-1 Co-edit, FR-2-7 Set semantics, FR-8 Equipment-Aware Logger | Honored + dramatically extended via Discovery |
| §7.5 Progress tab | FR-36 | Honored |
| §7.6 Nutrition tab | **Dropped intentionally** — Discovery removed nutrition from v1 (not in PRD §6 in-scope, not in §5 non-goals either — see Gap G7 below) |
| §7.7 Profile tab | FR-37 | Honored |
| §8 Trainer App (Home/Clients/Plan/Profile) | FR-1, FR-3, FR-12-14, FR-21, FR-24, FR-29-31 | Honored + extended |
| §8.3 Plan templates + bespoke + real-time sync | FR-29, FR-31, FR-1 | Honored verbatim, WS reconciliation in NFR §7.2 |
| §9 Active session shared behavior | FR-1, FR-3 | Honored |
| §10 Admin Web (Overview/Members/Trainers/Branches/Reminders) | FR-15-23 | Honored |
| §11 Notifications matrix | NFR §7.6, FR-18, FR-30 | Honored (channel mix changed: SMS deferred per Discovery) |
| §12 AI features (exercise suggestions + macro generation) | FR-13 (Trainer-only AI suggestion); macro generation **dropped** with nutrition (Gap G7) |
| §13 Apple Health / Google Fit | Non-Goals §5 (read-only display deferred); addendum §5 wearable deferred | Superseded per Discovery |
| §14 Muscle groups reference | FR-35 chip list | Honored |
| §15 Trainer leaderboard | FR-21, FR-24 | Honored |
| §16 Out of Scope v1 | PRD §5 Non-Goals + §6.2 | Honored + extended |
| §17.6 Theme strategy (light + dark, warm-only) | Implicit via `prototype/` reference; design tokens in CLAUDE.md | Honored at codebase level, **not in PRD** (Gap G2) |
| §17.7 Prototype structure + mock data profiles | `prototype/` referenced in PRD inputs | Honored at prototype level |
| §18 Open questions | PRD §9 (re-derived from Discovery) | Superseded — older questions retired by Discovery decisions |

## 3. Gaps — content silently dropped (ranked by load-bearing)

These are passages from the spec that the PRD does not carry forward, that Discovery did **not** explicitly supersede, and that materially affect downstream work (UX spec, design system, copy, motion). Ranked by load-bearing.

### G1 (HIGH) — Orb-as-organism metaphor + tension-state visual language

**Spec §7.4 (lines 141-152):** The Workout tab is described as "the brand's signature canvas for the orb-as-organism metaphor." Each exercise carries a **tension-state sphere** with four explicit visual + animation states:

| Set state | Orb appearance | Animation |
|---|---|---|
| At rest | smooth glossy sphere, neutral peach | gentle 6s breathing |
| Under load | sphere squeezed taller, deeper saturation | pulse synced to expected rep tempo |
| Peak tension | sphere with visible vertical stress lines, deepest rust shadow | tight, fast micro-shake |
| Recovered | sphere settles to soft glow, faint orange halo | one-shot 800ms ease-out settle |

Plus: **stacked orbs** for weekly volume strip ("Motion & progression" concept), **connection orb** with tension stem for trainer↔client live session ("Connection & collaboration" concept).

**Why load-bearing:** This is the single most distinctive visual concept in the spec — the orb is not decoration, it is the *meter* (effort, fatigue, progression). The PRD's §1 Vision says "beauty serves usability" and explicitly demotes "the orb aesthetic" as not-the-differentiator. That phrasing is correct strategically, but it has caused the PRD to drop the operational visual semantics entirely. FR-1 through FR-8 describe set rows with no mention of the orb-state visual feedback that gives the brand its identity. A UX designer reading only the PRD would build generic numeric set rows.

**PRD location it should land:** an FR under Block A (Trainer↔Client In-Person Sessions) describing the Set-state orb as a required visual affordance with the four states named above, or — if relegated to UX spec — an explicit `[NOTE FOR PM]` flagging that the orb tension-state visual is in scope and not gold-plating.

### G2 (HIGH) — Brand design system: Liquid Glass material + animation language + theme strategy

**Spec §17.1-17.6 (lines 388-528):** Roughly 140 lines of canonical brand language:

- **§17.1 Brand identity:** 5 logo concepts (tension point, hidden V, motion/progression, connection, custom typography). "The sphere is the *only* repeating visual icon in the system. Avoid introducing other illustrative motifs."
- **§17.2 Color system:** Warm-orange palette tokens (`--vis-peach`, `--vis-coral`, `--vis-flame`, `--vis-ember`, `--vis-rust`, `--vis-burnt`, `--vis-charcoal`) with hexes; orb radial gradient recipe; light + dark linear-grad recipes; rule of restraint ("rarely more than one orange gradient surface at full saturation"; "pure white and black are forbidden — always use warm-tinted neutrals").
- **§17.3 Typography:** Geist + Geist Mono, 7-step type scale (Display XL down to Eyebrow + Numeric), spacing scale `4·8·12·16·20·24·32·40·56·72`.
- **§17.4 Liquid Glass design language:** Six core principles (content leads, contextual blur, refractive edges, specular highlights, motion responsiveness, accessibility fallback); explicit *where to use* (top nav, hero card, modals, filter chips, live indicators) and *where NOT to use* (tables, dense lists, body paragraphs, charts, forms); sample CSS.
- **§17.5 Animation language:** Default easing `cubic-bezier(0.32, 0.72, 0, 1)`; duration scale `120/200/360/560ms`; breathing loops `5s alternate opacity 1→0.94 scale 1→1.005`; shimmer 6s diagonal sweep on orb; page transitions 240ms fade + 4px translateY-in; number tickers 360ms ease-out count-up; touch feedback 80ms.
- **§17.6 Theme strategy:** Light bg `#FAF6F2`, dark bg `#0F0A07`; "both themes share the *same* orange family — only luminance and gradient direction shift. Never introduce cool-tone accents."

**Why load-bearing:** This is the single most actionable design-system content in the project. None of it appears in the PRD. CLAUDE.md carries a partial palette (different hexes — `#FF7A1A` family, not `#F25A1F` family), but CLAUDE.md is an agent-instructions file, not a PRD input. A UX designer working off this PRD has no formal handle on Liquid Glass, the motion vocabulary, the rule of restraint, or the typographic scale.

**PRD location it should land:** PRD §7 cross-cutting NFRs has a §7.7 Accessibility — add a §7.10 "Brand & Visual System" pointing to a UX-spec section that carries §17.1-17.6 forward verbatim. Or addendum §7 "Brand language (for UX spec)".

> Note: The CLAUDE.md palette and the spec §17.2 palette use different hex values. This is a real conflict — spec says `#F25A1F` ember, CLAUDE.md says `#E06313`. Resolving this is a Finalize-time decision.

### G3 (MEDIUM-HIGH) — Brand voice / tone cues

**Spec §17.5 (line 509):** "Keep motion purposeful and warm — never showy."
**Spec §17.2 (line 408):** "Restraint is the rule."
**Spec §17.1 (line 403):** "The sphere is the *only* repeating visual icon … Avoid introducing other illustrative motifs."
**Spec §17 framing:** "cinematic in feel" (echoed in PRD §1, line 36, *only* mention).

The PRD's §1 Vision uses "cinematic in feel, evidence-based in method." That single phrase is the only voice cue in 609 lines. The spec's repeated **restraint / never showy / one icon / warm not cool** discipline is what gives Vis a distinct tone — and it is not anywhere in the PRD.

**Why load-bearing:** Copy, microcopy, marketing, animation budget, illustration commissions, and onboarding screens all need a voice anchor. Without it, contractors and downstream agents drift toward generic fitness-app exuberance (confetti, badges, gamification). The spec explicitly forbids that drift.

**PRD location it should land:** A short "Voice & Tone" paragraph under PRD §1 or §7, with the four canonical rules: cinematic not gaming, restraint, warm not cool, one icon only.

### G4 (MEDIUM) — Logo concepts as semantic anchors for product moments

**Spec §17.1 (lines 396-401):** The five logo concepts are not just identity ideas — three of them are explicitly tied to product surfaces in the spec:

- Concept #3 "Motion & progression" (stacked orbs) → **weekly schedule strip** rendering
- Concept #4 "Connection & collaboration" (two orbs + tension stem) → **Active Session header** when trainer joins live
- Concept #1 "Tension point" → set-state orb visualization (G1)

The PRD's FR-1 (Co-edited Session) does not call for any visual representation of the trainer-client connection. The Active Session header in the PRD is an undescribed surface.

**Why load-bearing:** These concepts give moments-of-truth a visual signature. PR card composer (FR-11) calls for a "cinematic moment screen, amber animation" — but with no anchor to the orb language, this is freelance work. Linking the moments to the logo concepts gives the visual designer a constraint that produces brand-coherent output.

**PRD location it should land:** Either consequences-under FR-1, FR-11, FR-36 (weekly volume view), or in the addendum as "UI moment anchors."

### G5 (MEDIUM) — Light + dark theme parity as a v1 requirement

**Spec §17.6 (lines 522-526):** Explicit two-theme table with separate bg, gradient, card surface, text per theme. The spec treats both as v1 deliverables.

The PRD does not mention theme at all. The prototype already implements both — but a reader of the PRD alone would not know whether dark theme is in v1 scope.

**Why load-bearing:** Implementation cost. Accessibility (some users *need* dark). Marketing (Indian Android user base often prefers dark for OLED battery + glare). The spec made an explicit choice; the PRD did not carry it forward.

**PRD location it should land:** PRD §7.7 (Accessibility) — add "Light + dark themes both shipped v1, sharing the warm-orange family per §17.6 of the source spec."

### G6 (MEDIUM) — Restraint constraint on semantic colors

**Spec §17.2 (line 408):** "Semantic colors (good/warn/bad) are kept muted so the brand orange always leads."

Without this rule, downstream contributors will introduce saturated greens/reds/yellows for status surfaces (Recovery chips per FR-35 are a prime target — "green = recovered; red = hours remaining"). The PRD's FR-35 takes the unmuted color language for granted.

**PRD location:** A line in the proposed §7.10 Brand & Visual System, or under FR-35 consequences.

### G7 (MEDIUM) — Nutrition module silently dropped, not explicitly excluded

**Spec §7.6 + §8.3 nutrition + §12.2 AI macro generation:** ~25 lines of nutrition product. The Trainer App had a full nutrition plan builder with AI macro generation; the Client App had a read-only nutrition tab.

The PRD does **not** list nutrition under §5 Non-Goals or §6.2 Out-of-Scope. It just is not in the FR set. This is a silent drop, not an explicit Discovery exclusion (I scanned the decision-log evidence in the PRD inputs section — Discovery focused on progressive-overload and did not address nutrition either way).

**Why load-bearing:** A stakeholder reading the spec then the PRD would assume nutrition is still in. Either it should be moved to §5 Non-Goals ("Not a nutrition / macro app v1 — defer to v2") or restored as an FR block. The current limbo state is a finalize-blocking ambiguity.

**Recommended:** Add to §5 Non-Goals as "Not a nutrition / macro tracking app v1. Trainer-set nutrition plans + AI macro generation are v2 candidates." Get user confirmation.

### G8 (LOW-MEDIUM) — Mock-data personas + naming continuity

**Spec §17.7 mock data:** Clients Arjun Mehta, Priya Shah, Rahul Desai; trainers Vikram Nair, Sneha Kulkarni. Membership ₹2,500/mo, PT ₹8,000/mo.

The PRD's named personas are Aarav, Rohit (solo), Priya, Karan (solo), Anita, Vikram. **Vikram is now owner, not trainer; Priya is now trainer (was client in spec).** The role swap is intentional per Discovery, but it means the prototype's mock data needs to be re-aligned with the PRD personas, and pricing references (₹2,500 / ₹8,000) are now stale because v1 is free.

**Why load-bearing:** Prototype + future story-fixtures should match the canonical persona names. Otherwise demos drift between spec-era and PRD-era names.

**PRD location:** Addendum §X "Persona naming canonical list" or a note in §2.1 Personas.

### G9 (LOW) — Onboarding step ordering nuance

**Spec §7.2 step 4:** Trainer browse happens *after* Body Profile setup. PRD UJ-3 has the questionnaire → trainer browse → trainer accept. Same shape; this is honored. No real gap, listed for completeness.

### G10 (LOW) — Spec's Section 18 open questions retired without trace

**Spec §18 (lines 553-557):**
- "Which specific AI provider (OpenAI, Gemini, Claude)?" — PRD resolved to Claude (FR-13, §7.3).
- "Client feedback / rating submission flow — when and how?" — PRD does not address.
- "Exact session scheduling model — trainer-set or client-derived?" — PRD does not address.

Items 2 + 3 are dropped, not resolved. Low load-bearing because they're already-stale operational questions, but worth a one-line "deferred per Discovery" acknowledgment in PRD §9 Open Questions for traceability.

## 4. Anti-gaps — places PRD intentionally diverges from spec (no action)

These are places where the PRD departs from the spec and Discovery's audit trail supports the change. Listed so the reader does not mistake them for gaps.

| Spec position | PRD position | Why correct |
|---|---|---|
| Gym-only platform | Solo + gym, six role-contexts | Discovery decision 1-3: solo path added |
| Single-tenant `OWNER_NAME=vis` | Multi-tenant SaaS, `Tenant`/`Brand` glossary | Discovery decision 4-5: multi-tenant from day 1 |
| Staff-assigned trainers | Client-chosen trainers with priority list (FR-25) | Discovery decision 8: client picks 1-3 in order |
| Trainer-set plans + nutrition only | Plans + progression engine + PR cards + equipment-aware logger | Discovery decisions 10-25: progressive overload as core differentiator |
| AI for solo macro generation | AI Trainer-only v1, cost ceiling $0.40/WAU (SM-C1) | Discovery decision 27-28: cost containment |
| Apple Health / Google Fit read-only display on Home | Wearables deferred per addendum §5 | Discovery decision 30: addendum scope move |
| Three rating dimensions (Experience / Feedback / Progress) shown to Client | Retained in FR-21, FR-24 | Spec honored |
| Notifications include SMS via Twilio | SMS deferred to v2 (PRD §5, NFR §7.6) | Discovery decision 35: cost + DLT regulatory load |
| Branch-imported via CSV/Excel as the one path | CSV + manual + QR self-onboard | Discovery decision 12, 26 |
| Plans = bespoke clone-from-template only | Plans = flat OR phased; three template sources | Discovery decision 18-19: phased plans + brand-shared library |
| Sessions logged real-time during PT | Sessions logged real-time + solo + ad-hoc; co-edit explicit FR-1 with WS | Discovery decision 14-16: structural moat formalization |
| Recovery chips per muscle group | Retained verbatim (FR-35) | Spec honored |
| Workout / Nutrition / Progress / Profile as v1 tabs | Workout / Progress / Profile retained; Nutrition silently dropped (see Gap G7) | Discovery did not address — see G7, treat as Finalize question |

## 5. Recommended Finalize-time actions

Ranked by load-bearing:

1. **Resolve G7 (nutrition):** ask user — is nutrition out for v1? If yes, move to §5 Non-Goals explicitly. If no, restore as FR block. (~5 minutes of clarification, blocks downstream UX spec scope.)
2. **Add brand & visual system anchor (G2 + G3):** add PRD §7.10 "Brand & Visual System" (one short section) with cross-reference to spec §17.1-17.6 and the four voice/tone rules; OR move the full §17 content into `addendum.md §7` "Brand language (for UX spec)" verbatim. Resolve hex conflict with CLAUDE.md.
3. **Add orb tension-state to FR set (G1):** new FR under Block A (e.g. FR-1a or FR-8a) — "Set-state orb visualization" with the four states from spec §7.4 named. Or addendum entry "UI moment anchors" covering G1 + G4.
4. **Confirm light + dark theme parity v1 (G5):** one line in §7 NFRs.
5. **Confirm restraint rule for semantic colors (G6):** one line under FR-35 or §7.10.
6. **Persona naming continuity (G8):** addendum note + prototype data refresh task.
7. **Acknowledge retired spec questions (G10):** one-line addition to PRD §9.

## 6. Diff summary by line-count

| Spec sections | Lines in spec | Substantively carried to PRD | Carried to addendum | Lost / silently dropped |
|---|---:|---:|---:|---:|
| §1-§16 (product) | ~385 | ~340 | ~10 | ~35 (mostly nutrition + minor) |
| §17 (brand + design system) | ~140 | ~5 (one phrase) | 0 | ~135 |
| §18 (open questions) | ~5 | ~2 | 0 | ~3 |
| **Total** | **~530** | **~347 (65%)** | **~10 (2%)** | **~173 (33%)** |

The 33% loss is concentrated in **§17 (brand + design system)** and concentrated in **qualitative content** (visual metaphor, voice, motion). This is the kind of loss the PRD finalize step is supposed to catch.

---

*This reconciliation is a finalize-phase input. Resolutions land in the PRD itself, the addendum, or the UX spec at the discretion of the PM.*
