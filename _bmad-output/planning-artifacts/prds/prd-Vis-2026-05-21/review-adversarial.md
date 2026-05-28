---
title: Vis v1 PRD — Adversarial Review
status: draft
created: 2026-05-22
reviewer: adversarial-review-agent
target: /Users/gauravprakashshinde/Documents/Projects/Ai/Vis/_bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/prd.md
companion: /Users/gauravprakashshinde/Documents/Projects/Ai/Vis/_bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/addendum.md
stance: cynical / hand-waving hunt
---

# Vis v1 PRD — Adversarial Review

Severity scale: **critical** (will break v1 if shipped as written) · **high** (will cause material delay or rework) · **medium** (defensibility / clarity gap) · **low** (polish).

---

## CRITICAL findings

### C-1. "Three load-bearing assertions" — none is actually defended

- **Location:** §1 Vision, lines 38-42.
- **Quote:** *"Three load-bearing assertions drive every downstream decision: 1. Primary user pain = remembering + inputting progression. Visual equipment-aware logging is the differentiator… 2. Trainer↔client real-time in-person collab + multi-branch gym ops are the structural moats. No competitor combines all three. 3. Free v1; pricing later."*
- **Critique:** Assertion 1 is sourced to the user's gut (addendum §1: *"User flagged this as the single most important UX problem"*) — a single founder anecdote, not user research, n=many, or competitive teardown showing existing logging is the conversion-killer. Assertion 2's "no competitor combines all three" is uncited — Trainerize, TrueCoach, FitBudd, Bevel all overlap two of the three; the PRD never names which competitor lacks which axis. Assertion 3 is a pricing punt, not a load-bearing product claim, and doesn't belong in the same list as the other two. The phrase "drive every downstream decision" then justifies six Equipment-Aware Loggers, real-time WebSocket, AI proxy, marketplace, multi-branch — i.e. it carries the whole v1 surface area on undefended ground.
- **Suggested fix:** Replace assertion 3 with a real third load-bearing claim or drop to "two load-bearing assertions". Cite at least one piece of evidence per assertion (domain research §, prototype usability test, named competitor gap). If no evidence exists, mark the assertions `[HYPOTHESIS]` and add an explicit early-validation experiment to §8 Success Metrics (e.g. "if SM-2 D7 < 25% by D45, primary-pain hypothesis is falsified").

### C-2. v1 In-Scope list is an enterprise launch wearing a startup t-shirt

- **Location:** §6.1 In Scope, lines 494-518.
- **Quote:** lists 19 in-scope bullets including *"All six role-contexts… ship day 1"*, *"All six Equipment-Aware Loggers"*, *"Real-time WebSocket co-edit"*, *"AI Progression Rule (Claude proxy with prompt caching)"*, *"ACWR trainer alert"*, *"Trainer-marketplace opt-in + moderation queue"*, *"Cross-branch Owner dashboards + trainer leaderboard"*, *"Audit log P0"*, *"Global units day 1 (kg + lb… per-locale dates, time zones)"*.
- **Critique:** This is not a minimum viable product — it's the full enterprise SaaS spec compressed to "v1". Six role-contexts means six distinct app surfaces (Client, Trainer, Staff, Owner × solo/gym) with RBAC, branch scoping, audit log, moderation queue, cross-branch revenue dashboards. The §6.2 "v1.5 cuts" only defer four items (marketplace, brand-shared templates, flat plan, additional progression rules) — leaving the rest of the elephant intact. The PRD nowhere estimates engineering capacity vs scope; a single-owner team (Open Question 4: *"single-owner shop until first hire"*) cannot ship this surface in a credible window. The phrase *"All six… ship day 1"* recurs three times — that is conviction-as-substitute-for-prioritization.
- **Suggested fix:** Run a forced-ranking pass before Finalize. Pick ONE primary path (gym OR solo) to ship D0; the other ships D60. Defer Owner dashboards (FR-20, FR-21, FR-22, FR-23) to v1.1 — no gym chain will sign a contract on D0 anyway. Drop two Equipment-Aware Loggers from D0 (kettlebell + bands are lowest gym-floor frequency); ship four. Mark explicitly in §6.1 which FRs are D0 vs D30 vs D60. Tie scope to a stated engineering capacity number.

### C-3. WALS target of 2,500 by D90 has no derivation

