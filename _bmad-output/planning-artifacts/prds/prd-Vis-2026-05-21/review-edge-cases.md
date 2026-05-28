---
title: Vis PRD — Edge Case Hunter Review
status: draft
created: 2026-05-22
reviewer: bmad-review-edge-case-hunter
scope: prd.md (FR-1..FR-43, UJ-1..UJ-3) + addendum.md
method: path enumeration — only unhandled branches reported
---

# Edge Case Findings — Vis v1 PRD

Method: walked every branching path + boundary in PRD §4 functional requirements, §2.4 user journeys, §7 cross-cutting NFRs, and addendum state machine. Reports unhandled paths only. Handled paths discarded silently.

Severity legend: **critical** (data loss / safety / state corruption), **high** (broken UX or RBAC), **medium** (under-specified rule or metric ambiguity), **low** (cosmetic / Open-Q-bait).

---

## A. PT Relationship State Machine (FR-28)

### A1. [critical] Simultaneous trainer-decline + client-requests-second-trainer race

**Scenario:** Aarav submits priority list (Priya #1, Sara #2). Priya taps Decline at t=T. At t=T+200ms (before Aarav's app receives the decline), Aarav cancels Priya and submits a fresh request to Sara. Server now has: (a) decline from Priya, (b) cancel-and-reissue from Aarav, both targeting the same client request record.

**Gap:** FR-25 says "first-come-first-serve accept" and "Trainer can decline + transfer", but the simultaneous decline-from-trainer + cancel-from-client collision is unspecified. Aarav could end up with two PtActive trainers, or zero with the auto-fallback silently swallowed.

**Suggested PRD addition:** Add consequence bullet to FR-25: "Server treats `(client_request, trainer)` pair as the unit. Decline + client-reissue at <1 s collision → server picks the later-wall-clock event; loser receives a 'request superseded' notification. A client may have at most one PtActive at a time per Tenant; second simultaneous accept rejects with conflict code." Open Q: cross-Tenant PtActive — can Aarav be PtActive with Priya at brand-X AND a Solo Trainer simultaneously? (cross-ref §2.1 "client may hold Gym Client … plus Solo Client … in parallel").

### A2. [critical] PT-end mid-Session with in-flight Co-Edit writes

**Scenario:** Priya is mid-Session with Aarav (UJ-1). Aarav's gym membership expires and Anita triggers FR-16 PT-suspend at t=T. Priya's WebSocket is still open and she writes Set 5 reps from 10→12 at t=T+100ms. Aarav's app has already received "PT membership expired" gate (FR-43) and dropped the WebSocket.

**Gap:** FR-1 says last-write-wins per Set row, FR-16 says state drives access gate, but the transitional window (PT-active→suspended while a Co-Edit Session is open) has no defined behavior. Does Priya's write commit? Is the Session finalized as-of-suspend? Does the Session record stay editable by Priya read-only later?

**Suggested PRD addition:** New FR-28b "Session-vs-PT-state precedence": "On PT state transition to `PtEnded` or `Suspended` while a Co-Edit Session is open: server (a) accepts all writes already in flight (committed within ≤2 s window), (b) closes the Co-Edit session, (c) marks Session as `frozen_at_pt_end` — Trainer retains read-only view per FR-28 alumni rule, Client retains full read access." Add to Open Questions: "Does Client also lose write access to in-progress Session, or only Trainer?"

### A3. [high] Client account deletion with active PT

**Scenario:** Aarav initiates DPDPA right-to-erasure (§7.4) while PtActive with Priya. Priya has authored 12 weeks of Plans + AI Progression Rules + Nutrition macros (FR-40) tied to Aarav.

**Gap:** §7.4 mentions erasure flow but does not specify cascade. Does the trainer-side AI history (FR-12 "persisted per (trainer × client × exercise)") get purged? Are Priya's Plan templates that were customized for Aarav now orphaned? Does the audit log (FR-26 append-only) lose the entries about Aarav, conflicting with append-only?

**Suggested PRD addition:** FR-43b "Erasure cascade": "On client erasure: (a) PII (name, photo, contact, body measurements) hard-deleted, (b) anonymized Session + Set rows retained for trainer's AI Rule continuity and audit log, keyed by surrogate ID, (c) audit log entries pseudonymized (subject → `redacted:<surrogate>`), preserving append-only invariant, (d) trainer notified of departed-client event." Conflict with FR-26 append-only flagged in §7.4.

### A4. [medium] PtEnded → NoPt threshold left as Open Q creates indefinite alumni list growth

**Scenario:** Solo Trainer Karan has 200 PtEnded clients accumulated over 3 years. Open Q 1 leaves the inactive threshold undefined. Karan's alumni list (FR-28 read-only access) grows unboundedly; DPDPA retention period unstated.

**Gap:** Open Q 1 acknowledges threshold TBD but does not specify a default upper bound. DPDPA implies a max retention period exists.

**Suggested PRD addition:** Default consequence in FR-28: "Pending Open Q 1 resolution, ship v1 with 18-month inactive threshold (no logged Session by client + no PT-attached state) → auto-transition to NoPt; trainer alumni access revoked; data anonymized per §7.4."

---

## B. Co-Edit / WebSocket (FR-1)

### B1. [critical] Both devices offline simultaneously with diverging local writes

**Scenario:** Priya and Aarav are in a basement gym; both lose connectivity. Priya logs Set 3 actual_reps=10 locally. Aarav logs the same Set 3 actual_reps=12 locally (he counted differently). Both reconnect within 5 s of each other.

**Gap:** FR-1 says "last-write-wins per Set row" — but with both queued, the wall-clock of each local write is on each device's local clock (possibly skewed). What is "last" here? The later device timestamp? Server receive-time? FR-1 Consequence 2 ("within 50 ms" rule) covers near-simultaneous online writes, not offline-queue divergence.

**Suggested PRD addition:** FR-1 new consequence: "Offline writes carry monotonic device-side `client_logical_clock` (incremented per write). Server reconciles using server receive-time as primary; ties broken by trainer-priority (Trainer write wins over Client write on the same Set within ≤2 s window). Diverging offline writes log a `coedit_conflict` event; UI surfaces a banner to both parties showing the chosen value + the discarded one." Open Q: should losing write be discarded silently or surfaced for manual resolve?

### B2. [high] Partial-write conflict at sub-50ms — multi-field Set row

**Scenario:** Priya updates `actual_load=50` and `actual_reps=10` as one trainer-side save. Aarav simultaneously updates `rpe_actual=9` and `assistance[rep_2]=spotter_assisted`. Both writes hit server within 30ms.

**Gap:** FR-1 says "last-write-wins per Set row" — but the Set row has ~10 fields. Does the entire row get overwritten by the later write (losing the other party's orthogonal field updates) or does the merge happen per-field? Consequence 2 doesn't say.

**Suggested PRD addition:** FR-1 clarification: "Last-write-wins is **per-field**, not per-row. Each field carries its own version vector; conflicting writes to the same field within 50 ms resolve to server-receive-time-latest; non-conflicting concurrent field writes merge." Add testable consequence: "Priya writes load=50; Aarav writes rpe=9 in same window → final row has both."

### B3. [medium] Client re-auths mid-Session (token refresh)

**Scenario:** Aarav's Firebase JWT expires mid-Session (default 1h). His app does a silent refresh; the WebSocket needs to renegotiate. FR-42 says backend validates JWT on every request, but WebSocket is long-lived.

**Gap:** Token-refresh handling for the live WebSocket is unspecified. Does the socket terminate on token expiry? Does Set save fail in the renegotiation window?

**Suggested PRD addition:** §7.2 new bullet: "WebSocket reauthentication: on JWT expiry, client refreshes silently; server allows up to 60 s grace before terminating socket. Set writes during grace window are accepted with old JWT claim cached server-side."

### B4. [medium] WebSocket disconnect during PR detection trigger

**Scenario:** Set 4 in UJ-1 saves locally on Aarav's offline-queued app. PR detection (FR-10) runs server-side after save. PR fires while Aarav still offline. When Aarav reconnects, does the PR Moment Screen (FR-11) still fire? Is the moment lost?

**Gap:** FR-10/FR-11 says detection runs "immediately after each Set save" but offline-queued saves commit much later. The cinematic moment ("PR fires") is tied to the live save event, not the server commit.

**Suggested PRD addition:** FR-11 consequence: "PR Moment Screen fires on the device-side save (locally) for offline-queued sessions; server-side PR detection on reconnect either confirms (no UI change) or rejects (PR was based on a discarded conflicting write → show 'PR retracted — co-edit conflict' toast, archive but don't celebrate)."

---

## C. Equipment-Aware Logger (FR-8)

### C1. [high] Pre-loaded last-used load physically unrealizable on available plates

**Scenario:** Aarav's last squat session was at a gym with 2.5 kg micro-plates; total 50 kg. Today he's at a different branch with only 25/10/5 increments. App pre-loads 50 kg suggestion (FR-8 "Defaults to last-used"); barbell visualizer cannot snap to 50 kg with this plate set (closest: 25+5+5 + 20 kg bar = 80 kg or 25+5 = 60 kg, etc.). Wait — 25+5 = 30 per side + 20 bar = 80; can't make 50 with no 10s alone? 25+10+5 = 40 per side + 20 = 100. To make 50 = 15 per side, need 10+5 = 15 → works. But 47.5 wouldn't.

**Gap:** FR-8 doesn't define per-branch plate inventory. Visualizer math assumes universal plate set. No fallback when the visualizer cannot represent the suggested load.

**Suggested PRD addition:** New FR-8b "Plate inventory awareness": "Branch may declare available plate set (default = {25,20,15,10,5,2.5,1.25}); barbell visualizer snaps suggested load to nearest achievable total. If suggestion is unachievable, visualizer shows nearest-up + nearest-down options and a 'enter exact' fallback." Add to Open Questions: "Where is plate inventory stored — per branch, per user-default, or per Session?"

### C2. [high] Voice fallback with gym background noise / non-English accent

**Scenario:** Priya says "fifty for ten" while a treadmill is at 70 dB and a barbell drops nearby. Whisper / device speech-to-text returns "fifteen for ten" or "fifty for then." App parses 15/10 or 50/0.

**Gap:** FR-8 Consequence 3 ("≥1s of speech parses 'fifty for ten' → 50/10") doesn't define confidence thresholds, confirmation UX, or error recovery.

**Suggested PRD addition:** FR-8 consequence: "Voice parse below confidence threshold (≤ 0.85) shows confirmation chip 'Did you say 50 × 10?' with one-tap accept / re-record / type. Voice parse never auto-commits a Set without visual confirmation in noisy contexts (ambient dB > 60)." Plus Open Q: "Voice in non-English accents — Hindi/Hinglish coverage v1 or post-translation only?"

### C3. [critical] Logger crash mid-Set save (between visualizer commit and server ack)

**Scenario:** Aarav drags pin to slab 10 on cable; visualizer commits to local state; app crashes before the 250 ms server commit (§7.5 NFR). On relaunch, was the Set saved?

**Gap:** §7.5 specifies p95 250 ms commit but doesn't define local-persistence ordering. No mention of write-ahead log or local journal for Set saves.

**Suggested PRD addition:** §7.5 new bullet: "Set writes hit a local journal (SQLite / IndexedDB) before UI confirmation. On relaunch, journal drains to server with idempotency key. User never sees 'did my Set save?' ambiguity." Counter-metric: "lost-Set rate (journal-drain failures) < 0.1% / WAU".

### C4. [medium] Dumbbell rack increments differ from "common-rack" snap

**Scenario:** Indian gym dumbbell racks often go 2.5 kg increments to 15 kg then 5 kg increments to 50 kg. FR-8 says "common-rack increments snap" but the snap set is undefined.

**Gap:** "Common-rack" not defined; varies by region.

**Suggested PRD addition:** FR-8 consequence: "Dumbbell snap-set configurable per branch (default: 2.5 step 2.5–25 kg, 5 step 25–50 kg, 10 step 50+). Solo users get a 'my home rack' setting in Profile."

---

## D. AI Progression Rule (FR-12, FR-13)

### D1. [high] Bootstrap heuristic for unknown / non-standard exercise

**Scenario:** Trainer creates a custom exercise "landmine rotational press" not in Vis exercise library. FR-13 heuristic ("≈ 50% bodyweight scaled by experience for compounds; isolation defaults") requires exercise classification.

**Gap:** FR-13 assumes compound/isolation class is known. Custom exercises have no class. Bootstrap fails.

**Suggested PRD addition:** FR-13 consequence: "On unclassified exercise, trainer prompted at creation to mark compound | isolation | custom. Custom → bootstrap defers to trainer-entered first prescription; AI history starts on Set 1." Open Q: "Does Vis ship a custom-exercise classifier (AI call) or require trainer manual classification?"

### D2. [critical] Claude API returns empty / null / malformed response

**Scenario:** Claude proxy returns `{prescribed_load: null}` or HTTP 500 mid-session. FR-12 outputs are assumed present.

**Gap:** No fallback specified for AI failure. Trainer ghost-overlay (FR-14) breaks.

**Suggested PRD addition:** FR-12 consequence: "On Claude proxy failure (timeout > 3 s, null/malformed response, HTTP 5xx): system falls back to deterministic rule = repeat last successful Set + 2.5% load if `rpe_actual ≤ 8` else maintain. Trainer ghost overlay labeled 'fallback' in `--vis-amber-shadow`. AI failure rate logged; > 2% trips a new SM-C11." Add SM-C11.

### D3. [critical] Claude API quota exceeded / cost ceiling breach

**Scenario:** SM-C3 cost ceiling = $0.40/WAU/mo. Mid-month, ceiling breached (e.g. viral signup spike).

**Gap:** §7.3 says "$1 / WAU triggers pivot" but does not specify the **runtime** behavior at breach. Does AI silently fail closed? Does it degrade for all users? Does it serve cached responses past freshness?

**Suggested PRD addition:** §7.3 new bullet: "Cost circuit-breaker: at $0.60/WAU monthly run-rate, AI suggestions degrade to cached-only (no new Claude calls); at $0.80/WAU, AI disabled, trainers see 'AI temporarily paused' banner with manual override path. Restored at month rollover or capacity expansion." Counter-metric SM-C1 unchanged.

### D4. [high] AI returns over-prescribed dangerous load

**Scenario:** Aarav's recent squat sessions show 80 kg × 5 @ RPE 7. AI hallucinates next prescription as 200 kg × 5 @ RPE 8 (e.g. prompt-cache poisoning, model error).

**Gap:** No safety bound on AI suggestion. Trainer-override surface (FR-14) assumes trainer will catch it, but `ai_locked = true` removes override; client follows the suggestion.

**Suggested PRD addition:** FR-12 consequence: "AI suggestion clamped server-side to [last_load × 0.7, last_load × 1.15] for working sets; out-of-band suggestions discarded and fallback rule (D2) applies. Clamp logged for trainer review." Plus consequence for FR-14: "`ai_locked = true` does not bypass clamp; clamp is server-enforced."

### D5. [medium] AI history persistence on trainer-switch (FR-12 last sentence)

**Scenario:** Aarav switches from Priya to Karan. FR-12 says "AI history persists; new trainer adopts or resets." UI for "adopt or reset" not specified.

**Gap:** Decision surface for new trainer undefined.

**Suggested PRD addition:** FR-12 consequence: "On trainer-switch, new trainer's first session-open shows a one-time modal per (client × exercise) with 3 options: (a) adopt prior AI history, (b) reset, (c) review then decide per-exercise. Default: adopt."

---

## E. Per-Rep Assistance (FR-5)

### E1. [high] All reps forced — PR detection vs Progression Index ambiguity

**Scenario:** Aarav logs a Set of 5 reps at 150 kg, all 5 tagged `forced`. FR-5 says "forced reps disqualify load-PR" (clear) but Progression Index treatment unclear when ALL reps are forced.

**Gap:** "spotter_assisted halves Progression Index credit" defined; `forced` treatment undefined. Does a fully-forced Set contribute zero to Progression Index? Or full credit with a quality penalty via `rpe_actual`?

**Suggested PRD addition:** FR-5 consequence: "Forced-rep treatment in Progression Index: each forced rep contributes 0 to ΔVol component; the Set still contributes to ΔRPE_eff (penalized via implicit RPE 10). Set with 100% forced reps = ΔVol contribution 0, Δe1RM contribution 0, ΔRPE_eff floored." Open Q candidate: "Should fully-forced Set surface a trainer alert (overreach signal)?"

### E2. [medium] Per-rep assistance tag missing for some reps

**Scenario:** Trainer taps assistance bar on reps 1, 3, 5 but skips 2, 4. Default for untagged reps?

**Gap:** FR-5 doesn't define the default state for untagged reps.

**Suggested PRD addition:** FR-5 consequence: "Untagged reps default to `unassisted`. UI shows tag chips in a strip; missing chips render greyed as 'unassisted'."

---

## F. ACWR Alert (FR-37)

### F1. [critical] Chronic baseline = 0 (new user / < 28 days history)

**Scenario:** Aarav has 3 days of Vis history (UJ-3 climax). ACWR = `acute / chronic = 12400 / 0` = ∞.

**Gap:** FR-37 formula assumes 28-day rolling chronic baseline exists. New users have undefined behavior.

**Suggested PRD addition:** FR-37 consequence: "ACWR computed only when `chronic` >= 14 days of logged history (≥4 Sessions). Prior to that, alert suppressed; profile card shows 'baseline building (day X / 14)' chip. Avoids false-positive injury alerts on new clients." Plus testable: "Aarav at day 3 → no ACWR alert; ACWR sparkline shows N/A."

### F2. [high] Deload override on non-resistance day

**Scenario:** FR-37 Consequence 2: "ACWR > 2.0 → automatic deload suggestion via FR-12 ghost overlay (next Session: 70% prescribed loads)." Aarav's next Session is a cardio-only day (FR-32). Deload-load doesn't apply to cardio.

**Gap:** Deload override semantics for non-resistance Sessions unspecified.

**Suggested PRD addition:** FR-37 consequence: "Deload override applies only to resistance Sessions. Cardio Sessions receive a deload duration cue ('cut today's cardio target by 30%') instead. Mixed Sessions: deload on resistance exercises only."

### F3. [high] Trainer dismisses ACWR alert and client gets injured

**Scenario:** Priya sees the ACWR amber alert on Aarav, dismisses without action. Aarav strains his back next session.

**Gap:** FR-37 has no dismissal tracking. No record that trainer was warned. Liability + product-learning gap.

**Suggested PRD addition:** FR-37 consequence: "Dismissals logged in audit log (FR-26 extension): `acwr_alert_dismissed` event with trainer, client, ACWR value, timestamp. Owner audit-log read shows trainer-dismissal patterns." Plus counter-metric SM-C12 candidate: "ACWR alert dismissal rate without deload > 80% sustained → alert is noise; tune thresholds."

### F4. [medium] ACWR for client across multiple trainers / contexts

**Scenario:** Aarav is Solo Client of Karan AND Gym Client at brand-X (no PT). ACWR aggregates across both contexts? Or per-trainer?

**Gap:** §2.1 allows multi-context clients; FR-37 doesn't define scope.

**Suggested PRD addition:** FR-37 consequence: "ACWR computed per-client (all logged Sets across all contexts contribute to acute + chronic). Alert visible to the active trainer; if multi-trainer, all PtActive trainers see the alert."

---

## G. Marketplace (FR-34)

### G1. [high] Trainer reported by a client they previously declined

**Scenario:** Aarav requests Priya; Priya declines. Aarav reports Priya's marketplace profile as "fake / abuse."

**Gap:** Moderation queue (FR-34) does not specify reporter qualification. Decline-then-report is plausible retaliation; FR-34 has no spam-protection or reporter-credibility weighting.

**Suggested PRD addition:** FR-34 consequence: "Abuse reports from clients who declined or were declined by the same trainer within 30 days are flagged `low_priority_likely_retaliation` and triaged after first-time-reporter complaints. Two-strikes rule: reporter with >2 dismissed reports/quarter loses report-submission rights." Open Q: cross-ref Open Q 6 moderation SLA.

### G2. [high] Cert-verified trainer's certifying body becomes invalid

**Scenario:** Priya's NSCA-CPT cert was verified at gym onboarding. NSCA revokes Priya's cert (or NSCA itself is delisted as a Vis-recognized body). Priya's badge still shows verified.

**Gap:** FR-19 covers initial verification but not revocation flow. No re-verification cadence.

**Suggested PRD addition:** New FR-19b "Cert re-verification": "Verified certs carry an expiry from the cert body (typically 2-3 years). T-30 days before expiry → trainer prompt to re-upload. On expiry without re-upload → badge auto-downgrades to 'unverified'. Vis maintains a denylist of revoked certs; weekly job rechecks active verified-cert IDs against denylist."

### G3. [critical] Trainer in two brands receives conflicting moderation actions

**Scenario:** Karan is Gym Trainer at brand-X AND brand-Y. Brand-X's owner bans him (FR-22 trainer-cert-pending → reject). Brand-Y's owner approves him. Marketplace status?

**Gap:** Cross-tenant moderation conflict undefined. Glossary says Trainer "may simultaneously hold Gym Trainer roles at multiple brands" but no rule for cross-brand actions.

**Suggested PRD addition:** FR-34 consequence: "Brand-level moderation actions are scoped to that Brand only (Karan banned from brand-X stays bannable by brand-Y). Vis-platform-level moderation (admin scope, e.g. proven fraud) globally blocks. Marketplace badge shows brand-specific status." Open Q: "Does the trainer's marketplace card show ban from brand-X publicly, or is it private?"

---

## H. Audit Log (FR-26)

### H1. [critical] Owner attempts to delete an audit log entry

**Scenario:** Vikram (Owner) tries to hide an entry showing payment mishandling. FR-26 says "append-only" but does not enumerate the deletion-prevention test.

**Gap:** Append-only stated; enforcement mechanism (DB-level constraint, API-level reject, both) unspecified. "Cannot edit history" in FR-23 covers Owner; what about platform admins / Vis ops?

**Suggested PRD addition:** FR-26 consequence: "Audit log table is INSERT-only at DB level (no DELETE / UPDATE grants for any application role). Schema-migrations or Vis-ops-level corrections go through a parallel 'audit_log_corrections' table that references the original entry by ID, never modifies it." Plus consequence: "Owner UI has no Delete action on audit-log rows; only filter + export."

### H2. [high] Staff edits a payment they didn't create

**Scenario:** Anita logs payment for member Bob (FR-17). Tomorrow, Anita is sick; staff Carla edits the amount because Bob disputed.

**Gap:** Payment edit permission boundaries unspecified. Audit log captures the edit (FR-26) but RBAC for who can edit unspecified.

**Suggested PRD addition:** FR-17 consequence: "Payment edits permitted by: (a) original logging staff, (b) any staff at the same Branch with `payment_edit` permission, (c) Owner. Each edit creates a new audit log row with `before` + `after` values; original payment row is never overwritten — edits are a chain." Add Open Q: "Should payment edits require two-staff approval (4-eye principle) above a threshold (e.g. > ₹5000)?"

### H3. [high] Payment edit on an expired PT block

**Scenario:** Staff edits a payment retroactively that extended PT block from 2026-03-01 to 2026-03-31. PT block expired 2026-04-15. Now staff changes start to 2026-04-01. Does it retroactively reactivate the access gate (FR-43)?

**Gap:** FR-17 + FR-43 interaction on retroactive payment edit unspecified.

**Suggested PRD addition:** FR-17 consequence: "Payment edits that change PT block dates do NOT auto-reactivate Access Gate; a separate PT Activation (FR-16) call is required. UI surfaces 'this edit changed PT dates — also reactivate access?' confirmation step." Audit log captures both events.

---

## I. Cross-Cutting

### I1. [high] Trainer dual-context running same client through both Solo + Gym paths

**Scenario:** Karan is Solo Trainer of Aarav AND Gym Trainer at brand-X where Aarav is also Gym Client. Open Q 5 acknowledges AI-history conflict but doesn't define UX.

**Gap:** Which `(trainer × client × exercise)` AI history wins? Which Plan is "today's workout" on Aarav's home? Whose nutrition macros (FR-40) display?

**Suggested PRD addition:** New FR-43c "Active context selector": "When a client has the same trainer in 2+ contexts, client picks 'active context' per-day. Default: the context that originated today's scheduled Session. Home shows active-context's Plan, Nutrition, AI history. Switching context surfaces a clarity banner." Open Q 5 closure path.

### I2. [critical] Client switches gym mid-Plan

**Scenario:** Aarav is on a 12-week phased Plan at brand-X (UJ-3). At week 6, he leaves brand-X for brand-Y. Plan was authored by Priya (gym-trainer at brand-X). What happens to (a) the Plan, (b) AI history, (c) past Session data, (d) Priya's authoring access?

**Gap:** Glossary says PT relationship is orthogonal to gym membership, but cross-brand client migration is undefined.

**Suggested PRD addition:** New FR-43d "Branch transfer": "On client switching Brand: (a) Past Session + Set data stays with the client (cross-Tenant client-owned), (b) Plan authored by prior trainer enters read-only mode for client; client prompted to request new trainer at new Brand, (c) AI history (FR-12) is per-trainer, so old AI history stays with Priya for read-only; new trainer at brand-Y starts fresh per FR-12 'adopt or reset', (d) Nutrition macros from prior trainer expire on transfer." Audit log captures the brand-transfer event.

### I3. [high] Locale change mid-Session (kg → lb on already-logged Sets)

**Scenario:** Aarav opens Profile, switches unit kg → lb mid-Session. Set 1 was logged at 50 kg. UI now needs to display 110 lb. Future Sets default-load needs to be in lb. Bar visualizer plate set needs to swap to imperial plates (45 lb / 25 lb / 10 lb / 5 lb).

**Gap:** §7.1 says canonical kg with UI conversion — fine for read. For *new* Set entry, what is the plate-snap behavior? Barbell visualizer (FR-8) doesn't define imperial mode.

**Suggested PRD addition:** §7.1 new bullet: "Locale-change mid-Session: already-logged Sets re-render in new unit (no data change). Equipment-Aware Logger plate set swaps per-region default (imperial: 45/25/10/5/2.5 lb; metric: 25/20/10/5/2.5/1.25 kg). User can edit personal plate set in Profile, overriding regional default."

### I4. [medium] Active Session at locale boundary (timezone change mid-Session)

**Scenario:** Aarav travels JFK → DEL mid-flight, his Session opened at 11pm EST and continues past midnight IST. Session date attribution?

**Gap:** §7.1 mentions per-user timezone with branch fallback, but Session-date assignment across timezone change unspecified.

**Suggested PRD addition:** §7.1 bullet: "Session attributes to the timezone active at Session-start; midnight rollover does not split a Session. Travel mid-Session is rare but follows the start-timezone."

---

## J. Nutrition (FR-40, FR-41)

### J1. [high] Trainer assigns macros then PT-Ends; client follows old macros indefinitely

**Scenario:** Priya assigns Aarav 2600 kcal / 180P. PT relationship ends. Aarav's Nutrition tab still shows 2600 kcal forever. He gains 15 kg over 6 months following stale macros.

**Gap:** FR-40 + FR-41 don't define lifecycle on PtEnded.

**Suggested PRD addition:** FR-41 consequence: "On PtEnded, Nutrition tab freezes the last macro plan with a banner: 'These macros were assigned by [trainer] on [date]. They may no longer apply — get a new trainer to update.' After 60 days, banner escalates to red + Nutrition tab gated behind acknowledgement."

### J2. [medium] Nutrition tab with no macros assigned yet (new PtActive client)

**Scenario:** Aarav becomes PtActive with Priya. Priya hasn't authored macros yet. Aarav taps Nutrition tab.

**Gap:** Empty-state for Nutrition tab unspecified.

**Suggested PRD addition:** FR-41 consequence: "When no macro plan exists: Nutrition tab shows 'Your trainer hasn't shared a meal plan yet — ask them during your next session.' Trainer App surfaces a 'Nutrition: not started' chip on the client's profile card."

### J3. [medium] Macro variant on training day — what counts as a training day?

**Scenario:** FR-40 mentions "weekly variants (e.g. higher-carb on training days)". Definition of "training day" — scheduled or actually-trained?

**Gap:** Training-day predicate unspecified.

**Suggested PRD addition:** FR-40 consequence: "'Training day' = day with at least one logged working Set (not scheduled, not warm-up-only). Variant applies after the first working Set saves; pre-session shows default macros. Open Q: should variant apply to scheduled days regardless of actual logging?"

### J4. [low] Macro plan with zero values / invalid arithmetic

**Scenario:** Priya saves macros 0 kcal / 0P / 0C / 0F. Or 2600 kcal but macros sum to 1800 kcal worth.

**Gap:** No validation in FR-40.

**Suggested PRD addition:** FR-40 consequence: "Validation on save: kcal > 1000; macro-derived kcal (P×4 + C×4 + F×9) within ±10% of stated kcal. Warning, not block."

---

## K. Other Reachable Gaps

### K1. [high] PR detection on the same Set both via e1RM AND 1RM AND volume

**Scenario:** Rohit hits 200 kg × 3. Both 1RM PR (load), e1RM PR (e1RM goes up too), and total-volume PR (session volume) fire from one Set save.

**Gap:** FR-10 detects 4 PR dimensions; FR-11 says "PR Moment Screen fires" — singular. Multi-PR collision UX unspecified.

**Suggested PRD addition:** FR-11 consequence: "Multiple PRs on same Set → single Moment Screen with stacked PR badges (load + e1RM + volume + rep-at-load). One share card aggregates all PRs from the Session."

### K2. [medium] Plan-block expiry FCM acknowledged but client has no PT at expiry

**Scenario:** Priya gets T-2 reminder for Aarav's Phase 1 expiry (FR-30). Before the trainer acknowledges, the PT relationship ends. FR-30 says reminders fire "until acknowledged or new Plan/Phase assigned" — but trainer no longer has access.

**Gap:** Reminder cadence orphan on PtEnded undefined.

**Suggested PRD addition:** FR-30 consequence: "On PtEnded, all open expiry reminders for that (trainer × client) auto-acknowledge as 'cancelled — relationship ended.' Client's home shows 'plan ended' card; new-trainer flow re-creates Plan-block reminders fresh."

### K3. [medium] AI bootstrap heuristic for non-binary sex / undisclosed

**Scenario:** FR-13 uses "sex" as a heuristic input. FR-27 questionnaire field "sex" — values not enumerated. Bootstrap formula references "≈ 50% bodyweight scaled by experience" — sex-specific?

**Gap:** Sex enumeration not specified. Non-binary / prefer-not-to-say bootstrap behavior undefined.

**Suggested PRD addition:** FR-27 consequence: "Sex field options: female / male / non-binary / prefer-not-to-say. Bootstrap heuristic uses bodyweight + experience as primary factors; sex modifier applied as ±5% on compounds only when female/male disclosed; undisclosed → default to lower of the two for safety."

### K4. [medium] Cardio logging (FR-32) — HR field with no wearable

**Scenario:** FR-32 lists "avg HR" as a field. §2.3 says no Apple Watch v1. How does avg HR get captured?

**Gap:** Heart rate input mechanism for cardio Sets unspecified.

**Suggested PRD addition:** FR-32 consequence: "avg HR is manual numeric entry v1 (user reads cardio machine display). Wearable-derived HR is v2 per §5 (out of scope) but field exists day 1 to capture manual values."

### K5. [low] Onboarding "injuries: free-text" — no structured triage to AI bootstrap

**Scenario:** Aarav enters "left knee meniscus tear 2023" in injuries field (FR-27). AI bootstrap (FR-13) prescribes 80 kg squat without considering this.

**Gap:** Free-text injury data not flowing into AI safety constraints.

**Suggested PRD addition:** FR-13 + FR-27 consequence: "Injury free-text passed to AI as system-prompt context with safety-conservative bias. Optional structured tags layered on top (knee / lower-back / shoulder) for trainer-side filtering of exercises. Trainer prompted to review injuries before approving first Plan." Open Q candidate.

### K6. [medium] PR Card composer — share fails / native sheet rejects

**Scenario:** Rohit picks 9:16, share-sheet opens but Instagram is not installed.

**Gap:** Share-sheet failure mode unspecified.

**Suggested PRD addition:** FR-11 consequence: "If selected target app is not installed, native share-sheet falls back to share-as-image to camera roll. Share-failed event tracked but does not affect SM-5 (since user didn't complete share)."

### K7. [low] RPE 0.5 step entry on touch UI

**Scenario:** RPE 1.0–10.0 in 0.5 steps requires fine touch granularity. Common UX pitfall.

**Gap:** Input affordance for 19-step RPE slider not specified.

**Suggested PRD addition:** UX-spec referral, not PRD-level. Flag in §11 aesthetic pointer.

### K8. [high] Same-trainer multiple-Sessions-same-day

**Scenario:** Priya runs Aarav at 7am and 6pm same day. FR-7 rest timer + ACWR (FR-37) treat as one "day" of volume or two Sessions?

**Gap:** Multi-Session-per-day attribution to acute window unspecified.

**Suggested PRD addition:** FR-37 consequence: "Acute volume = sum of all working-Set volumes within 7-day rolling window regardless of Sessions/day count. Trainer App surfaces a 'Aarav has trained 2× today' info chip if same-client opens twice in 24h."

### K9. [medium] Phase transition without trainer action (auto vs manual)

**Scenario:** Phase 1 of UJ-3 plan ends 2026-06-01. Phase 2 starts 2026-06-02. Does the transition happen automatically or require trainer click?

**Gap:** FR-29 / FR-30 doesn't define transition automation.

**Suggested PRD addition:** FR-29 consequence: "Phase transitions auto-advance on the start-date of the next Phase. Trainer receives T-2 reminder to review next Phase before it activates. Client home updates the Phase chip at activation."

### K10. [medium] Trainer-tunable Progression Index weights (Open Q 10)

**Scenario:** Open Q 10 acknowledges defaults need locking. Without defaults, Progression Index undefined for new (client × goal) pairs.

**Gap:** Open Q acknowledges but does not ship a default.

**Suggested PRD addition:** Glossary 'Progression Index' addendum: "v1 default weights (pending Open Q 10): hypertrophy (0.3, 0.5, 0.2); strength (0.6, 0.2, 0.2); endurance (0.2, 0.6, 0.2); general (0.4, 0.4, 0.2). Trainer-tunable per (client × goal); change logged."

---

## Top critical/high summary (for caller relay)

1. **A1 [critical]** FR-25 §4.6 — Trainer-decline ↔ client-cancel-and-reissue race produces dual-trainer state.
2. **A2 [critical]** FR-1 + FR-28 — PT-end mid-Session with in-flight Co-Edit writes; commit window undefined.
3. **A3 [high]** §7.4 — DPDPA erasure cascade vs audit-log append-only conflict; no cascade rule for AI history / Plans / Nutrition.
4. **B1 [critical]** FR-1 — Both devices offline with diverging local writes; "last" wall-clock under skew unspecified.
5. **C3 [critical]** FR-8 + §7.5 — Logger crash mid-Set save; no local journal / idempotency guarantee.
6. **D2 [critical]** FR-12 — Claude API empty/null/malformed response has no fallback rule.
7. **D3 [critical]** §7.3 — Cost-ceiling breach runtime behavior undefined (circuit-breaker missing).
8. **D4 [high]** FR-12 + FR-14 — AI returns dangerous over-prescribed load; no server-side clamp; `ai_locked` bypasses trainer.
9. **F1 [critical]** FR-37 — ACWR for new users (chronic=0) divides by zero; baseline-building rule missing.
10. **G3 [critical]** FR-34 — Cross-brand moderation actions on multi-tenant trainer undefined.
11. **H1 [critical]** FR-26 — Append-only enforcement mechanism (DB constraint vs API) unspecified; ops-correction path undefined.
12. **I2 [critical]** Glossary + FR-43 — Client switches gym mid-Plan; cross-Tenant data + Plan + AI ownership undefined.

---

*End of edge-case review. Method: exhaustive path enumeration of FR branches + UJ entry/exit states + cross-cutting state transitions. Findings limited to unhandled paths.*