- **Location:** §8 Success Metrics, lines 612-613.
- **Quote:** *"SM-1 (North Star): Weekly Active Logged Sessions (WALS) — Sessions with ≥1 Set logged in a week. Target: 2,500 by D90; 12,000 by D180."*
- **Critique:** No TAM, no comparable benchmark, no funnel math. 2,500 WALS at e.g. 2.5 sessions/active-user/week = 1,000 WAUs by D90 — is that grounded in domain research's India PT-attached gym population, or pulled from air? The Hevy / Strong / Trainerize baselines aren't named. D180 = 12,000 is a 4.8x of D90 with no acquisition-channel rationale (organic? brand QR? gym partnerships?). Counter-metric SM-C1 (AI cost > $0.40/WAU) becomes meaningless without a credible WAU forecast — at 1,000 WAU that's $400/mo AI spend, trivial; at 50,000 WAU it's a real ceiling.
- **Suggested fix:** Derive WALS from a stated funnel: target gym partnerships × avg PT-active members × adoption% × sessions/week. Cite the Hevy / Trainerize / FitBudd benchmarks domain research surfaced. If targets are aspirational with no derivation, label them as such and add a 30/60/90 leading-indicator ladder.

---

## HIGH findings

### H-1. "No competitor combines all three" is undefended at exactly the moment competitor velocity matters

- **Location:** §1, line 41.
- **Quote:** *"Trainer↔client real-time in-person collab + multi-branch gym ops are the structural moats. No competitor combines all three."*
- **Critique:** Domain research (`_bmad-output/.../domain-vis-fitness-pt-research-2026-05-21.html`) is referenced but the competitive teardown isn't pasted into the PRD or even quoted. Trainerize has trainer-client collab and is shipping AI. FitBudd is India-focused with branch scoping. Bevel, FitnessAI, Motra are all shipping rapidly. The PRD targets a D180 launch — six months of competitor releases. The claim has a half-life. The "all three" formula (live co-edit + multi-branch gym ops + Equipment-Aware Logger) is the only differentiator the PRD identifies — and the Equipment-Aware Logger isn't in the "all three" sentence at all, it's in assertion 1. So which is it — three, two, or one moat?
- **Suggested fix:** Inline a one-paragraph competitive matrix in §1 (three rows: live co-edit, multi-branch ops, equipment-aware logger; columns: Vis, Trainerize, TrueCoach, FitBudd, Bevel). Cite domain research §competitive-landscape with a date stamp. State the durability hypothesis: "we believe X stays a moat for N months because…". Track competitor releases as a counter-metric (SM-C11).

### H-2. Personas are six but JTBDs collapse to three

- **Location:** §2.1 (lines 50-60) + §2.2 (lines 62-68).
- **Quote:** §2.2 lists JTBDs for *"Client (any path) … Client (PT-attached) … Trainer … Gym Staff … Gym Owner … All"*.
- **Critique:** Six personas, but JTBDs only differentiate to three distinct surfaces: Client, Trainer, Gym-Ops (Staff + Owner merged into one in practice). Solo vs gym client share the same JTBD ("Tell me what to do next, log without typing"). Solo vs gym trainer share the same JTBD ("one place to author, track, coach"). Anti-personas are listed (§2.3) but only as features-out, not user-segments-out — who is Vis EXPLICITLY not for? CrossFit boxers? Powerlifting federations? Bodybuilding-prep coaches? Group-class instructors are listed but as feature-out not segment-out. The PRD risks "everyone who lifts" positioning, which historically beats no one.
- **Suggested fix:** Collapse personas to three role-archetypes with sub-context flags (`is_pt_attached`, `is_solo_trainer`, `is_brand_owner`). Add an explicit "Vis is NOT for" list: e.g. competitive powerlifters, group-class first gyms, gyms < 50 members, sports-team programming. This sharpens marketing and product cuts.

### H-3. FR-1 (co-edit) at position 1 implies build-order; reality requires FR-3, FR-8 first

- **Location:** §4.1 FR-1 (line 177) vs FR-3 (line 194) vs FR-8 (line 224).
- **Quote:** *"FR-1: Co-edited Session — Trainer and Client can edit the same Session on two devices via WebSocket."*
- **Critique:** Co-edit is meaningless without the Set data model (FR-3: prescribed/actual/rpe_target/rpe_actual/source/ai_locked) and the Equipment-Aware Logger UI (FR-8) — both of which must exist before two devices can have anything to co-edit. Numbering FR-1 as the headline misleads engineering sequencing. The "Real-Time Co-Edit usage ≥ 25% by D90" SM-3 target is also at risk if FR-1 ships before FR-12 (AI ghost overlay) because the trainer-side has no superior input — they're just typing into a shared form, not coaching with a ghost suggestion to accept/override.
- **Suggested fix:** Add an explicit "Build Sequencing" sub-section after §4 listing FRs in implementation order: FR-3 (Set model) → FR-8 (Logger) → FR-12+FR-14 (AI + ghost) → FR-1 (Co-edit). Re-number FRs OR add a `build_phase` field to each FR. Otherwise downstream epic/story workflows will treat FR-1 as P0 day-1.

### H-4. AI cost ceiling "$0.40/WAU/month" is a number from nowhere

- **Location:** §7.3 (line 567) and SM-C1 (line 627).
- **Quote:** *"Cost ceiling: $0.40 / WAU / month (SM-C3). $1 / WAU triggers pivot."*
- **Critique:** No back-of-envelope: how many AI calls per WAU per month, at what token count, at what cache hit rate, at what model price? With Claude 4 input pricing ~$3/Mtok and ~80% cache hit-rate target, the ceiling implies a specific token budget per WAU — but the PRD never states it. At 3 sessions/WAU/week × 6 exercises × N=12 set history × ~200 tokens per exercise lookup, even with caching, this needs math. Without it, "$0.40" looks like a comfortable-sounding number, not an engineered ceiling. Pivot trigger at $1 is 2.5x the ceiling — why that ratio and not 1.5x or 5x?
- **Suggested fix:** Show the calculation: "(prompts/WAU/mo × tokens/prompt × $/token × (1 − cache_hit_rate)) ≤ $0.40". State assumed values. If cache hit-rate target (≥80%) misses by 20pp, what happens to the budget? Make SM-C1 actionable instead of decorative.

### H-5. SM-C5 trainer-override threshold "> 70%" is arbitrary

- **Location:** §8 SM-C5 (line 631) and FR-14 (line 306).
- **Quote:** *"Trainer-override rate > 70% trips SM-C5 (AI failing)."*
- **Critique:** Why 70%? In a "trainer reviews + accepts" UI (FR-14), a thoughtful trainer might override 50% on novice clients (they know better than the model on early phase) and 5% on accommodated long-tenure clients. A blanket 70% threshold conflates two distinct AI quality signals: (a) suggestion is wrong, (b) trainer wants to express expertise even when suggestion is fine. Better counter-metric: "trainer-override followed by *worse* actual outcome", i.e. measuring agreement-quality not agreement-rate. As written, this metric can be gamed — show duller suggestions, override rate drops, "AI is working".
- **Suggested fix:** Either (a) raise to 85% with rationale ("if trainer overrides 4 of 5 suggestions, model isn't earning a turn"), or (b) replace with "override-then-actual-RPE-worse-than-suggested" — meaning the trainer's override produced a worse session than the AI suggestion would have. This is harder to compute but defensible. Track override-rate separately as a leading indicator, not a pivot threshold.

### H-6. Spotter-assisted half-credit is a load-bearing ASSUMPTION

- **Location:** FR-5 (line 210) and Assumptions Index §10 (line 662).
- **Quote:** *"spotter-assisted halves Progression Index credit `[ASSUMPTION: half-credit; confirm at Finalize]`."*
- **Critique:** Progression Index drives FR-12 (AI suggestions), FR-14 (ghost overlay), FR-37 (ACWR alert), SM-1 (WALS validation downstream). The half-credit rule materially shifts every load suggestion. "Confirm at Finalize" punts it, but the formula is already in the In-Scope §6.1 (line 500: *"Progression Index computed as w_e1rm·Δe1RM + w_vol·ΔVol + w_quality·ΔRPE_eff"*). If the half-credit rule changes post-Finalize, every downstream metric and every AI suggestion needs re-baselining.
- **Suggested fix:** Resolve before Finalize — not "at Finalize". Three options: (a) full-credit (rep happened), (b) half-credit (current default), (c) RPE-penalty (RPE_effective += 1.0). Pick one with stated rationale from a strength coach. Same urgency for FR-13 bootstrap heuristic (50% bodyweight) — that's the cold-start prescription every Solo Client sees on day 1.

### H-7. "Marketplace opt-in" is in §6.1 in-scope AND §6.2 v1.5-cut

- **Location:** §6.1 line 507 vs §6.2 line 522.
- **Quote:** §6.1: *"Trainer-marketplace opt-in (+ moderation queue) + invite-link."* §6.2: *"Trainer Marketplace opt-in (defer; ship invite-link only at launch) `[NOTE FOR PM]`"*.
- **Critique:** Direct internal contradiction. §6.1 ships the marketplace; §6.2 defers it. The `[NOTE FOR PM]` is honest but doesn't resolve it — and any reader (UX designer, architect, downstream BMad workflow) sees both. The decision must be made BEFORE UX phase, not deferred. Same issue: §6.1 ships *"brand-shared"* templates; §6.2 cuts brand-shared.
- **Suggested fix:** Pick. Recommended: defer marketplace + brand-shared + flat-plan-toggle to v1.5 (the §6.2 cut). Move them out of §6.1 entirely. The `[NOTE FOR PM]` callouts are stale once the decision is made.

### H-8. PR Card watermark default — Open Question that will block launch

- **Location:** §9 Open Question 7 (line 653) and FR-11 (line 274).
- **Quote:** *"PR Card watermark default: brand visibility vs. user freedom — confirm default-on at Finalize."*
- **Critique:** This is a launch-blocker dressed as a polish question. Default-ON drives SM-5 (PR Card share rate → brand awareness loop). Default-OFF means SM-5 measures organic share intent uncoupled from brand-acquisition. Pick wrong and the entire growth-loop hypothesis is invalidated. "Confirm at Finalize" leaves the design system, the share template, the analytics tagging all undecided.
- **Suggested fix:** Default ON for v1. Rationale: zero-cost virality is the primary growth-loop case for a free product. User-togglable off is already in FR-11 — that's the escape hatch. Resolve now.

### H-9. Voice fallback scope is in FR-8 but flagged ASSUMPTION

- **Location:** FR-8 (line 240) + Open Question 3 (line 649) + Assumptions Index (line 663).
- **Quote:** *"Voice fallback (trainer in-session, ≥ 1 s of speech) parses 'fifty for ten' → 50 / 10 [ASSUMPTION: voice fallback v1 scope to confirm at Finalize]."*
- **Critique:** Voice input on a noisy gym floor is a 6-week R&D project alone (speech-to-text noise robustness, accent handling for India-anchored user base — Hindi/English code-switching, false-trigger guardrails). Flagging it as `[ASSUMPTION]` inside FR-8 means the FR's testability is undefined. If voice ships, the AC list grows materially. If voice cuts, the trainer-in-session UJ-1 path loses an input mode.
- **Suggested fix:** Cut voice from v1. Add to v2 explicitly. Trainer in-session uses the visualizer + numeric keypad fallback — both already in FR-8. The half-second saved by voice is dwarfed by the false-positive risk on a gym floor.

---

## MEDIUM findings

### M-1. UJ-1 entry state says "Both devices on the same WebSocket session for this scheduled time block" — scheduling isn't a v1 FR

- **Location:** UJ-1 entry state (line 86).
- **Critique:** Where does "this scheduled time block" come from? Scheduling is not in §4 FRs (no FR for session scheduling / booking). Either UJ-1 is depending on a missing capability, or "scheduled" is informal and any active Session triggers Co-Edit. Clarify.
- **Suggested fix:** Either add FR-N for "session scheduling" (likely v2 — scheduling is a separate product surface) or rewrite UJ-1 to use "open Session" rather than "scheduled time block".

### M-2. Six Equipment-Aware Loggers vs §4.1 FR-8 says "six equipment types" but only 6 are listed including bodyweight which isn't equipment

- **Location:** FR-8 (lines 224-235).
- **Critique:** Six types listed: cable, barbell, dumbbell, machine pin-stack, bodyweight, kettlebell, bands. That's seven. "Bodyweight" is also not a Logger surface — it's a silhouette with optional weight-vest field, fundamentally a different UI affordance. Counting inconsistency.
- **Suggested fix:** Correct the count or remove bodyweight from the "six equipment-aware loggers" framing. Recommended: ship four (cable, barbell, dumbbell, machine pin-stack) as the headline loggers; bodyweight, kettlebell, bands as secondary numeric-with-affordance variants.

### M-3. ACWR thresholds (1.5 amber, 2.0 red) are cited as "IOC-grounded" without inline citation

- **Location:** FR-37 (line 432).
- **Quote:** *"Source: IOC-grounded; domain research §training-load."*
- **Critique:** ACWR's 1.5 threshold is contested in sports-science literature; some studies argue the Gabbett 1.5 line is overfit. Auto-deload at >2.0 is a meaningful product action — sending the AI suggestion to 70% of prescribed without trainer review. Trainer dissent risk.
- **Suggested fix:** Cite the specific paper or domain research line. Add a trainer-side toggle: "auto-deload on ACWR > 2.0" default on, can be disabled per (trainer × client).

### M-4. Solo client D30 retention pivot at < 10% by D180 — but the threshold matches the target

- **Location:** §8 SM-4 (line 616) + Pivot Triggers (line 643).
- **Quote:** *"SM-4: Solo D30 retention… Target: ≥ 10% by D180 (pivot trigger if below)."*
- **Critique:** The target IS the pivot trigger. That means "we hit our target = we don't pivot, we miss by 1pp = we kill the solo path". The target should be the floor; the pivot should be a worse number. As written, there's no aspirational margin.
- **Suggested fix:** Set target ≥ 20%, pivot trigger < 10%. Or state explicitly: "10% is the survival floor, not the target".

### M-5. "Global from day one" but Cloud Run asia-south1 only

- **Location:** §1 (line 36) vs §7.9 (line 603).
- **Quote:** *"Global from day one."* vs *"Single-region v1 (Cloud Run asia-south1)."*
- **Critique:** "Global" with a single region in Mumbai means EU and Americas users get 200-400 ms RTT before WebSocket round-trips. Co-Edit's 500 ms p95 target (FR-1 consequence) is at risk for non-India users. The vision sentence overpromises.
- **Suggested fix:** Reframe "Global from day one" as "globally available from day one, India-optimized in v1" or "Global units, dates, time zones day one; multi-region deployment v2". Don't conflate locale-readiness with deployment-readiness.

### M-6. Counter-metric SM-C3 numbering collides with NFR-AI cost section

- **Location:** §7.3 (line 567) refers to "SM-C3" for AI cost ceiling; §8 (line 627) defines SM-C1 as AI cost / WAU and SM-C3 as Crash-free rate.
- **Critique:** Internal cross-ref bug: §7.3 says "Cost ceiling: $0.40 / WAU / month (SM-C3)" but SM-C3 in §8 is the crash-free rate metric. AI cost is SM-C1. This will trip up downstream readers building dashboards.
- **Suggested fix:** Correct §7.3 reference to SM-C1.

### M-7. "Light + dark theme parity ships v1" — restated twice in §11, scope-creep against the §6.2 spirit

- **Location:** §11 (lines 680 + 682).
- **Critique:** Repeated assertion that light + dark theme ships v1 is fine, but where it lives matters: it's a UX-spec mandate, not a PRD scope flag. Doubling down on it in PRD adds review surface for the UX phase without adding clarity. The repetition signals anxiety.
- **Suggested fix:** State once, in §6.1, as a one-line in-scope item. Remove duplicate in §11.

---

## LOW findings

### L-1. UJ-3 trainer card includes "~2h response time" but FR-24 doesn't define how response-time is measured

- **Location:** UJ-3 step 2 (line 127) + FR-24 (line 360).
- **Critique:** "Response time" surface implies tracking trainer reply latency to client messages — but Vis explicitly defers messaging to WhatsApp (§5 non-goal). What is being timed? Trainer accept-on-request? FCM-acknowledge time? Define.
- **Suggested fix:** Specify "response time = time to accept/decline a client request, measured server-side from request-created to trainer-decision". Otherwise the metric is unmeasurable.

### L-2. Persona "Anita" and "Vikram" have no associated UJ

- **Location:** §2.1 (lines 56-57) vs §2.4 (UJ-1, UJ-2, UJ-3).
- **Critique:** Three UJs cover Aarav, Rohit, Priya. Anita (Staff) and Vikram (Owner) get no walked-through scene, despite their FRs being significant (FR-15 through FR-23 = 9 of 43 FRs). UX phase has no story to build flows from.
- **Suggested fix:** Add UJ-4 (Anita imports CSV + activates PT) and UJ-5 (Vikram reviews branch leaderboard, exports audit log) at Finalize.

### L-3. "~15 curated public Plan Templates v1" — by whom, signed off by whom?

- **Location:** FR-9 (line 258) + FR-31 (line 400).
- **Critique:** Authorship + sign-off responsibility unclear. Templates are user-facing programming content with strength-science liability. Who authors? Who reviews? Is a coach contractor on the workstream?
- **Suggested fix:** Add Open Question: "Vis starter template authorship + review process — internal coach contractor, named consultant, or trainer-community PR-style?".

### L-4. RTO 4h, RPO 5min in §7.9 — without on-call rotation (Open Question 4), these are aspirational

- **Location:** §7.9 (line 605) + Open Question 4 (line 651).
- **Critique:** RTO 4h presumes someone is on-call. Open Question 4 says "no defined on-call rotation. Single-owner shop until first hire." A single-owner shop has no 4h RTO at 3am. Honest RTO is "next business day" until a team exists.
- **Suggested fix:** Either define on-call (with cost implication) or relax v1 RTO to 24h and call it out. Don't carry an SLO the org can't honor.

### L-5. Glossary "Tenant" overload

- **Location:** §3 (line 141).
- **Quote:** *"Tenant — A gym brand … OR an independent trainer operating in solo path."*
- **Critique:** Single term for two structurally different entities (multi-branch brand with sub-orgs vs single-person solo trainer). Architecture phase will inherit the ambiguity. Two concrete types under one Glossary term invites confusion in JPA entities and RBAC rules.
- **Suggested fix:** Split: `BrandTenant` and `SoloTenant` with a discriminator field. Or rename Glossary to `Tenant (Brand | Solo)` with the explicit union.

### L-6. "AI Bootstrap" heuristic "≈ 50% bodyweight scaled by experience for compounds" — gendered, age-blind

- **Location:** FR-13 (line 295).
- **Critique:** "50% bodyweight" for a novice 78kg male maps to 40kg starting squat. For a novice 55kg female it's 27.5kg — below the 20kg bar. For a 65-year-old novice it's potentially injurious regardless of sex. The Questionnaire collects sex, age, injuries — but the heuristic doesn't reference them.
- **Suggested fix:** Heuristic must take (sex, age, experience, injuries) as inputs. Add a "starting load lookup table" sourced from a coach, not a single linear scaler.

---

## Summary table

| Sev | ID | Topic | Block-on |
|---|---|---|---|
| critical | C-1 | Three load-bearing assertions undefended | §1 |
| critical | C-2 | v1 In-Scope is enterprise-launch wearing startup t-shirt | §6.1 |
| critical | C-3 | WALS target 2,500/D90 has no derivation | §8 |
| high | H-1 | "No competitor combines all three" stale at D180 launch | §1 |
| high | H-2 | Six personas, three JTBDs, no anti-personas | §2 |
| high | H-3 | FR-1 numbered first but depends on FR-3 + FR-8 | §4.1 |
| high | H-4 | $0.40 AI cost ceiling has no derivation | §7.3 |
| high | H-5 | 70% trainer-override threshold arbitrary | SM-C5 |
| high | H-6 | Spotter half-credit ASSUMPTION drives Progression Index | FR-5 |
| high | H-7 | Marketplace simultaneously in §6.1 and §6.2 | §6 |
| high | H-8 | PR Card watermark default = launch-blocker | OQ-7 |
| high | H-9 | Voice fallback ASSUMPTION inside an FR | FR-8 |
| medium | M-1 | UJ-1 references scheduling, not a v1 FR | UJ-1 |
| medium | M-2 | "Six loggers" counts seven; bodyweight is not a logger | FR-8 |
| medium | M-3 | ACWR thresholds cited but uncited | FR-37 |
| medium | M-4 | Solo D30 target == pivot trigger | SM-4 |
| medium | M-5 | "Global day one" vs single-region | §1/§7.9 |
| medium | M-6 | SM-C3 numbering collision | §7.3/§8 |
| medium | M-7 | Light+dark parity restated twice | §11 |
| low | L-1 | Trainer response-time unmeasurable | FR-24 |
| low | L-2 | Anita + Vikram have no UJ | §2.4 |
| low | L-3 | ~15 templates — authored by whom | FR-9 |
| low | L-4 | RTO 4h without on-call | §7.9 |
| low | L-5 | "Tenant" overloaded | §3 |
| low | L-6 | Bootstrap heuristic gendered/age-blind | FR-13 |

---

*End adversarial review. Recommend addressing all CRITICAL + HIGH before Finalize step 7 (status flip to `final`). MEDIUM + LOW can carry into UX / Architecture phases as known gaps.*
