---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
lastStep: 14
status: completed
completedAt: 2026-05-23
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/addendum.md
  - _bmad-output/planning-artifacts/research/domain-vis-fitness-pt-research-2026-05-21.md
  - docs/superpowers/specs/2026-05-04-vis-design.md
  - prototype/src/
  - docs/brand-ref/reference_light.png
  - docs/brand-ref/reference_dark.png
  - CLAUDE.md
decisionAids:
  - _bmad-output/planning-artifacts/ux-spec-step-02-party-mode-2026-05-22.html
  - _bmad-output/planning-artifacts/ux-spec-step-03-preview-2026-05-22.html
  - _bmad-output/planning-artifacts/ux-spec-step-03-party-mode-2026-05-22.html
  - _bmad-output/planning-artifacts/ux-spec-step-04-preview-2026-05-22.html
  - _bmad-output/planning-artifacts/ux-spec-step-05-preview-2026-05-22.html
  - _bmad-output/planning-artifacts/ux-spec-step-05-party-mode-2026-05-22.html
  - _bmad-output/planning-artifacts/ux-spec-step-06-paths-2026-05-22.html
  - _bmad-output/planning-artifacts/ux-spec-step-06-plain-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-spec-step-06-party-mode-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-spec-step-07-preview-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-spec-step-08-preview-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-design-directions-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-spec-step-10-preview-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-spec-step-11-preview-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-spec-step-12-preview-2026-05-23.html
  - _bmad-output/planning-artifacts/ux-spec-step-13-preview-2026-05-23.html
scope:
  - client-app
  - trainer-app
  - admin-web
anchorPriorities:
  - Equipment-Aware Logger fidelity (6 gesture vocabularies)
  - Real-time co-edit + multi-role context model
project: Vis
author: Gauravprakashshinde
date: 2026-05-22
companion_html: ux-design-specification.html
---

# UX Design Specification Vis

**Author:** Gauravprakashshinde
**Date:** 2026-05-22

---

## Executive Summary

### Project Vision

Vis makes progressive overload effortless for anyone who lifts — solo or with a trainer. Where competitors force gym-goers to type weights and remember last week, Vis logs through visual gestures that mimic the equipment in front of you: drag the cable pin, snap plates onto a bar, slide the dumbbell rack. Where competitors silo lifters, trainers, and gyms into three apps, Vis is one product where a trainer and client can edit the same set log live during an in-person session — and where a gym can run that experience across all its branches.

Three load-bearing assertions drive every UX decision:

1. **Primary user pain = remembering + inputting progression.** Visual equipment-aware logging is the differentiator; the orb is a brand surface, not the moat. Beauty serves usability.
2. **Trainer ↔ client real-time in-person co-edit + multi-role multi-branch context = structural moats.** No competitor unifies all three (Trainerize = async-only and single-org; FitBudd = ops-only; Bevel = solo-only).
3. **Free v1; global day one.** Units (kg + lb), date formats, time zones — toggle-aware from the first screen. India-anchored copy + INR-ready data model + RTL-stub from launch.

**Anchor priorities for v1 design quality:**

- **Anchor 1 — Equipment-Aware Logger fidelity.** Six logger interactions (cable pin-stack drag, barbell plate-snap with auto-sum, dumbbell number-line slider, machine pin-stack, bodyweight silhouette + vest field, kettlebell discrete picker, band resistance-tier picker), one consistent gesture grammar. The single biggest UX investment v1.
- **Anchor 2 — Real-time co-edit + multi-role context model.** The structural SaaS wedge. Visible presence + provenance during live in-person sessions; identity + scope clarity for trainers operating across multiple brands and owners operating across multiple branches.

Brand carrier (separate from anchors): cinematic amber, molten emissive, orb-as-organism as a *brand surface* with backend-tied state. Restraint over showiness; warm not cool; one icon only across all three apps. Light + dark theme parity ships v1 as a quality bar (not as differentiator). Reduce-motion + reduce-transparency degradation is functional, not optional.

### Target Users

Vis serves **six role-contexts** across two onboarding paths (solo, gym). Spec body uses functional role labels. Internal team shorthand uses Game of Thrones house codenames — included once below as a parenthetical glossary, then dropped.

| Role | Path | Branch scope | Internal codename |
|---|---|---|---|
| Gym Client (beginner arc) | gym | single branch | House Tarly |
| Solo Client (independent strength athlete) | solo | n/a | House Greyjoy |
| Gym Trainer (sworn, disciplined) | gym | single branch | House Tarth |
| Solo Trainer (+ optional multi-brand Gym Trainer) | solo + gym | per affiliation | House Seaworth |
| Gym Staff / front-desk | gym | single branch | House Tully |
| Gym Owner (chain operator) | gym | all brand branches | House Lannister |

Secondary archetypes from domain research, retained for edge-persona checks: longevity-led client (House Arryn), hybrid remote trainer (House Reed), single-branch owner (House Manderly), junior staff apprentice (House Payne), branch manager / loyal lieutenant (House Cassel).

A Solo Trainer may concurrently hold gym-trainer roles at multiple brands. A Gym Client may simultaneously hold a Solo Client relationship with an independent trainer. PT-relationship state is orthogonal to gym membership. RBAC scopes data to branch for everyone except Gym Owner.

**Anchor JTBDs:**

- **Client (any path):** "Tell me what to do next, and let me log it without typing."
- **Client (PT-attached):** "Let me train with my trainer, not around them." Co-edit + prescribed-vs-actual + earned PR moments.
- **Trainer (any path):** "Give me one place to author programs, track every client's adherence, and coach during the session."
- **Staff:** "Let me run member ops without spreadsheets."
- **Owner:** "Show me which branches and which trainers are working — and which aren't."
- **All:** "Make my data feel mine, my equipment feel mine, my progress feel earned."

### Key Design Challenges

Ordered by structural weight, not by FR number. Each challenge names the FR/SM it lives inside and the failure mode if mishandled.

1. **Co-edit conflict visibility — how the losing write degrades gracefully (FR-1, FR-3, NFR §7.2).** Two devices editing the same Session. Last-write-wins per Set row + server-stamped monotonic version is the *transport* contract — not the *UX* contract. If Trainer types 12 reps at t=0 and Client types 10 at t=40 ms, the Client's screen flips from 10 → 12 *after* their own input committed. That is the design problem. Required surface: who-just-edited-what (presence), prescribed-vs-actual divergence (provenance), offline-queue state, optimistic-then-reconciled writes — all without modals mid-set. Conflict surfaces as cues (subtle pulse, recently-edited-by tint), never alerts. The trainer-client Connection Orb is the affordance for "we're in sync."

2. **Multi-role + multi-branch context switching (FR-25, FR-28, FR-43, §5 RBAC).** A Solo Trainer who also holds gym-trainer roles at two brands needs to switch identity per session/client without mode confusion. A Gym Owner toggles between brand-aggregate and per-branch drill-down. Branch-scope chip in nav is always present + glanceable. AI history per (trainer × client × exercise) follows trainer identity, not human identity — Open Question 5 in the PRD flags the dual-context AI-history collision. Wrong-scope writes are a class of bug we design out at the interaction layer, not just at the data layer.

3. **Gesture-vocabulary consistency across the six Equipment-Aware Loggers (FR-8).** Cable, barbell, dumbbell, machine, bodyweight, kettlebell, bands. Each is a custom interaction; the discipline is that the grammar across all six is consistent enough that a user who learns cable can pick up bands in one tap. Shared vocabulary: drag, snap, slide, pick, toggle, scan. Numeric-keypad fallback always available. First-contentful render ≤ 200 ms (FR-8 NFR); last-used load pre-loaded per (user × exercise). Failure mode = six bespoke loggers that feel like six different apps, which collapses the differentiator.

4. **Onboarding split paths sharing one Questionnaire (FR-27, FR-43).** Gym path = QR scan → roster match → trainer browse → first session pre-loaded. Solo path = sign-up → onboarding → template marketplace → AI bootstrap. The ~10-field Questionnaire is identical; the surrounding screens diverge sharply. Directly gates SM-1 (WALS) and SM-4 (Solo D30 retention). Risk = duplicate flows that drift. Solution = shared Questionnaire component with path-aware shell.

5. **AI ghost-overlay trust calibration — confidence, dismissal grammar, staleness (FR-12, FR-14, NFR §7.3).** Trainer-only v1. The challenge is not just "discoverability vs noise" — it is *trust calibration*. Trainers will reject AI wholesale on day 3 if the first wrong suggestion isn't gracefully dismissible. Required surfaces: confidence display (where does this number come from), one-tap accept, long-press override, `ai_locked` autopilot toggle, and **AI suggestion staleness self-dismissal** — the Claude proxy 20% cold path is 2–6 s; ghost suggestions that arrive after the user logs the set must self-expire. Counter-metric: SM-C5 trainer-override rate > 70% trips a pivot.

6. **PR Moment composer + share surface — "earned, not cringe outside the app" (FR-10, FR-11).** Cinematic amber animation on PR detection. One Vis template, three aspect ratios (square / 9:16 / 4:5). Watermark default ON, user-togglable OFF per share (persists per user). The composer is hollow if cards don't get shared — SM-C10 PR-card-with-no-share rate > 80% kills the social-moat assumption. Tone discipline: restraint, "earned" over "celebrated." Differentiates from gamified-badge competitors (Fitbod confetti, Hevy social feed). Card must read as content the user wants to repost to Instagram Stories or WhatsApp Status — not a watermark-laden vendor ad.

7. **i18n + units day 1 — toggle UI from launch (NFR §7.1).** kg/lb, m/ft+in, DD/MM vs MM/DD, per-locale time zones. Every numeric input + display carries a unit affordance without cluttering the row. Onboarding establishes preference; each Equipment-Aware Logger respects it; PR cards render in the user's preferred unit. India-anchored: INR-ready data model + Hinglish-friendly copy + RTL stub from launch. Hevy is kg/lb-only; this is structurally defensible if executed.

8. **One-handed reachability — thumb-zone discipline during a set.** The logger is operated mid-rep, often sweaty, often with the phone propped on a bench or held in one hand. All set-row primary controls (Done toggle, +reps, weight change, rest-timer dismiss) must fall inside a 56 mm bottom-quadrant thumb arc on a Pixel 6a / iPhone 14 form factor. Secondary controls (warm-up flag, failure intent, assistance tagging) accessible via long-press or sheet — never tucked at the top of the screen. Hit targets ≥ 44 × 44 pt mandatory (NFR §7.7).

9. **Offline-first session integrity — queued writes, visible state, 5 s reconnect drain (FR-1, NFR §7.5).** Indian gym basements have spotty wifi. Writes must queue locally and reconcile on reconnect within 5 s (PRD promise). UI must distinguish: committed (server-confirmed) vs queued (local, uncommitted) vs stale (failed reconcile, user must retry) at the Set-row level. Connection Orb shows offline state. The Set-save idempotency UUID (NFR §7.5) is the architecture lever — the UX is rendering it visible. If the user can't tell "did this save?" the co-edit moat collapses.

10. **Trainer ↔ client trust handoff + data portability (FR-25, FR-28).** When a client switches trainers within the same gym (or a trainer is declined / transferred per FR-25 single-active-state invariant), the Onboarding Questionnaire + active Plan + AI history must flow to the new trainer with the client's consent. PtEnded retains read-only access for the prior trainer (FR-28 alumni). The UI surface for "consent to share my history with [new trainer]" is the trust-handoff moment — handled badly, it kills retention; handled well, it makes Vis the gym brand's churn-defense lever.

11. **Reduce-motion + reduce-transparency fallback is functional, not optional (NFR §7.7).** Orb breathing, glass blur, shimmer all degrade gracefully. Reduce-motion = orb tension states readable in static frames. Reduce-transparency = glass collapses to warm-tinted solid (`#1A1310` dark, `#FFF3E8` light). VoiceOver / TalkBack labels intact on every interactive element. WCAG 2.1 AA on Admin Web v1.

12. **Orb-as-organism brand surface — each state names a backend data source (constraint, not anchor).** Five tension states tied to backend-observable signals:

    | State | Data source | Surfaces |
    |---|---|---|
    | At rest | no active session in last 24 h | Client home idle |
    | Under load | active session WebSocket connected, set in progress | Active Session header |
    | Peak tension | last working set of session, PR detection running | Active Session climax |
    | Recovered | session marked complete + recovery timer running | Client home post-session |
    | Disconnected / Stale | WebSocket disconnect > 5 s OR offline queue non-empty | Active Session offline banner |

    The orb is a brand surface, not the moat. Discipline: each state has a data source; the orb never lies about state. Stacked orbs render weekly volume on Progress Tab; Connection Orb appears in Active Session header during live co-edit; Recovery Orb on Client home colour-codes muscle-group readiness.

### Design Opportunities

Four structural moats + one craft principle layer. Cuts from prior draft: light/dark parity (table stakes), cross-app brand discipline (hygiene), house-naming-as-moat (internal-only) — moved to design principles below.

**Moats — defensible, hard to copy, compound over time:**

1. **Equipment-Aware Logger as flagship gesture vocabulary (industry-first).** Six custom logger interactions, one consistent design language. Drag, snap, slide, pick, toggle, scan. Each logger feels like the equipment in front of the user. Competitors use numeric keypads (Hevy, Strong, Fitbod) or watch-motion (Motra — leg exercises poor). Vis owns the gesture moat; data flywheel compounds (per-equipment usage telemetry refines defaults).

2. **Real-time co-edit live-session feel — visible presence + provenance + conflict grace.** Two phones, 3 feet apart, editing the same Set rows live. Trainerize, TrueCoach, Everfit are remote-first; their data model assumes async. Mariana Tek just launched Appointments 2026-05 but still treats the trainer as scheduler, not co-editor. Real-time CRDT-grade sync is hard to retrofit. Connection Orb makes the WebSocket-invisible-thing visible — tension stem pulses on each write from either side.

3. **Multi-role multi-brand context model — the SaaS wedge.** A trainer who works across 3 brands AND owns one independent practice. A client who's gym-attached at one brand AND solo with an independent trainer. An owner who sees all branches without seeing other brands. No competitor handles this — Trainerize forces separate accounts; FitBudd ops are single-org. Data-model moat surfaced through interaction (branch-scope chip, role-context switcher, audit log scope visible at the action).

4. **Offline-resilient logging as trust differentiator.** Indian gym wifi is unreliable; "my last 8 sets just vanished" is the killer app failure. Vis queues writes locally, reconciles on reconnect, surfaces queued-vs-committed state per Set row. Connection Orb shows offline state without alarming. None of the consumer logging apps (Hevy, Strong, Fitbod) treat offline as a first-class UX state — they treat it as an infra failure. This is structurally defensible because the data-flywheel reward is asymmetric: gym-floor users who trust the app log more sets, which improves AI suggestions, which retain trainers.

**Brand surface (craft principle, not moat):**

- **Orb-as-organism — molten amber, cinematic emissive, restraint over neon.** Five tension states tied to backend signals (per Challenge 12). Stacked orbs = weekly volume. Connection Orb = trainer ↔ client link. Recovery Orb = muscle-group readiness. Single icon, layered visual. Carries information *if* the data-source constraint holds.
- **PR Moment cinematic "earned" tone.** Molten emissive sweep, restraint over confetti, share-sheet-ready. Vis watermark default-on signals brand without nagging. Differentiates against gamified-badge competitors but does not constitute a structural moat — Strava clipped the format in 2014.

**Quality bars (not moats, must ship anyway):**

- Light + dark theme parity v1 — baseline 2026 professionalism.
- Cross-app brand discipline — one icon (orb), no other illustrative motifs across all three apps. Hygiene.
- WCAG 2.1 AA on Admin Web; mobile hit targets ≥ 44 × 44 pt; reduce-motion + reduce-transparency intact.
- i18n + units toggle wired into every numeric surface from day 1.

**Internal team shorthand (do not surface in user-facing copy):**

- GoT house codenames (Tarly, Greyjoy, Tarth, Seaworth, Tully, Lannister) used in standups, design crit, and PR titles for fast role-context reference. Spec body uses functional role labels. Glossary table in §Target Users is the only place codenames appear in this document.

---

## Core User Experience

### Defining Experience

The Vis core loop is multi-actor. The PRD's first structural moat — real-time co-edit between trainer and client on the same Set — is the lead. Stated once:

**"Trainer and client both touch the same Set, in the same room, on two devices. The Set knows who wrote it."**

Solo-lifter framing (no trainer present) is a secondary loop, not the headline. This positioning is deliberate: every Cluster-A competitor (Fitbod, Hevy, Strong, Motra, Bevel) ships a polished solo logger. Vis's defensible wedge is the multi-actor moment plus the multi-role context (trainer multi-brand, owner cross-branch).

The most-frequently-performed action is **logging a Set on an Equipment-Aware Logger**, by *either* trainer or client. The Set row carries `prescribed` + `actual`, `source ∈ {TRAINER, AI, AI_BOOTSTRAP, CLIENT}`, `actor_id`, `committed_at`. The Connection Orb in the Active Session header pulses on each write from either side; the Set row badge shows last-write provenance.

**Set-row prefill source — the Logger never opens in a vacuum.** Prefill cascades, in order:

1. AI ghost suggestion if present (FR-12 / FR-14)
2. Trainer's assigned prescribed value for this Session day (FR-3)
3. Last-Set memory for this (user × exercise) (FR-8 default)
4. AI bootstrap if no prior Set history (FR-13)
5. Empty + numeric-keypad fallback

**Five loops form around the core:**

1. **Pre-session loop (Trainer)** — author / assign program → review client adherence + ACWR alerts → coach next Session.
2. **Active-session loop (Trainer + Client)** — co-edit Sets, prescribed-vs-actual, warm-up flag, failure intent, per-rep assistance.
3. **Abandonment loop (Client)** — first-class flow for skipped prescribed Sets: explicit "Skip" affordance with reason capture (Fatigue / Equipment busy / Injury / No time / Other); Progression Index excludes; trainer's adherence dashboard reflects.
4. **Post-session loop (Client)** — Progression Index update, e1RM graph tick, Recovery Orb refresh, PR detection + share-sheet.
5. **Ops loop (Staff + Owner)** — member CSV → PT activation → payment logging → reminder cadence → branch dashboards.

If the multi-actor Set-write loop breaks, the moat collapses. If it works, switching cost compounds for both trainers and clients on Vis.

### Platform Strategy

Three apps, two paradigms, one brand.

| App | Platform | Primary input | Posture |
|---|---|---|---|
| Client App | React Native (iOS + Android) | touch, single-hand thumb arc | mid-session, sweaty, distracted, sometimes offline |
| Trainer App | React Native (iOS + Android) | touch, two-hand authoring + one-hand in-session | high context-switch rate (8 sessions/day) |
| Admin Web | Angular (browser) | mouse / keyboard, dense tables, dashboards | seated at front-desk or office; spreadsheet-replacement |

**Cross-app constraints:**

- **Offline-first mandatory on Client + Trainer.** NFR §7.5 idempotency UUID is the architectural lever; UX renders queued-vs-committed state per Set row. Local ack ≤ 100 ms; server ack `p95 ≤ 250 ms` when online.
- **Online-only acceptable on Admin Web.** Ops surfaces don't need disconnection survival.
- **WebSocket transport for Co-Edit Sessions** on mobile; polling fallback at 10 s. Telemetry for "First Co-Edit moment" gates on `ws.readyState === OPEN && peer_presence === true` — polling-fallback is silent degradation, not Co-Edit.
- **Admin Web ↔ mobile Co-Edit write-conflict rule.** Admin Web writes to a Set row inside an active Co-Edit Session are **advisory only** — they queue server-side and flush after the Session is marked complete by either party. Trainer / Client mobile writes are authoritative. Eliminates the data-loss feel from a Staff edit landing mid-Set.
- **Real-time UI not required on Admin Web v1.** Dashboards refresh on page load or explicit reload.
- **Light + dark theme parity** v1 across all three apps (quality bar).
- **WCAG 2.1 AA** on Admin Web. Mobile: VoiceOver / TalkBack labels on every interactive element, 44 × 44 pt hit targets, reduce-motion + reduce-transparency fallback (NFR §7.7).
- **Cold start ≤ 2.0 s** on mid-tier Android (Pixel 6a / Galaxy A54). Logger first-contentful render ≤ 200 ms.
- **i18n + units day 1** — kg/lb, m/ft + in, DD/MM vs MM/DD, per-locale time zones. India-anchored: INR-ready data model, Hinglish-friendly copy, RTL stub from launch. English-only translation v1.

**Auth + platform plumbing UX:**

- **FCM token lifecycle.** Token rotates on reinstall / device restore. Silent re-link on next foreground; if it fails, banner prompts the user to re-enable notifications.
- **Deep-link strategy.** Universal Links (iOS) + App Links (Android) + fallback HTTPS handler at `vis.app/o/{path}`. No firebase dynamic links (sunset). Cold-start from a deep link with logged-out state opens sign-in then resumes the deep-link target.
- **OAuth callback handling.** Email-link / Google / Apple cold-start opens app to a "completing sign-in" screen with progress indicator. Failure mode (link expired / rotated) returns to sign-in with a named error.
- **Notification permission priming.** iOS: a single one-shot priming screen before the system permission alert. Android: same priming on API 33+.

**Operational scope (single-region v1, declared as UX promise):**

- Cloud Run **asia-south1 only** v1. UX promise: India-anchored, low-latency in IN, no EU / US trainers on Vis v1 (data residency).
- AI cost ceiling **as a platform-level budget primitive**, not just a counter-metric. Inference frequency throttle activates at $0.80 / WAU rolling 7-day average (per PRD §7.3).
- **Audit-log retention window** = full v1 retention (no expiry), per PRD FR-26 + NFR §7.9 audit integrity.

### Effortless Interactions

Vis-distinct surfaces first; baseline second; async-effortless (skeleton + ghost) last. Items competitors deliver at parity (PR detect, AI accept, What's next) are demoted from the Vis-distinct tier.

**Vis-distinct effortless — the moat surfaces:**

1. **Logging a Set on the equipment in front of you, with prefill.** Tap exercise → Logger pre-loads the right value through the 5-step prefill cascade → adjust by gesture (drag pin, snap plate, slide slider) → tap Done. No typing in the happy path. Voice fallback for trainer in-session.
2. **Both sides editing the same Set with provenance.** Trainer types prescribed; client lifts actual; UI shows both with a divergence tint; actor avatar + timestamp render on the row within 500 ms of either write.
3. **Switching identity context.** Trainer multi-brand: tap branch-scope chip in nav → identity swaps, audit-log scope flips, AI history per (trainer × client × exercise) follows. Owner cross-branch: same chip → aggregate or per-branch drill-down. Zero menu-diving.
4. **Reconciling offline writes inside a live Co-Edit.** Connection Orb shows offline state without alarm. On reconnect, 5 s drain reconciles; the row never shows "8 sets just vanished." Conflicting queued writes resolve via server-monotonic stamp; divergence shown as a one-line non-modal banner.
5. **Correcting a mis-logged committed Set.** Long-press on a Set row → Edit / Mark warm-up / Mark abandoned. Audit log records the edit. Happens 10× more than voice fallback; first-class.
6. **Onboarding via QR (gym path).** Front-desk staff QR → sign in → roster matched → trainer browse → first session pre-loaded. No manual data entry beyond the Questionnaire.
7. **Trainer client-handoff between back-to-back clients.** End Session sheet auto-promotes the next client in today's schedule; "Switch to {next client}" CTA is the primary action. Prevents fat-finger on the previous client's log.

**Baseline effortless — table-stakes; must work:**

8. **Knowing what to do next.** Today's Plan card on Client home; Today's schedule on Trainer home. One tap to enter Active Session.
9. **Reading recovery status.** Recovery Orb on home colour-codes per-muscle readiness from last Session — green / amber / red — at a glance.

**Async-effortless — not real-time, but feels seamless via skeleton + ghost:**

10. **AI suggestion acceptance.** Ghost overlay appears asynchronously when the Claude proxy returns (median 200 ms hot path, up to 6 s cold path). The slot reserved for the suggestion shows a skeleton while pending. Once it lands, one-tap accept is local-instant. Long-press = override. `ai_locked` autopilot toggle removes the override surface for clients on full autopilot.
11. **PR detection.** Set save → PR detection runs server-side → cinematic moment screen fires asynchronously on detection. User doesn't compute or trigger anything. If detection fails to fire within 3 s, the moment is silently skipped (no fake celebration). Composer offers share to native share-sheet.

### Critical Success Moments

Eight make-or-break moments, ranked by defensibility tier. Tier 1 = compound moats hard to copy. Tier 2 = baseline gating retention. Tier 3 = table-stakes (necessary, not differentiating).

**Tier 1 — Defensibility moments (the wedge):**

1. **First Co-Edit moment** (UJ-1, typically D1–D7 for PT-attached client). Trainer + Client both editing the same Set on two devices; Connection Orb pulsing on each write. **Gates SM-3 (≥ 25% Trainer-led Sessions with bidirectional Co-Edit by D90).** Telemetry gates on `ws.readyState === OPEN && peer_presence === true`; polling-fallback is silent degradation, not a Co-Edit moment. UI distinguishes "co-edit live" from "co-edit catching up."
2. **First Branch-Switch identity context** (Trainer multi-brand). A Solo + Gym Trainer switches from Brand A morning session to Brand B afternoon; branch-scope chip reflects the new context obviously; audit-log writes scope to Brand B; AI history per (trainer × client × exercise) follows the trainer identity. Wrong-scope write here is a data bug at the interaction layer.
3. **First PR Share to external platform** (D7–D60). PR detected → composer renders three aspect ratios → user actually shares to Instagram Stories / WhatsApp Status / X / camera roll. **Gates SM-5 (≥ 30% share rate by D180) and SM-C10 (no-share > 80% kills viral moat).** Detection without share is a hollow Moment.
4. **Owner Morning Glance** (daily). One screen: yesterday's revenue, branch-by-branch active count, trainer leaderboard delta, churn-risk list, recent audit-log entries. **Maps to SM-7 (≥ 50% trainer adherence-monitoring D180) and SM-8 (≥ 2 audit-log queries / brand / week).** Drill-into-7-tabs = dashboard fails its job.

**Tier 2 — Baseline retention moments:**

5. **First Completed Onboarding D0.** Sign-in → Questionnaire → first trainer browse (gym) or first template pick (solo) → first Session pre-loaded. **Maps to SM-6 (≥ 60% completion by D90) and SM-C8 (no-log > 50% trips).**
6. **First Set logged** (D0). Onboarding → first exercise → first Logger touch. If this fails (logger > 200 ms, crash, requires typing), user churns same-day.
7. **First reconcile after offline drop.** Mid-Session WebSocket disconnect → app shows offline banner without alarm → reconnect drains queue → no data lost. "8 sets just vanished" = product trust evaporates. **Gates SM-2 (D7 retention ≥ 35% by D90).**

**Tier 3 — Table-stakes (must work; not defensible):**

8. **First trainer-AI-accept.** Trainer sees ghost overlay; one-tap accept feels right; suggestion was sane. **SM-C5 watches trainer-override rate > 70%.** Competitors deliver at parity; necessary but not sufficient.

**Dropped from prior draft:**

- *First PT-Renewal D28* — no PRD SM covers renewal directly. Tracked internally as a leading indicator for SM-2 (D7 retention proxy); not surfaced as a UX-spec Success Moment.
- *First PR detection* (as distinct from share) — folded into Moment 3 above. Detection without share is SM-C10 firing.

### Experience Principles

Five testable principles + one operational. Every UX decision must serve at least one; if it violates one, it needs a stated reason in the spec.

1. **No typing in the happy path.** *Verifiable:* count keystroke events on Set log; happy path fires zero. Numeric keypad exists as fallback only. Survives v2 voice-input as a Tier-1 modality.
2. **Visible presence + provenance over silent state.** *Verifiable:* every co-edited row renders actor avatar + last-write timestamp within 500 ms of write commit. Silent state is a bug.
3. **Celebration UI only fires post-server-confirm.** *Verifiable:* no cinematic moment animation triggers on an optimistic write; all PR / streak / progression celebrations gate on a server-confirmed event. PR cinematic ≤ 1.5 s and dismissible.
4. **Orb state changes are backed by a named signal.** *Verifiable per state:*

   | Orb state | Signal source |
   |---|---|
   | At rest | no active Session in last 24 h |
   | Under load | `ws.readyState === OPEN && session.active === true` |
   | Peak tension | `session.last_set.is_working === true && session.last_set.intent === 'failure'` |
   | Recovered | `session.completed_at !== null` + post-session timer running |
   | Disconnected / Stale | `ws.readyState !== OPEN OR offline_queue.depth > 0` |

   Decoration without a named signal is forbidden.
5. **Branch-scope chip always present, always glanceable.** *Verifiable:* every screen audit shows the chip in nav. Data source: `auth.claims.branch_id` (Owner) or `user.current_branch_id` (Trainer multi-brand). Inspectable in ≤ 200 ms of eye-travel from any screen.

**Operational principle:**

6. **Offline-first means the orb doesn't lie.** Truth source = `(ws.readyState, offline_queue.depth, last_server_ack_at)` tuple. If any of these indicates degraded state, the orb states it. Reconcile divergence must be visible — never silently "ate" a write. Trust in the orb's state is trust in the app.

---

## Desired Emotional Response

### Primary Emotional Goals

Three feelings Vis is built to produce. Every screen should serve at least one.

1. **Quiet competence.** The user feels capable, in control, like the equipment in front of them is theirs to command. Not "performing." Not "celebrated." Earned, owned. Vis dignifies the work, never flatters the worker.
2. **Coached presence (PT-attached) + Solitary focus (solo).** PT-attached users feel accompanied without being managed; solo users feel focused without being lonely. Vis carries both modes — the trainer cursor is information, not monitoring; the solo streak heatmap is recognition, not gamification.
3. **Earned trust in the tool.** The orb doesn't lie. The data doesn't disappear. The AI doesn't condescend. Trust compounds with usage — every successful reconcile, every PR confirmation, every offline survival adds to the bank.

### Emotional Journey Mapping

Fourteen stages across the user journey. Each stage names the persona most exposed (functional role; internal codename in brackets) and the desired feeling at that beat.

| Stage | Persona | Desired feeling |
|---|---|---|
| Discovery / sign-up | All | Intrigue + low-stakes ("free, no card"). Not "another fitness app" fatigue. |
| Onboarding Questionnaire | All | Brisk, dignified. Not interrogated. Skippable advanced fields signal respect. |
| First trainer browse (gym path) | Gym Client [Tarly] | Confidence in choice — three verified rating dimensions, certs, language, response time. Not overwhelmed. |
| First template pick (solo path) | Solo Client [Greyjoy] | "I know what I'm doing" — respect for prior knowledge. Not held-by-hand. |
| First Set logged | All | Magic / "huh" — the Logger gesture feels like the equipment. The first "this is different" beat. |
| First Co-Edit moment | Gym Client + Trainer [Tarly + Tarth] | Connected — trainer's edit lands on screen with provenance. Both feel *in sync*. Not surveilled, not abandoned. |
| Failure / form break (mid-rep) | Gym Client [Tarly] | Held — trainer flips assistance flag in real time without breaking flow. Dignity preserved. |
| PR detection + share | All | Earned — cinematic restraint. User wants to share because the moment is worth it, not because of a hook. |
| Offline drop | All | Calm — orb says "queued," not "broken." Trust intact. |
| Return D7 | All | Recognised — Vis remembers last-Set, weekly volume, recovery state. Streak heatmap visible. Feels resumed, not reset. |
| Plan-block expiry (T-2 day) | Trainer [Tarth] | Anticipated — FCM lands before client's last Session in block. Trainer feels professional, prepared. |
| Owner morning glance | Gym Owner [Lannister] | Confident overview — revenue + branches + leaderboard + churn-risk in one screen. The chain runs without daily presence. |
| Trainer-AI override (rejection) | Trainer [Tarth] | Heard — long-press override doesn't argue, doesn't re-prompt. AI learns. Trainer stays in command. |
| Skipped Set (abandonment) | Gym Client [Tarly] | Non-judged — "Why?" reason capture is offered, not demanded. No guilt UX. |
| PT relationship ending | Gym Client + Trainer [Tarly + Tarth] | Honoured — prior trainer keeps read-only history; client carries plan + AI history forward with consent. Dignified close. |

### Micro-Emotions

**Engineer for:**

- **Confidence over confusion.** Every screen answers "what next?" in ≤ 200 ms of scan. No empty-state fog.
- **Trust over skepticism.** Orb truth principle. Every state has a named signal. Never silently swallow a write.
- **Accomplishment over frustration.** Failure modes are designed (offline, AI cold path, conflict). Set-save shows committed state.
- **Dignity over shame.** Skipped Sets, missed PRs, low-adherence weeks are never moralised. Plain data, peach-tinted shadows.
- **Belonging (gym path) vs Solitude (solo path), each respected.** PT-attached: Connection Orb visible. Solo: streak heatmap + curated template marketplace.
- **Restraint over stimulation.** Cinematic amber, not gaming neon. PR cinematic ≤ 1.5 s. No infinite feeds.

**Actively avoid:**

- **Notification fatigue** — FCM cadence tuned (T-2 + T-0 only on expiry; no daily nags).
- **Gamification ick** — no streak-loss anxiety, no XP bars, no leaderboard shaming.
- **Surveillance feel (PT-attached)** — trainer cursor presence is information, not monitoring. Debounced cursor render.
- **Tech-debt visibility** — offline state shown calmly, never as a red alert.
- **Performative celebration** — no celebration animation fires without a server-confirmed event (Core Principle 3).
- **Beginner shaming** — a beginner's first Sets are dignified by the same UI as an advanced lifter's PR (load + reps + RIR). No "newbie" branding.

### Design Implications

Eight emotion-to-UX mappings. Each is a specific design move, not a vibe.

| Emotion | UX choice |
|---|---|
| Quiet competence | Equipment-Aware Logger as gesture vocabulary; minimal chrome on Set rows; cinematic typography (Geist) with restraint. |
| Earned trust | Orb states tied to named signals (Core Principle 4); Set-row provenance avatar + timestamp; offline state explicit; idempotent writes by client UUID. |
| Coached presence | Connection Orb in Active Session header; prescribed-vs-actual divergence tint; trainer cursor presence with debounce so it doesn't feel jittery / monitored. |
| Solitary focus | Solo home gives a single Today's Plan card; no live elements except Recovery Orb; AI ghost is opt-in tier-2 v1; no fake-social hooks. |
| Dignity | Skip-Set reason capture optional, never required; no guilt copy on missed Sessions; ACWR alert phrased "consider deload," never "you're overtraining." |
| Earned celebration | PR cinematic ≤ 1.5 s; dismissible; never on optimistic write; restraint over confetti; watermark default-on but togglable. |
| Recognition on return | Home greets with last-Set summary; weekly streak heatmap; Recovery Orb state delta vs prior week; no day-zero reset feel after first Session. |
| Confidence in choice (trainer browse) | Three independent rating dimensions (Experience / Feedback / Progress) instead of one star score; verified cert badge prominent; response-time stat shown. |

### Emotional Design Principles

Five guiding rules. Each is testable in design crit.

1. **Earned over celebrated.** No animation, badge, or moment fires without something the user did + the server confirmed. (Already in Core Experience Principle 3; restated here as emotional posture.)
2. **Calm under degradation.** Offline, network drop, AI cold path, conflict — every degraded state has a calm, named, visible UX. **Anxiety is a bug.**
3. **Dignity in failure.** Missed Set, skipped Session, low adherence, AI override — never moralised. Plain data + neutral phrasing.
4. **Presence without surveillance.** PT-attached users feel accompanied; trainer cursor is information, not monitoring. Solo users feel focused; no fake-social hooks.
5. **Recognition over reset.** Returning users see continuity (last-Set, weekly heatmap, recovery state). New-day-zero feel is forbidden after the first Session.

---

## UX Pattern Analysis & Inspiration

### Inspiring Products Analysis

Five inspiration sources, ordered by transfer fit. Vis ships a **fresh new UI** — these are pattern donors, never visual templates.

**1. Motra (ex-Train Fitness) — training UX, strongest fit.**
*Learn:* compact Set row with inline RIR/RPE (direct adoption for Active Session view), recovery-aware routine generation (shades trainer's recommendation, never auto-rewrites v1), exercise-history-first home (the lift is the entity), lift-specific AI prompts.
*Leave behind:* Apple-Watch-only logging (wrist motion can't detect leg lifts reliably), iOS + Watch lock-in.

**2. Strong app — lifting-logger gold standard.**
*Learn:* compact set rows, rest-timer-as-ambient (not a modal), one-handed thumb operation during a working set, minimal numeric typing path.
*Leave behind:* solo-only framing, no co-edit, numeric-keypad-first input — Vis replaces with gesture-first via Equipment-Aware Logger.

**3. Apple Health — graph + chart interaction.**
*Learn:* line-chart rendering style for Body Measurements + e1RM + ACWR sparkline; segmented time-range control (Day / Week / Month / 6M / Year); colour-coded health-category chips → muscle-group recovery chips; pinch-to-zoom timeline; tap-a-point-for-callout; swipe between metrics on same axis.
*Leave behind:* generic Apple semantic palette, forms-heavy empty states, sterile white chrome.

**4. Linear / Figma multiplayer — real-time presence + workspace context.**
*Learn:* real-time cursor presence model with debounce (input to Connection Orb tension stem); workspace switcher pattern (input to branch-scope chip in nav); Linear's command-K speed + restraint as overall product-polish anchor.
*Leave behind:* keyboard-shortcut-heavy interaction (Vis is touch-first on mobile), engineering-tool aesthetic.

**5. Bevel — AI confidence pattern only (reserved for v1.1).**
*Learn:* AI suggestion confidence display + dismissal grammar — informs ghost-overlay confidence treatment when shipped in v1.1.
*Leave behind:* holistic dashboard, biological-age metric, full chat surface, generalist health framing. (Bevel's broader UX does not map to Vis; this is a narrow pattern borrow.)

*Note: the Vis prototype + wordmark + orb recipe are not on this inspiration list — they are the **locked-in visual identity** captured below in §Strategy and in CLAUDE.md brand palette.*

### Transferable UX Patterns

Eight FR-anchored patterns. Each row names source, Vis FR served, and the SM it moves. Rows without an FR + SM mapping were cut from the prior draft.

| Bucket | Pattern | Source | Vis FR / SM |
|---|---|---|---|
| Navigation | Bottom-tab nav (4–5 tabs, thumb-arc respect) | Apple Health, Bevel | FR-43 access gate / Client home / Trainer home; supports SM-6 onboarding completion |
| Navigation | Workspace switcher → branch-scope chip in nav header | Linear (pattern donor) | FR-25 + RBAC; supports SM-3 + SM-8 (multi-role identity correctness) |
| Interaction | Compact Set row with inline RIR/RPE (no separate sheet) | Motra, Strong | FR-1 + FR-3 + FR-4; supports SM-3 + SM-C6 (set-save p95) |
| Interaction | Real-time presence cursor (debounced) → Connection Orb tension stem | Linear / Figma multiplayer | FR-1 Co-Edit; supports SM-3 (≥ 25% Co-Edit by D90) |
| Interaction | Recovery state shades trainer's next-session intensity recommendation (no auto-rewrite v1) | Motra | FR-12 + FR-35 + FR-37 ACWR alert; supports SM-7 |
| Interaction | Rest-timer-as-ambient (visual progress + haptic, no modal) | Strong | FR-7 Rest Timer; supports SM-C6 set-save flow |
| Visual | Apple Health chart interactivity (pinch-zoom timeline, tap-for-callout, swipe between metrics on same axis) | Apple Health | FR-38 Progress Tab; supports SM-1 + SM-7 |
| Visual | Sparkline-on-card for at-a-glance KPI | Apple Health | FR-22 + FR-38 + Owner Dashboard; supports SM-7 + SM-8 |

*Reserved for v1.1: Bevel AI suggestion confidence display — token slot `--ai-confidence-low/med/high` defined now; component lands when calibrated probability + dismissal feedback loop are scoped.*

### Anti-Patterns to Avoid

Six named anti-patterns. Each cites the principle it violates.

1. **MyFitnessPal-style data interrogation** — long forms, judgmental copy, anxiety-inducing macro pies. *Violates Emotional Principle 3 (Dignity in failure).* Implication: no daily calorie tracking v1; macro plan stays trainer-authored + client read-only.
2. **Gym-management dense-table ops UI (Mindbody / Glofox)** — corporate-blue chrome, dated tables, spreadsheet feel. *Violates Emotional Principle 4 (Presence without surveillance — dense ops UI is surveillance posture) + Principle 1 (Earned over celebrated — admin chrome over member story).* Implication: Admin Web tables stay solid (per Liquid Glass "content leads") on warm cream bg with amber accents and generous spacing — not corporate white-on-grey.
3. **Hyper-bro lifting community aesthetic (Bodyspace, /r/bodybuilding-style)** — anatomy line drawings, dark machismo, neon-yellow accents, intimidating gym-bro copy. *Violates Emotional Principle 4 (Presence without surveillance).* Implication: copy stays gender-neutral, low-key competent; no anatomy illustrations; PR cards restrained.
4. **Fitbod-style gamified celebration** — confetti PR animations, XP bars, "level up!" badges, streak-loss anxiety. *Violates Emotional Principle 1 (Earned over celebrated) + Core Experience Principle 3 (celebration only post-server-confirm).* Implication: PR cinematic ≤ 1.5 s, dismissible; no XP; streak heatmap renders as data, not scolding.
5. **Strava-style social feed + leaderboards** — public activity stream, follower counts, "kudos" performative engagement. *Violates Emotional Principle 4 (Presence without surveillance).* Vis is private 1:1 coaching, not feed. Implication: PR-Card share is one-way to *external* platforms (Instagram / WhatsApp / X), never to a Vis internal feed. SM-5 measures share rate; SM-C10 watches no-share — measuring the moment, not building a feed.
6. **Whoop-style metric overload on home** — 12 KPI tiles, sparklines everywhere, "recovery score 47%" alarm aesthetic. *Violates Emotional Principle 2 (Calm under degradation) — the home becomes a degraded state by being noisy.* Implication: Client home leads with one number that matters (today's Plan card), with supporting tiles for Recovery Orb + Today's Stats; never grid-of-12. Same discipline on Trainer Today + Owner Dashboard.

### Design Inspiration Strategy

**Adopt:**

- Motra compact Set row with inline RIR/RPE → Active Session view (FR-1 + FR-3).
- Strong rest-timer-as-ambient → no modal interruption between sets (FR-7).
- Apple Health chart interactivity → Progress Tab (FR-38).
- Apple Health sparkline-on-card → Trainer Today + Owner Dashboard KPI cards.
- Linear / Figma multiplayer cursor-presence model → Connection Orb tension stem visual (FR-1).
- Linear workspace switcher pattern → branch-scope chip in nav (multi-role / multi-brand / multi-branch identity, per RBAC).
- Linear's restraint + speed product-polish → cross-app quality bar.
- Apple iOS 26 Liquid Glass material on nav + hero only (per original spec §17.4).

**Adapt:**

- Motra recovery-aware programming → Vis shades trainer's next-session intensity recommendation; never auto-rewrites v1. Requires `recovery_score` (0–100) — compute client-side from HealthKit / Health Connect HRV + sleep already in scope (read-only, on-device). If client-side compute proves too noisy in beta, add `GET /recovery` endpoint as a follow-up.
- Bevel AI suggestion confidence pattern → token slots reserved (`--ai-confidence-low/med/high`); component lands v1.1 (requires backend calibrated probability + dismissal feedback loop; no FR yet).
- Apple Health time-range segmented control → adapted with Vis amber accent + warm chrome.

**Avoid:** the six anti-patterns above. Also avoid direct visual mimicry of Bevel / Apple Health / Motra / Strong / Linear — borrow the moves, not the looks.

**Locked-in visual identity (Vis-original, not "inspiration"):**

- Orb-as-organism with 5 tension states (per Core Experience Principle 4) and gradient recipe (radial peach top-highlight + linear amber body + smoked-glass overlay).
- Vis wordmark — orb replaces the i-dot; sharp 45° v wedge; tight s terminals.
- Geist + Geist Mono typography (display + UI body + tabular numeric).
- Warm ivory bg (`#F9F6F0` light; `#050505` dark) — never pure white.
- Peach-tinted shadows — never neutral black.

### Design System Reservation — User-Themable Accent (deferred to v1.1)

The user-themable accent feature ships in **v1.1**. The supporting **two-layer token architecture ships v1** so v1.1 lands without re-architecture. v1 surfaces Tangerine only; no Profile → Appearance → Accent UI in v1.

**Two-layer token system:**

| Layer | Tokens | Consumed by |
|---|---|---|
| A — Raw | `--vis-amber-primary`, `--vis-amber-deep`, `--vis-amber-ember`, `--vis-amber-soft`, `--vis-amber-highlight`, `--vis-amber-shadow` (v1); `--preset-ember-500`, `--preset-rust-500`, `--preset-slate-500` (v1.1) | Never consumed by components |
| B — Semantic (themable) | `--accent`, `--accent-on`, `--accent-muted`, `--accent-emphasis` | All themable surfaces — CTAs, focus rings, selected-state tints, chart accents, ghost-overlay color |
| B — Brand-locked | `--brand-locked-primary`, `--brand-locked-deep`, `--brand-locked-shadow` | Orb (all 5 tension states), Vis wordmark, PR-card watermark — never overridden by accent preset |

**Architectural constraints (not stylistic):**

- Accent applies to non-text affordances only — fills, icons ≥ 24 px, chart strokes, focus rings.
- Text on accent uses `--accent-on`, precomputed per preset, validated at token-build time. CI fails if any preset's `(--accent, --accent-on)` pair falls below WCAG 2.1 AA 4.5:1 contrast.
- Shared `tokens.ts` workspace package consumed by RN ThemeProvider + Angular CSS variables — single source of truth across all three apps.

**Preset set (v1.1, when shipped):** 4 presets — **Tangerine** (default, same as current `--vis-amber-primary`), **Ember red**, **Rust gold**, **Slate teal**. Sage green + Dusk plum dropped (270° hue spread conflicts with brand-locked amber orb). Future presets behind RFC.

**v1 ships with Tangerine only.** Profile → Appearance → Accent surface lands v1.1. Token plumbing is the v1 deliverable; the preset UI is not.

---

## Design System Foundation

### Design System Choice

Vis uses a **two-layer design system**: a custom Vis brand layer on top of a themeable primitives foundation (Path B from the design-system trade-off).

| Layer | What it is | Vis decision |
|---|---|---|
| Brand layer (custom) | Every Vis-distinctive visual element: orb (5 tension states), Vis wordmark, Equipment-Aware Logger (6 variants), Connection Orb, Recovery Orb, PR Moment composer, Branch-Scope Chip, Active Session Set row, Liquid Glass nav + hero, and the 6 components added during party-mode review | Built from scratch; no UI library substitute |
| Primitives layer (library-backed) | Theme switching, accessibility, focus management, dialogs, overlays, drag-and-drop, gesture handling, animation, charting | Library-backed |

**Primitives layer stack:**

| Surface | Library | Role |
|---|---|---|
| React Native (Client + Trainer) | **Tamagui** (with `@tamagui/babel-plugin` `optimize: true`) | Theme system maps 1-to-1 to the Vis two-layer token architecture; compile-time style flattening for cold-start budget |
| RN gesture handling | **React Native Gesture Handler** | Equipment-Aware Logger gestures (drag cable pin, snap a plate, slide dumbbells) |
| RN animation | **Reanimated 3** | 60 fps on native UI thread — orb tension states, Connection Orb pulsing, PR Moment ≤ 1.5 s |
| RN blur (Liquid Glass) | **@react-native-community/blur** | UIVisualEffectView on iOS, RenderEffect on Android 12+. Android < 12 fallback = gradient overlay. Scoped to nav + Active Session hero only |
| RN gradient morphing | **react-native-skia** | Orb-hero only (+2 MB bundle acceptable). Used where shape distortion / gradient interpolation matter (Connection Orb tension stem, Recovery Orb segments) |
| Angular (Admin Web) | **Tailwind CSS** | Utility-class styling driven by shared `tokens.css` |
| Angular accessibility | **Angular CDK** | Headless a11y primitives — focus trap, overlay, live region, drag-drop |
| Icons (all three apps) | **Lucide** (RN + Angular ports) | Restrained line-style icon set |
| Fonts | **@fontsource/geist** + **@fontsource/geist-mono** | Self-hosted; no external CDN |
| Charts | **Victory Native** (RN) + **Apache ECharts** (Angular) | Themable; consume Vis tokens |

**Component generation workflow:** 21st-dev/magic MCP scaffolds new components from intent (per CLAUDE.md mandate); output is adapted to Vis tokens via the 5-step gated checklist in §Implementation.

**Expo dev client required** for both RN apps (not Expo Go) — Reanimated, Gesture Handler, blur, and Skia are all native modules. EAS Build pipeline supports this.

### Rationale for Selection

1. **Token-system fit.** The two-layer token architecture (Layer A raw + brand-locked + future presets; Layer B semantic `--accent` / `--brand-locked-*`) maps directly to Tamagui's `createTokens` + `createTheme` API. Light + dark + v1.1 user-themable accent presets are one config change in Tamagui — not a per-component override pass.

2. **WCAG 2.1 AA mostly free.** Angular CDK ships focus traps, ARIA live regions, overlay positioning, drag-and-drop with screen-reader announcements. Tamagui ships ARIA roles + correct semantic primitives on React Native. Hand-rolling these would push WCAG compliance work 8+ weeks deeper and risk shipping with a11y gaps.

3. **Cinematic amber survives.** Path B doesn't change the brand layer — it removes the cost of rebuilding generic primitives underneath. The orb / Equipment-Aware Logger / PR Moment / Connection Orb stay custom-built.

4. **Skill match.** Stays in React + Angular. No new language to learn.

5. **Watch / Wear OS readiness (v2 path).** Apple Watch (watchOS) and Wear OS will be separate codebases regardless of phone framework — Apple requires SwiftUI for Watch; Wear OS requires Kotlin Compose. Sharing across all four targets happens via the backend API + shared TypeScript types, not via a cross-platform UI library.

6. **Theme-swap performance.** Tamagui compiles styles at build time. Switching presets in v1.1 (Tangerine → Ember → Rust → Slate) costs a single React re-render of the theme subtree, not a full restyle pass.

### Implementation Approach

**Phase 0 deliverables (must precede any feature work):**

- **Monorepo with `packages/tokens` workspace package.** pnpm workspace. Exports three artifacts from one source: `tokens.ts` (RN / Tamagui input), `tokens.css` (Angular CSS variables), `tokens.json` (Style Dictionary input for future Figma sync). A `build-tokens.ts` script runs in CI on every PR touching `packages/tokens/**`. Drift across apps becomes impossible.
- **Realtime transport decision (WebSocket vs polling) — closed in Phase 0.** Currently unresolved in CLAUDE.md. SM-3 Co-Edit gate requires this decided before Phase 1. Recommend WebSocket with 10 s polling fallback per PRD NFR §7.2.
- **Feature flag system wired Phase 0.** SM-1 / SM-3 / SM-6 measurement needs A/B variants from day one.
- **WCAG contrast CI.** `pa11y-ci` on Angular Admin Web + a Node script using the `wcag-contrast` library on RN tokens. GitHub Action on PR (not pre-commit — too slow). Input: a static `tokens.contrast-matrix.json`. Fail build if any (text, bg) pair drops below 4.5:1.
- **ESLint guardrails.** `no-restricted-syntax` rule blocking hex literals in `/components/**`. Brand-lock allowlist file enumerating the orb / wordmark / PR-watermark components permitted to import `--vis-amber-*` directly. Everything else imports `--accent` / `--brand-locked-*` only.
- **6 gesture maps documented Phase 0.** One per equipment type (cable pin-stack, barbell plate-snap, dumbbell slider, machine pin-stack, bodyweight silhouette, kettlebell discrete picker, band tier picker). Documented *before* Phase 2 build prevents Logger gesture-grammar drift.

**Phase 0.5 — bridging week between Phase 0 close and Phase 1 kickoff:**

- Tamagui setup with `@tamagui/babel-plugin` `optimize: true`. Cold-start delta measured on Pixel 6a / Galaxy A54 with plugin off vs on; verify ≥ 120 ms improvement (else misconfigured).
- One throwaway practice screen exercising token plumbing end-to-end across RN + Angular.
- Expo dev client builds verified on iOS + Android (Reanimated, Gesture Handler, blur, Skia all installed and confirmed).
- Angular CDK wired; Tailwind config scopes `preflight` reset away from `.cdk-overlay-container`.
- Lucide ports installed both sides.
- Geist + Geist Mono self-hosted via `@fontsource`.

**Per-feature workflow (every PR after Phase 0.5):**

1. Designer or 21st-dev/magic MCP `21st_magic_component_inspiration` generates a starting reference.
2. `21st_magic_component_builder` (new) or `21st_magic_component_refiner` (existing) produces a JSX / TSX scaffold.
3. **"Adapt to Vis tokens" — 5-step gated checklist, all required for merge:**
   1. Replace every hex literal with `var(--gc-*)` (Angular) or a Tamagui theme key (RN).
   2. Replace the shadow stack with the 3-layer amber falloff from CLAUDE.md (`0 0 20px rgba(255,106,0,0.35)`, `0 0 60px rgba(255,106,0,0.18)`, `0 0 120px rgba(255,122,26,0.12)`).
   3. Verify against `docs/brand-ref/reference_light.png` + `reference_dark.png`.
   4. WCAG contrast CI green.
   5. Light + dark screenshot diff approved.

**Component inventory (custom-built, on top of Path B primitives):**

*Core brand surfaces:*
- Orb (5 tension states + animation engine; backend signals named per Core Experience Principle 4)
- Connection Orb (live trainer ↔ client tension stem; debounced cursor presence)
- Recovery Orb (per-muscle readiness colour-coded chips)
- Vis wordmark + brand logo treatments
- Liquid Glass nav + Active Session hero (using `@react-native-community/blur`; Android < 12 = gradient overlay)

*Equipment-Aware Logger family (6 variants, FR-8):*
- Cable pin-stack (drag visual pin to slab position)
- Barbell plate-snap (auto-sum to total)
- Dumbbell number-line slider
- Machine pin-stack
- Bodyweight silhouette + optional weight-vest field
- Kettlebell discrete picker + band tier picker

*Active Session:*
- Set row (prescribed-vs-actual divergence tint, warm-up flag, failure intent, per-rep assistance)
- **Presence Avatar Stack** — overlapping dots on the Set row showing who's currently editing live (FR-1 + FR-25)
- **Conflict Toast** — non-modal last-write-wins notification when a remote write overwrites yours
- **Ghost Set Row** — AI-suggested set with accept / dismiss gesture (FR-12 + FR-14); distinct interaction model from the standard Set row
- Branch-Scope Chip — multi-role identity switcher in nav

*Alerts + recovery:*
- **Risk Banner** — ACWR > 1.5 alert with primary action (FR-37)
- **Session Recovery Sheet** — abandonment flow ("you left 47 min ago, resume or discard?") with partial-set preview

*Onboarding + cards:*
- Onboarding Questionnaire — shared component with path-aware shell (FR-27)
- Today's Plan card (Client home)
- Trainer Today schedule card + client avatar row
- Owner KPI cards (sparkline + drill-down)
- Branch dashboard tables (solid surfaces per "content leads" rule)
- Audit-log query interface

*Sharing + states:*
- PR Card composer (square / 9:16 / 4:5 templates with Vis watermark)
- **Empty States set** — per surface (empty logger, empty PR feed, empty branch dashboard, empty client list, empty trainer roster)

### Customization Strategy

**Brand layer customization is total** — every Vis-distinctive component is custom code; no library override needed.

**Primitives layer customization is theme-driven** — Tamagui themes + Tailwind config + Angular CSS variables all read from one shared `tokens.ts` workspace package. Changing a token (e.g. `--accent`) updates all three apps from one place.

**v1.1 accent presets** ship via Tamagui's theme switcher (`<Theme name="ember">`) — swaps the entire Layer B semantic token bindings. Four presets (Tangerine default, Ember, Rust, Slate). No component changes required.

**Component-level overrides** are reserved for edge cases — if a Tamagui or CDK primitive can't be reasonably styled via token, the alternative is replacing that primitive with a custom build, not forking the library.

**Anti-pattern guardrails:**

- Components must not reach into raw Layer A tokens (`--vis-amber-primary`) for themable surfaces — only `--accent` / `--brand-locked-*` are valid. Enforced by ESLint `no-restricted-syntax`.
- Brand-lock allowlist enumerates specific components (orb, wordmark, PR-watermark) permitted to import `--vis-amber-*` directly.
- Liquid Glass blur scoped to nav + Active Session hero only — never on tables, lists, or dense data surfaces (per original spec §17.4 "content leads").

**Open items for v1.1 spike:**

- **Voice fallback for trainer in-session** — RN voice library landscape is thin. Needs a native module wrapper spike before v1.1. Budget ~1 week of architecture work.
- **AI suggestion confidence display** (Bevel-inspired) — token slots reserved (`--ai-confidence-low/med/high`); component lands when backend calibrated probability + dismissal feedback loop are scoped (no FR yet).

---

## Defining Core Experience

### Defining Experience

The interaction Vis is built around — the one we'd say to a friend to explain Vis in one sentence:

> **"Two phones, three feet apart, both editing the same Set live. The trainer drags the cable pin to 40 kg; the client sees it update before they grip the handle. The client lifts; either of them taps Done; the other side sees the green check land within 500 ms. No typing. The Set knows who wrote it."**

This moment does not exist in Fitbod, Hevy, Strong, Motra, Bevel, Trainerize, TrueCoach, or Mariana Tek. Every competitor either makes both parties wait for an async update (PT-SaaS cluster) or has only one party using the app at a time (solo loggers + gym ops). Vis collapses the gap.

**The three irreducible parts of the moment:**

1. **The Logger gesture.** Drag the cable pin. Snap a plate. Slide the dumbbell rack. No typing. Mimics the equipment in front of the user.
2. **The Provenance line.** The Set row knows which actor (trainer or client) wrote each field, and when. Small avatar + timestamp render on the row within 500 ms of every commit.
3. **The Connection Orb pulse.** A tension stem between two amber spheres in the Active Session header pulses on every write from either side. Makes the WebSocket-invisible-thing visible.

If we nail those three together, every other Vis surface follows: solo sessions inherit the same Logger + Provenance (just without a partner); Owner dashboards inherit the same tokens + Recovery-Orb language; PR Moment inherits the same restraint.

### User Mental Model

Users do **not** think "I'm logging a Set." They think "I'm doing the lift." Vis must respect that.

**Gym Client beginner [House Tarly] — Samwell:**
*"I walked up to the cable. My trainer said 40 kg × 12. I want to do the work, not configure software."*
*Expects:* equipment-shaped input. *Confused by:* number pads, dropdowns, modal sheets between sets.

**Gym Trainer [House Tarth] — Brienne:**
*"I'm coaching 8 sessions today. I prescribe, watch the form, count to failure, write the next set. I have ~3 seconds between reps."*
*Expects:* one-tap commit, instant ghost suggestion for next set, hands-free voice fallback when spotting. *Confused by:* data validation errors mid-rep.

**Solo Client strength athlete [House Greyjoy] — Theon:**
*"I'm running a 5×3 block. I know my percentages. The app should remember what I lifted last week and let me bump load with one gesture."*
*Expects:* prefill cascade (last session → AI bootstrap → empty), one-tap "same as last set." *Confused by:* AI suggestions that don't match his program.

**What users love and hate about existing solutions:**

- **Hevy / Strong (numeric loggers):** love the speed, hate typing weight numbers with chalky / sweaty hands.
- **Fitbod / Boostcamp (AI auto-progression):** love "tell me what to do next," hate that the AI overrides their gut.
- **Trainerize / TrueCoach (remote PT):** love async messaging, hate that the trainer isn't *with* them during the session.
- **Apple Watch tap-to-log:** love hands-free, hate that leg lifts don't auto-detect reliably.

**Shortcuts users currently invent (and Vis replaces):**

- Voice memos to themselves ("squat 200 × 5") → Vis voice fallback honors this (v1.1 spike).
- Group-chat photos of a notebook page → Vis replaces with the Active Session view.
- Last-session memory held in the user's head → Vis bakes this into the prefill cascade.

### Success Criteria

The interaction is successful when users *describe* it differently from any prior app they've used. Eight measurable thresholds + three qualitative tests.

| Indicator | How we measure |
|---|---|
| Set save → server commit `p95 ≤ 250 ms` (online) | NFR §7.5 · SM-C6 |
| Set save → local-queued ack `≤ 100 ms` (offline) | NFR §7.5 |
| Logger first-contentful render `≤ 200 ms` after tap | NFR §7.5 |
| Other side's write appears on screen `≤ 500 ms` after commit | WebSocket round-trip target tied to Connection-Orb pulse tolerance |
| Zero keystroke events in the happy path | Core Experience Principle 1; CI lint can probabilistically check |
| Set-row provenance visible (avatar + timestamp) when ≥ 1 write within 30 s | Visual audit + automated screenshot test |
| Connection Orb pulse animation completes `< 800 ms` after a remote write | Animation-timing test |
| Trainer-AI accept gesture lands in ≤ 1 tap (long-press for override) | Interaction tap-count audit |

**Qualitative "we got it" moments:**

- A PT-attached client says: *"It felt like she was logging with me."* — Co-edit moment lands; SM-3 fires.
- A solo client says: *"I never had to type anything."* — Logger gesture moat holds; SM-1 + SM-C8 healthy.
- A trainer says: *"I authored the next set with one tap during a rest period."* — Ghost overlay accept works.

### Novel UX Patterns

**Established — adopt unchanged:**

- Bottom-tab nav, segmented time-range controls, sparkline-on-card (Apple Health donor).
- Rest-timer-as-ambient (Strong donor).
- Workspace switcher → branch-scope chip (Linear donor).
- Modal-free single-row Set editing (Motra donor).
- Native share-sheet for PR Card.

**Novel — Vis has to teach:**

- **Equipment-Aware Logger gesture vocabulary.** Six interactions, one design language. Industry-first. *Teaching:* first-time logger tap shows a 2-second animated hint ("drag the pin to your slab"); dismissed after first successful gesture. No persistent tutorial overlay.
- **Connection Orb tension stem.** A live visual link between two avatars in the Active Session header. Pulses on each write from either side. *Teaching:* appears with an 800 ms intro animation on first Co-Edit moment; thereafter ambient.
- **Multi-role identity switcher (branch-scope chip).** Trainer multi-brand or owner cross-branch. *Teaching:* chip is always visible in nav; tapping it on first run shows a "you have access to N brands — pick one" sheet. After first switch, becomes a tap-to-toggle.
- **Ghost Set Row (AI-suggested set).** Greyed-out row beneath the live Set row; tap accept, long-press override. *Teaching:* first AI suggestion fires an inline coachmark ("this is an AI suggestion — tap to accept, long-press to override"). Coachmark fades after first interaction.

**Combined-in-new-ways:**

The Equipment-Aware Logger × prescribed-vs-actual × per-rep assistance × failure intent — all on one Set row. Each component is familiar in isolation; the combined density is novel. Visual hierarchy + divergence tint keep it legible.

### Experience Mechanics

The Co-edited Set logging interaction, step by step.

**1. Initiation.**

Trainer (or solo client) opens Active Session from Today's schedule. Logger pre-renders within 200 ms with the prefill value (cascade: AI ghost → trainer prescribed → last-session → AI bootstrap → empty). Client (PT-attached) opens Active Session and lands on the same Session via WebSocket connect. Connection Orb appears in the header with a brief intro animation; both avatars visible; tension stem at rest.

**2. Interaction.**

- *Trainer gesture:* drags the cable pin to slab 8 (40 kg). Action commits optimistically; tension stem pulses on the trainer's side; client's screen receives the write via WebSocket within 500 ms and the pin animates to position.
- *Client lifts.* Either party taps Done. If both tap within 50 ms, server-stamped monotonic order resolves; the later write wins; loser's row shows a one-line Conflict Toast.
- *Provenance.* The Set row updates with `actor_id` (trainer or client) avatar + timestamp. Avatar persists for 30 s after last write, then fades.
- *Auto-rest timer.* Triggers on Set save. Compound = 2:30; isolation = 1:00; per-(client × exercise) override available. Haptic cue at 10 s remaining.

**3. Feedback.**

- *Success:* Set row gains a green check on commit. Connection Orb pulses once per write. Tension stem brightens slightly when both parties have edited the same Set within 60 s.
- *Network drop:* Logger row shows a "queued" badge (distinct from "committed"). Connection Orb state transitions to "Disconnected / Stale" with calm peach tint. No alarms.
- *Mistake (mis-logged set):* long-press on Set row → Edit / Mark warm-up / Mark abandoned. Audit log entry created.
- *Conflict:* non-modal Conflict Toast — "Trainer overwrote your value 47.5 → 50. Undo?" Tappable revert; ignored = the trainer's write stands.

**4. Completion.**

Last Set saved → Session can be marked complete by either party. Marking complete → Progression Index recomputes server-side → both sides see the result (and the e1RM tick on Progress Tab on next view). If a PR is detected: PR Moment screen fires asynchronously when detection lands; cinematic ≤ 1.5 s; composer ready for share. If the Session was abandoned (last activity > 30 min ago without an explicit End): Session Recovery Sheet appears on next app open with partial-set preview — Resume or Discard.

**The interaction loop is the product.** Everything else — Owner dashboards, audit logs, PR Card composer, Onboarding Questionnaire — is supporting infrastructure for this one moment.

---

## Visual Design Foundation

### Color System

Two-layer token architecture (locked in step-05 / step-06). Layer A holds raw values (never consumed directly by components); Layer B holds semantic names (what components reference).

**Layer A — Raw amber palette (canonical, from CLAUDE.md):**

| Token | Dark hex | Light hex | Use |
|---|---|---|---|
| `--vis-amber-primary` | `#FF7A1A` | `#E06313` | Main orb fill + brand CTA. Light deeper for AA text. |
| `--vis-amber-deep` | `#FF5E00` | `#FF5500` | Inner glow / hotspots / liquid base |
| `--vis-amber-ember` | `#D9590B` | `#F26200` | Orb liquid floor / dome dark. Vibrant tangerine, never brown. |
| `--vis-amber-soft` | `#FF9B4A` | `#FFA366` | Gradient transitions |
| `--vis-amber-highlight` | `#FFC27A` | `#FFE3C2` | Reflections / specular / glare |
| `--vis-amber-shadow` | `#A94400` | `#C45100` | Depth + lower orb shading |
| `--vis-amber-glow` | `#FF6A00` | `#FF6A00` | Outer bloom — low opacity on light bg |
| `--vis-cream` | `#1A1310` | `#FFF3E8` | Glass tint / page tint |
| `--vis-smoke` | `#2A1812` | `#EFE1D5` | Upper orb dark glass overlay |
| `--gc-bg` | `#050505` | `#F9F6F0` | Main application background — never pure white |
| `--gc-bg-elevated` | `#0E0E0E` | `#FFFFFF` | Card surface. Light cards may use pure `#FFFFFF` (only exception to no-white rule). |

**Layer B — Semantic tokens (what components consume):**

| Token | Maps to | Use |
|---|---|---|
| `--accent` | `--vis-amber-primary` (v1) / active preset (v1.1) | CTAs, focus rings, selected-state tints, ghost-overlay color, chart accents |
| `--accent-on` | precomputed per preset for ≥ 4.5:1 contrast | Text on accent surface |
| `--accent-muted` | `rgba(--accent, 0.15)` | Hover tints, secondary chip backgrounds |
| `--accent-emphasis` | `--vis-amber-deep` (v1) / preset emphasis (v1.1) | Hero CTA, primary action emphasis |
| `--brand-locked-primary` | `--vis-amber-primary` (immutable) | Orb fill, Vis wordmark, PR-card watermark — never themable |
| `--brand-locked-deep` | `--vis-amber-deep` (immutable) | Orb deep emissive |
| `--brand-locked-shadow` | `--vis-amber-shadow` (immutable) | Orb cast shadow |

**v1.1 preset palette (new in this step):**

| Preset | `--accent` light | `--accent` dark | `--accent-on` light | `--accent-on` dark |
|---|---|---|---|---|
| Tangerine (default) | `#E06313` | `#FF7A1A` | `#1A0F08` | `#FFF6EA` |
| Ember red | `#B22F18` | `#D63B1F` | `#1A0805` | `#FFF0EC` |
| Rust gold | `#8E6209` | `#B8830C` | `#1A1006` | `#FFF8E8` |
| Slate teal | `#2E5961` | `#3F7A85` | `#08181A` | `#E8F4F6` |

Each preset's pair validated at token-build time against `--gc-bg` and `--gc-bg-elevated` for AA contrast. CI fails build on any pair below 4.5:1.

**Semantic state colors:**

| State | Light | Dark | Use |
|---|---|---|---|
| Success | `#1F7A4A` | `#6BD49A` | Green check on Set commit, recovered-muscle chip |
| Warning (ACWR amber) | `--vis-amber-soft` | `--vis-amber-soft` | ACWR > 1.5 banner / chip |
| Danger (ACWR red) | `#B53A1A` | `#F58A6A` | ACWR > 2.0 banner — auto-deload suggestion |
| Disabled / muted | `rgba(--gc-text, 0.5)` | same | Inactive controls, placeholder |

### Typography System

**Typefaces:**

| Token | Family | Weights | Role |
|---|---|---|---|
| `--vis-font-display` | Geist | 600, 500 | Display + UI headings |
| `--vis-font-ui` | Geist | 400, 500, 600 | Body, captions, controls |
| `--vis-font-mono` | Geist Mono | 500 | Tabular numerics — weights, RIR/RPE, time, dates in tables |

Self-hosted via `@fontsource/geist` + `@fontsource/geist-mono`. No external CDN.

**Type scale (4 px baseline grid):**

| Token | Size / Line | Weight | Letter-spacing | Use |
|---|---|---|---|---|
| Display XL | 40 / 44 | 600 | -0.02em | Owner Dashboard hero only |
| Display | 28 / 32 | 600 | -0.015em | Active Session header, Onboarding welcome |
| Title | 20 / 26 | 600 | -0.01em | Card titles, screen titles |
| Body | 15 / 22 | 400 | 0 | Default text |
| Caption | 13 / 18 | 500 | 0.005em | Secondary labels, metadata |
| Eyebrow | 11 / 14 | 600 | 0.08em UPPER | Section labels, metric units |
| Numeric (tabular) | varies | 500 | 0 | Geist Mono — set weights, RIR/RPE, time, table numerics |

**Anti-aliasing + reading constraints:**

- `-webkit-font-smoothing: antialiased` + `text-rendering: optimizeLegibility` on all surfaces.
- Max line-length for body text: 72 characters on Admin Web (~640 px at body 15 px).
- Mobile line-length follows screen width; minimum 18 characters per line.
- Vertical rhythm: body line-height (22 px) is the base unit for vertical spacing inside cards.

### Spacing & Layout Foundation

**Spacing scale (the only valid values, per CLAUDE.md):** `4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 56 · 72` (px). No off-grid (no 7, no 10, no 18). Enforced by ESLint custom rule blocking arbitrary spacing literals.

**Grid (Admin Web):**

- 12-column grid · 80 px max column · 24 px gutter
- Page content max-width 1280 px · centered
- Card content max-width 760 px for prose; full-width for tables and dashboards

**Layout (mobile RN apps):**

- No grid system — vertical stack of cards with consistent spacing (16 px between cards; 20 px card padding)
- Safe-area insets respected on iOS + Android
- Bottom-tab nav reserves 56 px; content lives above
- Thumb-zone discipline: primary Set-row controls live in bottom 50% of screen on common phone form factors (per Challenge 8)

**Card composition:**

- Padding: `20` mobile (16 outside + 4 internal gap before title); `24` web
- Radius: `16` mobile, `20` web hero cards
- Shadow: 3-layer peach-tinted falloff per CLAUDE.md — `0 0 20px rgba(255,106,0,0.35)`, `0 0 60px rgba(255,106,0,0.18)`, `0 0 120px rgba(255,122,26,0.12)` for hero; smaller stack for standard cards

**Layout principles:**

1. **One number that matters per screen.** Home leads with Today's Plan card. Trainer Today leads with today's schedule. Owner Dashboard leads with revenue + branch breakdown. Never grid-of-12.
2. **Content leads, chrome follows.** Tables + dense data stay on solid surfaces; Liquid Glass blur reserved for nav + Active Session hero only.
3. **Single-column primary axis on mobile.** No side-by-side columns under 768 px.
4. **Vertical rhythm = 4 px unit.** Every vertical gap is a multiple of 4. No 7 px, no 10 px.

### Accessibility Considerations

Target = **WCAG 2.1 AA** on Admin Web v1. Mobile equivalent enforced by spec. Tamagui + Angular CDK handle primitives; Vis components ship explicit ARIA labels.

**Contrast:**

- Body text ≥ 4.5:1 against all background tokens. Token-build-time validation; CI fails build on drift.
- Large text (Display + Title) ≥ 3:1.
- Non-text affordances (icons ≥ 24 px, focus rings, chart strokes) follow the same `--accent` / `--accent-on` precomputed contrast pair.

**Hit targets:**

- Mobile: ≥ 44 × 44 pt per interactive element (Apple HIG + Material aligned).
- Web: ≥ 32 × 32 px clickable target; ≥ 24 px touch target on mobile-formatted admin views.

**Screen-reader labels:**

- VoiceOver (iOS) + TalkBack (Android) labels on every interactive element. Tamagui ARIA primitives + Angular CDK `cdkAriaLive` directives provide this where supported.
- Custom Vis components ship explicit ARIA labels — Orb state announced as "Recovery: 78%, increasing"; Logger announced as "cable, slab 8, 40 kilograms."

**Motion + transparency:**

- `prefers-reduced-motion: reduce` → orb breathing loop stops; PR cinematic collapses to static frame; Connection Orb pulse becomes a 1-frame fade; intro animations skip.
- `prefers-reduced-transparency: reduce` → Liquid Glass collapses to solid warm-tinted surface (`#1A1310` dark, `#FFF3E8` light); blur removed.

**Color blindness:**

- Critical state distinctions never rely on color alone. Recovery Orb colour-coding chips also carry text labels ("Recovered" / "28h"). Conflict Toast carries an "Overwritten by trainer" caption, not just a tint.

**Touch alternatives:**

- Voice fallback for trainer in-session (v1.1 spike).
- Hardware keyboard on Admin Web fully navigable; CDK focus-trap handles modals.

**Privacy ∩ accessibility:**

- Body measurements + biometric reads never announced via VoiceOver unless the user is on the specific surface — avoid passing health info to nearby observers.

---

## Design Direction Decision

### Design Directions Explored

Six visual directions were generated and previewed in the companion showcase (`ux-design-directions-2026-05-23.html`). All six used the locked Vis tokens, Geist + Geist Mono typography, 4-px spacing grid, and the orb-as-organism brand mark. They differed on information density, hero emphasis, layout structure, and which persona they optimised for.

| # | Direction | Lead | Best for |
|---|---|---|---|
| 1 | **Liquid Restraint** | Single hero orb, sparse data | Beginner Gym Client [Tarly]; solo client on deload week |
| 2 | Cinematic Data | Big-number (Progression Index) leads | Trainer monitoring + advanced solo client |
| 3 | Workshop / Tactile | Equipment-Aware Logger = hero | The mid-set moment |
| 4 | **Coach-led Companion** | Connection Orb header + presence + provenance | PT-attached Co-Edit moment (the structural moat) |
| 5 | **Solo Athlete** | PR + e1RM curves + streak heatmap | Independent strength athlete [Greyjoy] |
| 6 | **Owner Cockpit** | KPI grid + branch drill-down | Gym Owner [Lannister] + Gym Staff [Tully] |

### Chosen Direction

**Vis ships a per-surface composition.** No single direction dominates the entire product because the personas, surfaces, and moments demand different emphases. Locked decisions:

| Surface | Direction | Why |
|---|---|---|
| Client Home (PT-attached) | **Direction 1 — Liquid Restraint** | Honours "Quiet competence" + "Earned trust" emotional goals. Hero orb leads; one Today's Plan card. No data interrogation, no metric overload on home. |
| Active Session (all clients + trainer) | **Direction 4 — Coach-led Companion** | The signature surface where the moat lives. Connection Orb header + Presence Avatar Stack + Provenance + Ghost Set Row. Co-edit moment fully expressed. |
| Solo Client Home (variant when no PT) | **Direction 5 — Solo Athlete** | Recognition over reset — yesterday's PR + e1RM curve + streak heatmap. No Connection Orb (no trainer present). Same tokens, different composition. |
| Admin Web (Owner + Staff) | **Direction 6 — Owner Cockpit** | Cross-branch glance + KPI grid + drill-down. Warm cream bg instead of corporate white. Tables solid (no Liquid Glass), per "content leads" rule. |

**Universal across all surfaces** (no direction override): Geist + Geist Mono, 4-px spacing grid, the locked palette + accent tokens, Lucide icons, and the orb as the only repeating visual motif. The differences are composition, hero emphasis, and density — not visual identity.

### Design Rationale

1. **Direction 1 (Liquid Restraint) as the brand-pure expression on Client Home** sets the tone: a beginner Gym Client should arrive at Vis and feel calm, not overwhelmed. The hero orb breathing softly on a warm ivory background is the strongest possible signal of brand restraint. This is the home a PT-attached client returns to before and after a session — not a place for data interrogation.

2. **Direction 4 (Coach-led Companion) on Active Session** is non-negotiable because Active Session *is* the moat. The Connection Orb tension stem pulsing on each remote write makes the WebSocket-invisible-thing visible. Presence Avatar Stack, prescribed-vs-actual divergence tint, and the AI ghost row all live here. This is where users describe Vis differently from every competitor.

3. **Direction 5 (Solo Athlete) on the solo-client Home variant** because a Greyjoy-archetype solo lifter does not benefit from the empty Connection Orb a PT-attached client sees. Their recognition signal is the curve, the PR, the streak heatmap — not "your trainer hasn't logged in yet." Same tokens, same orb, different composition.

4. **Direction 6 (Owner Cockpit) on Admin Web** because the Owner's daily ritual is "morning glance + drill-down." A KPI grid + branch list serves SM-7 (trainer adherence monitoring) and SM-8 (audit-log query) directly. The warm cream bg + amber accents + generous spacing differentiate this from Mindbody / Glofox corporate dashboards (an explicit anti-pattern from step-05).

**Why not pick one direction:**

- Direction 2 (Cinematic Data) on Client Home would alarm a beginner — wrong persona match.
- Direction 3 (Workshop / Tactile) is the Logger interaction itself, not a screen-level composition — its principles are absorbed into Active Session.
- A single-direction product would force compromises on at least one of the four locked surfaces.

### Implementation Approach

**Component reuse across surfaces.** The brand layer custom components (Orb, Connection Orb, Recovery Orb, Equipment-Aware Logger, Set row, Presence Avatar Stack, Conflict Toast, Ghost Set Row, Risk Banner, Session Recovery Sheet, PR Moment composer, Branch-Scope Chip, KPI cards, Empty States set) all work across directions. The differences are layout + hero emphasis + density.

**Per-direction layout libraries:**

- **Liquid Restraint composition (Direction 1)** — single hero card pattern: 1 orb + 1 title + 1 primary CTA + optional secondary metadata. Vertical centring. Negative space dominates.
- **Coach-led Companion composition (Direction 4)** — header dock (Connection Orb + presence) + scrollable Set list + AI ghost row at bottom. Liquid Glass blur on header only.
- **Solo Athlete composition (Direction 5)** — KPI strip top + 2 charts middle + Today's Plan card bottom. Recognition layer (last-PR / streak / e1RM) leads.
- **Owner Cockpit composition (Direction 6)** — 3-up KPI grid + branches list + audit-log query input. Warm cream bg. Tables solid (no Liquid Glass).

**Cross-surface continuity rules:**

- Orb appears in all four directions but at different scales: hero (Direction 1) → medium (Direction 4 header) → small (Direction 5 accent) → absent from Direction 6 main content (kept only as nav-corner mark).
- Branch-Scope Chip always visible in nav (Direction 6 emphasises it; others reserve a corner).
- PR Moment cinematic uses the same composition + 1.5 s timing regardless of which Home direction is active.
- Recovery Orb appears on Direction 1 and Direction 5 (Client Home variants) — not on Direction 4 (Active Session) or Direction 6 (Admin Web).

**Direction-specific motion language:**

| Direction | Motion |
|---|---|
| 1 Liquid Restraint | Hero orb breathes 6 s loop (opacity 1 → 0.94); single CTA; no other animation on Home. |
| 4 Coach-led Companion | Connection Orb pulse on remote write (800 ms); avatar fade-in on commit; AI ghost slide-in from below. |
| 5 Solo Athlete | Curves redraw on tab focus; heatmap cells animate sequentially on Monday; PR moments fire ≤ 1.5 s. |
| 6 Owner Cockpit | KPI count-up 360 ms on load; branches list animates row-by-row; no decorative motion. |

All directions honour `prefers-reduced-motion: reduce` — every loop and intro animation skips; only enter / exit fades remain.

---

## User Journey Flows

Six journeys with detailed mechanics. PRD ships UJ-1/2/3 as narratives; this section adds entry → interaction → feedback → completion mechanics for each plus three additional journeys (multi-role identity switch, owner ritual, offline reconcile) that were under-served in the PRD. Full visual flow boards with edge cases live in the companion HTML: `ux-spec-step-10-preview-2026-05-23.html`.

### UJ-1 — Live Co-Edit Session

**Actors:** Brienne of Tarth (Gym Trainer) + Samwell of Tarly (PT-attached Gym Client). **FRs:** FR-1, FR-3, FR-7, FR-8. **SM:** SM-3 Co-Edit ≥ 25% by D90.

**Flow:** Brienne opens Active Session → Samwell joins same WebSocket room → Connection Orb intro (800 ms) → Brienne drags pin to 40 kg (optimistic commit; tension stem pulses) → Samwell sees pin animate ≤ 500 ms → Samwell lifts; either taps Done; LWW resolves → green check + provenance avatar + timestamp (visible 30 s) → rest timer starts (isolation 1:00, haptic at -10 s) → server-side PR check → PR Moment fires only on server-confirm (cinematic ≤ 1.5 s) → Session marked complete; Progression Index recomputes.

**Edge cases:** Both tap Done within 50 ms → server-stamped monotonic order, Conflict Toast on loser. WebSocket disconnect → Connection Orb tints "Disconnected / Stale," writes queue, 5 s drain on reconnect. Polling-fallback active → telemetry does NOT count as Co-Edit (gates on `ws.readyState === OPEN && peer_presence === true`). Trainer flips assistance flag mid-Set → row updates with Brienne's avatar; PR detection excludes assisted reps.

**Components:** Active Session header (Liquid Glass, Direction 4) · Connection Orb · Presence Avatar Stack · Set row · Provenance line · Ghost Set Row · Rest timer · Conflict Toast · PR Moment composer.

### UJ-2 — Solo PR + Share

**Actor:** Theon of Greyjoy (Solo Client). **FRs:** FR-10, FR-11. **SMs:** SM-5 PR-Card share ≥ 30%; SM-C10 no-share > 80% triggers.

**Flow:** Opens Active Session with today's prescribed 195 × 3 → warm-up sets 1-3 (long-press → flag warm-up) → working sets 1-4 hit 195 × 3 → Set 5 barbell visualizer (snap plates to 200 kg, auto-sum) → lifts 1 unassisted + 1 spotter-assisted (rep-quality bar) → server-side PR check across 4 dimensions (1RM, e1RM, total-volume, reps-at-load) → PR Moment cinematic ≤ 1.5 s (amber sweep, dismissible) → composer (3 aspect ratios, watermark default ON) → share to native sheet (Instagram / WhatsApp / X / camera roll). SM-5 fires on share; SM-C10 increments if dismissed without share.

**Edge cases:** Spotter-assisted rep = half-credit in Progression Index (PRD assumption). Dismissed PR → archived with one-time "unshared PR" nudge. Watermark toggled OFF persists per user. 1RM PR without e1RM PR still fires (any of 4 dimensions trigger).

**Components:** Active Session (Direction 4, Connection Orb dimmed since solo) · Equipment-Aware Logger (barbell) · Set row with rep-quality bar · PR Moment cinematic · PR Card composer · native share-sheet.

### UJ-3 — Gym Onboarding

**Actors:** Samwell of Tarly (new Gym Client) + Edmure of Tully (Front-desk Staff) + Brienne of Tarth. **FRs:** FR-25, FR-27, FR-28. **SMs:** SM-1 WALS; SM-6 Onboarding ≥ 60%; SM-C8 no-log > 50% triggers.

**Flow:** Edmure imports CSV → Samwell scans QR (brand tenant URL) → sign in Google (Firebase JWT) → roster matched by phone → ~10-field Questionnaire (advanced section skippable) → trainer browse (3 rating dimensions, cert badges) → Samwell picks Brienne #1 with 2 fallback priorities → FCM push to Brienne's request inbox → Brienne reviews + accepts (PtActive state) → Brienne assigns phased plan (PPL Beginner 12 wk) → Samwell home unlocked. SM-6 fires on first session opened within 24 h.

**Edge cases:** Brienne no-response 24 h → next-priority trainer notified. All 3 decline → marketplace browse opens (v1.5 deferred; v1 = invite-link only). No roster match → "pending activation" until Edmure manually activates. Samwell abandons mid-Questionnaire → state persists; resumes from last field. Two trainers accept simultaneously → first server-stamped accept wins; second sees "already assigned" toast.

**Components:** QR scanner · Sign-in (Google / Apple / Phone OTP) · Onboarding Questionnaire · Trainer Public Profile Card · Trainer request flow · FCM push · PT-relationship state machine · Plan Builder · Today's Plan card.

### UJ-4 — Multi-Role Identity Switch (new in this spec)

**Actor:** Davos of Seaworth (Solo Trainer + Gym Trainer at 2 brands). **FR:** FR-25 + RBAC. **Significance:** the multi-role moat surface.

**Flow:** Davos opens Trainer App → default identity = Solo · Davos shown in Branch-Scope Chip → taps chip → identity sheet ("You work at 3 brands") → picks "Brand A · Kandivali" → app reloads Brand A context (today's schedule, AI history, audit scope all scoped to Brand A) → runs Brand A sessions all morning → at lunch taps chip again → picks "Brand B · Borivali" → app reloads Brand B context; AI history per (Davos × client × exercise) follows his trainer identity across brands.

**Edge cases:** Wrong-scope write eliminated by design — backend rejects mismatched scope (403). Brand A + Brand B share a client → AI history shared because trainer identity is same Davos (PRD Open Question 5). Switch during active Co-Edit Session → Session locks scope on start; must complete or abandon before switching. New brand invite mid-day → identity sheet refreshes with "new" badge.

**Components:** Branch-Scope Chip (always visible in nav) · Identity switcher sheet · Trainer Today schedule (re-renders per brand) · Audit-log scope indicator · Empty States.

### UJ-5 — Owner Morning Glance + Audit Query (new in this spec)

**Actor:** Tywin of Lannister (Gym Owner, 7 branches). **FRs:** FR-20, FR-21, FR-22, FR-23, FR-26. **SMs:** SM-7 Trainer monitoring ≥ 50%; SM-8 Audit query ≥ 2/brand/week.

**Flow:** Tywin opens Admin Web 8:30 AM → Owner Dashboard loads (Direction 6 layout) → 3-up KPI grid (Revenue · Active PT · WALS) with sparklines → notices anomaly (Borivali churn ↑, 3 active PT churning) → drills into Borivali branch detail → queries audit log "payment edits last 7d" (SM-8 increments) → spots pattern (3 voids same staff shift) → sends message to branch manager via WhatsApp template generator.

**Edge cases:** All branches green → no anomaly highlights, owner skims < 30 s. If no anomalies for 14 days, audit usage may dip below SM-8 threshold; surface via weekly digest. Audit query returns 0 rows → empty state with broaden / retry suggestions. Tywin tries to edit a logged audit entry → rejected (append-only); shown as read-only.

**Components:** Owner Dashboard (Direction 6) · KPI cards with sparkline · Branches list · Trainer Leaderboard · Audit-log query input · Empty States · WhatsApp template generator.

### UJ-6 — Offline → Reconcile (new in this spec)

**Actors:** Same as UJ-1. **FRs:** FR-1 + NFR §7.2 + NFR §7.5 idempotency UUID. **Significance:** gates SM-2 D7 retention; gym-basement WiFi reality.

**Flow:** Mid-Co-Edit Session, two phones, WS connected → WiFi drops on Samwell's phone → WS heartbeat fails > 5 s (`ws.readyState !== OPEN`) → Connection Orb tints "Disconnected / Stale" (calm peach, no alarm) → Samwell finishes set, taps Done → write queued locally with idempotency UUID (local ack ≤ 100 ms) → Set row shows "queued" badge → WiFi returns; WS reopens → offline queue drains ≤ 5 s with server-stamped monotonic order → Connection Orb resumes pulsing; "queued" badges become "committed" → conflict check vs trainer's writes during the gap → Session continues with no data lost.

**Edge cases:** Both sides offline simultaneously → both queue locally; reconcile on whoever reconnects first. Reconnect > 30 s → app stays usable in offline mode. App force-closed during offline → queue persists in local storage; Session Recovery Sheet offers Resume on next open. Idempotency UUID collision (duplicate) → no double-write; prior commit confirmed silently. Server rejects on reconnect (validation) → Set row badge transitions to "stale" with retry / dismiss.

**Components:** Connection Orb (Disconnected / Stale state) · Set row queued / committed / stale badges · Idempotency UUID infrastructure · Conflict Toast · Session Recovery Sheet.

### Journey Patterns

Reusable patterns that recur across the six journeys:

| Pattern | Recurs in | What it does |
|---|---|---|
| **Branch-Scope Chip identity** | UJ-1 · UJ-4 · UJ-5 | Always visible in nav. Single source of truth for "who am I, where am I." Tap = identity sheet. |
| **Connection Orb presence** | UJ-1 · UJ-6 | Visible link between two parties on the same Session. Tension stem pulses on remote write. Tints to "Disconnected / Stale" on network loss. |
| **Provenance line** | UJ-1 · UJ-4 · UJ-6 | Set row badge = actor avatar + timestamp. Visible within 500 ms of commit. Fades after 30 s of no further writes. |
| **Prefill cascade** | UJ-1 · UJ-2 · UJ-3 · UJ-4 | Logger never opens empty. Order: AI ghost → trainer prescribed → last-Set → AI bootstrap → empty + keypad. |
| **Cinematic moment post-server-confirm** | UJ-1 · UJ-2 | PR Moment fires only after server confirms detection. Optimistic celebrations forbidden (Emotional Principle 3). |
| **Queued vs committed visual state** | UJ-6 | Set-row badge: "queued" (local ack) → "committed" (server ack) → "stale" (failed reconcile). Tied to `(ws.readyState, offline_queue.depth, last_server_ack_at)` tuple. |
| **Non-modal conflict surfaces** | UJ-1 · UJ-6 | Conflict Toast is one-line non-blocking. Never modals mid-Set. Tappable revert; ignored = remote write stands. |
| **FCM notification → context restore** | UJ-3 · UJ-5 | Tapping a push opens app to the exact relevant surface with branch scope already correct. |

### Flow Optimization Principles

1. **Minimise steps to first Set logged.** UJ-3 onboarding has ~11 steps; first Set should follow within 24 h of completion (SM-6 + SM-C8). Onboarding fields skippable where not strictly required.
2. **Reduce cognitive load at every decision point.** UJ-4 identity switch presents the chip — not a settings menu dive. UJ-5 owner dashboard surfaces anomalies without requiring a query first.
3. **Provide visible progress at every step.** UJ-2 PR detection shows running compute; UJ-3 onboarding shows "X of 10 fields"; UJ-6 reconcile shows "queued / committed" per Set row.
4. **Create moments of delight only when earned.** UJ-1 + UJ-2 PR Moment fires only on server-confirmed detection. No celebratory animation on optimistic write.
5. **Handle edge cases gracefully.** Every journey lists 4-6 edge cases. UI degrades to a calm explicit state — never a silent failure.
6. **Persist state across drops.** UJ-3 abandon mid-Questionnaire = resume from last field. UJ-6 force-close offline = Session Recovery Sheet on next open. State trust > re-derivation.

---

## Component Strategy

### Design System Components (foundation, library-backed)

What comes from libraries underneath the brand layer. Adapted to Vis tokens, never imported raw.

| Surface | Library | What it provides |
|---|---|---|
| RN primitives | **Tamagui** | Button, Input, Dialog, Sheet, Toast, Popover, ScrollView, FlatList wrapper, Avatar, Spinner, Animated views (via Reanimated 3) |
| Angular a11y | **Angular CDK** | Overlay, FocusTrap, FocusMonitor, A11yModule (LiveAnnouncer, FocusKeyManager), Drag-Drop, Portal, Layout (BreakpointObserver) |
| Web styling | **Tailwind CSS** | Utility classes driven by shared `tokens.css`; `preflight` scoped away from `.cdk-overlay-container` |
| Icons (both apps) | **Lucide** | ~1400 line icons, RN + Angular ports; one stroke weight; no brand mark (orb is the only Vis illustration) |
| RN charts | **Victory Native** | Line, area, bar — Progress Tab; consumes Vis tokens; respects reduce-motion |
| Web charts | **Apache ECharts** | Owner Dashboard KPI charts, branch performance, audit timeline |
| Liquid Glass | **@react-native-community/blur** | UIVisualEffectView (iOS), RenderEffect (Android 12+); gradient overlay fallback on Android < 12 |
| Orb hero | **react-native-skia** | Gradient morphing + shape distortion for the central orb; +2 MB bundle, hero only |
| Typography | **@fontsource/geist** + Geist Mono | Self-hosted, no external CDN |
| Component scaffolding | **21st-dev/magic MCP** | Generates JSX/TSX from intent; output adapted via 5-step Vis-tokens checklist before merge |

### Custom Components

Thirteen custom components define the Vis brand layer. Each spec includes purpose, anatomy, states, variants, accessibility, and interaction behavior. Full collapsible spec cards live in the companion HTML: `ux-spec-step-11-preview-2026-05-23.html`.

#### 1. Orb (5 tension states) — brand-locked

**Purpose:** signature visual; backend-signal-driven sphere conveying session state, recovery, and presence at a glance. Never decoration.
**Anatomy:** 3-layer gradient (radial peach top + linear amber body + smoked-glass overlay) + 3-layer peach-tinted shadow stack. Uses `--brand-locked-*` tokens; never themable by accent preset.
**States:** *At rest* (no Session in 24 h) · *Under load* (`ws.readyState === OPEN && session.active`) · *Peak tension* (`last_set.is_working && intent === 'failure'`) · *Recovered* (`session.completed_at` + post-session timer) · *Disconnected / Stale* (`ws.readyState !== OPEN OR offline_queue.depth > 0`).
**Variants:** Hero (110 × 110 px Direction 1) · Medium (56 px Direction 4 header) · Small (32 px nav corner) · X-large (150 px PR Moment).
**A11y:** ARIA label announces state plain-language. Reduce-motion stops breathing; state readable in static frame. Reduce-transparency collapses gradient to solid amber.
**Interaction:** Non-interactive on most surfaces. On Recovery Orb (Direction 1 + 5 home), tap opens muscle-group breakdown sheet.

#### 2. Connection Orb (live trainer ↔ client tension stem) — brand-locked

**Purpose:** visualises the live WebSocket bond between trainer and client during a Co-Edit Session. Makes the moat visible.
**Anatomy:** two avatar circles linked by horizontal tension stem (gradient amber). Stem brightness encodes presence; pulses 800 ms on remote write.
**States:** *Idle* (both connected, no recent writes — dim) · *Active* (one party wrote within 60 s — mid-bright) · *Live mutual* (both wrote within 60 s — bright + glow pulse) · *Pulsing* (per-write animation) · *Disconnected* (WS down — fades to peach tint, avatars greyed).
**Variants:** Header dock (Active Session, ~88 px wide) · Inline (FCM notification preview, ~44 px).
**A11y:** "Trainer is connected. Last edit by Brienne, 12 seconds ago." Throttled `cdkAriaLive` on state change.
**Interaction:** Tap → presence detail sheet (who's online, last write per party, reconnect time).

#### 3. Equipment-Aware Logger family (6 variants — FR-8, gesture moat)

**Purpose:** Set logging via equipment-shaped gestures. No typing in the happy path.
**Anatomy per variant:** **Cable** = vertical pin-stack with draggable pin · **Barbell** = bar with two-sided plate-snap, auto-sum total · **Dumbbell** = number-line slider with snap increments · **Machine pin-stack** = same as cable · **Bodyweight** = silhouette + optional vest field · **Kettlebell** = discrete picker matching standard weights · **Bands** = colour-coded tier picker.
**States:** Idle (prefilled per cascade) · Active (gesture in progress) · Committed (server-ack + green check) · Queued (offline) · Stale (failed reconcile) · AI-suggested (ghost overlay visible).
**Variants:** All 6 share `onCommit(load, reps, source)`, `prefill`, `variant`. Numeric-keypad fallback always available via long-press.
**A11y:** Announces "cable, slab 8, 40 kilograms" on focus. Drag gestures have keyboard equivalents (arrow keys ± slab). Hit targets ≥ 44 × 44 pt. Reduce-motion: settle animations removed.
**Interaction:** Drag / snap / slide / pick / toggle. Long-press = numeric keypad. First-time tap = 2 s animated hint, dismisses after first successful gesture.

#### 4. Active Session Set Row (FR-1, FR-3)

**Purpose:** single-row Set representation with prescribed-vs-actual, warm-up, failure intent, per-rep assistance, provenance.
**Anatomy:** from left — warm-up checkbox · prescribed (load × reps × RPE in mono) · actual (load × reps × RPE in mono, accent-tinted on divergence) · rep-quality bar · Done toggle · provenance avatar + timestamp.
**States:** Empty (no actual) · Active (gesture in progress) · Committed (green check) · Queued (offline badge) · Conflict (Conflict Toast spawn) · Edit mode (long-press → fields editable, audit-log entry on save).
**Variants:** Mobile (full-width, 64 px height) · Tablet (56 px height). PT-attached: includes Presence Avatar Stack.
**A11y:** "Set 3, prescribed 50 kilograms 10 reps, actual 47.5 kilograms 10 reps." Each field independently focusable.
**Interaction:** Tap any field = edit via appropriate Logger. Long-press row = Edit / Mark warm-up / Mark abandoned.

#### 5. Branch-Scope Chip (identity switcher — FR-25, multi-role moat)

**Purpose:** always-visible nav element showing current identity + branch scope. Tap to switch.
**Anatomy:** pill shape with small orb glyph + brand initial + branch name. Right-side chevron when multiple options available.
**States:** Single-scope (no chevron, informational) · Multi-scope (chevron, tap opens sheet) · Active switching (loading spinner during swap) · All-branches view (Owner — shows "All Branches").
**Variants:** Mobile nav corner (32 × 32 collapsed) · Web nav left-side (full pill) · Identity sheet variant.
**A11y:** "Current scope: Brand A, Kandivali. Tap to switch." LiveAnnouncer fires "Switched to Brand B, Borivali" on change.
**Interaction:** Tap → identity sheet (bottom-sheet mobile / dropdown web). During active Co-Edit Session, chip is read-only.

#### 6. Ghost Set Row (AI suggestion — FR-12, FR-14)

**Purpose:** greyed-out AI-suggested next Set below the live row. Tap accept; long-press override.
**Anatomy:** same field structure as Set row, 50% opacity in `--accent` color. Confidence indicator reserved (`--ai-confidence-low/med/high` token; component lands v1.1).
**States:** Pending (skeleton during Claude proxy call up to 6 s) · Loaded · Accepted (transitions to live Set row) · Dismissed (slides out + records override telemetry) · Stale (arrived after user logged → self-dismiss with "stale-ai" badge).
**Variants:** Solo client: AI Bootstrap only per FR-13. Trainer: full per-set AI v1.
**A11y:** "AI suggests 52 kilograms 10 reps. Tap to accept, long-press to override." First time = inline coachmark, fades after first interaction.
**Interaction:** Tap = accept; long-press = numeric override. `ai_locked` clients lose the override surface.

#### 7. PR Moment Composer (FR-10, FR-11, SM-5)

**Purpose:** cinematic detection + share composer for personal records.
**Anatomy:** full-screen overlay — amber emissive sweep (≤ 1.5 s) → headline "200 kg × 1 — your new best" → composer with 3 aspect-ratio templates → native share-sheet trigger.
**States:** Detection-pending (server compute) · Detected (cinematic fires) · Composing (preview templates) · Sharing (native sheet open) · Dismissed (archived, one nudge per unshared PR).
**Variants:** Solo (full cinematic) · PT-attached (same + co-shared with trainer feed) · ACWR-triggered demote (no cinematic if recent volume spike).
**A11y:** Cinematic dismissible via tap or system back. ARIA labels on each share target. Reduce-motion = static frame.
**Interaction:** Auto-fires on detection. Watermark default ON, per-user toggle persists. SM-C10 if > 80% no-share triggers composer copy review.

#### 8. Recovery Orb (per-muscle readiness — FR-35, FR-36)

**Purpose:** at-a-glance per-muscle-group recovery status on Client Home.
**Anatomy:** central orb with 12 small chip-segments arranged radially. Each chip green / amber / red based on time since last training that group.
**States:** All recovered (all green) · Mixed (multi-colored) · Heavy load (red segments warn against training group today) · Cold start ("baseline-building, day X / 14").
**Variants:** Home (Direction 1 — large) · Progress Tab (smaller summary) · Pre-session check (shown before Active Session if any segment red).
**A11y:** "Chest: recovered. Back: 28 hours remaining. Quads: 2 hours remaining."
**Interaction:** Tap = full breakdown sheet. Long-press segment = jump to that muscle's history.

#### 9. Presence Avatar Stack (FR-1, FR-25)

**Purpose:** overlapping avatar dots on a Set row showing who's currently editing live.
**Anatomy:** up to 3 overlapping circular avatars (current + recent writers). 22 × 22 px each. Most-recent-on-top.
**States:** Empty · Single · Multi-presence (trainer + client) · Recent-write fade (avatar persists 30 s post-commit, then fades).
**Variants:** Set-row-level (small, inline) · Active Session header (medium, with name labels) · Owner audit log (large with role labels).
**A11y:** "Brienne is editing. Last edit by Samwell, 8 seconds ago." Throttled live region.
**Interaction:** Tap = presence-detail sheet.

#### 10. Conflict Toast (FR-1 LWW)

**Purpose:** non-modal notification when a remote write overwrote yours.
**Anatomy:** bottom-sheet single-line toast — avatar of overwriter + "Trainer overwrote your value 47.5 → 50" + Undo + dismiss.
**States:** Visible (8 s default) · Tapped Undo (revert via server stamp) · Dismissed (auto or manual).
**Variants:** Single-conflict · Batch (multiple overwrites collapsed into "5 writes overwritten" with expand).
**A11y:** Live region announces overwrite; Undo is hard-target ≥ 44 × 44 pt.
**Interaction:** Auto-dismiss 8 s; tap = persistent. Tap Undo → revert; recipient sees their own Conflict Toast.

#### 11. Risk Banner (ACWR alert — FR-37)

**Purpose:** trainer-side alert when a client's ACWR crosses risk thresholds.
**Anatomy:** inline banner on Trainer client-detail — amber tint (ACWR > 1.5) or red tint (ACWR > 2.0) + 1-line summary + primary action ("Apply deload to next session") + dismiss.
**States:** Amber (consider deload) · Red (auto-deload via Ghost Set Row at 70%) · Cold-start ("baseline-building, day X / 14" — suppressed first 14 days).
**Variants:** Inline banner · FCM push (single client) · Dashboard summary (across clients in Trainer Today).
**A11y:** "Risk alert for Samwell. ACWR 1.7 — injury risk zone." Action button = primary focus target. Tone: "consider deload," never "you're overtraining."
**Interaction:** Primary action applies deload to next Session (Ghost Set Row updates). Dismiss = hidden until next ACWR recompute (weekly).

#### 12. Session Recovery Sheet (abandonment flow)

**Purpose:** on next open after Session abandoned (last activity > 30 min ago without explicit End), offer Resume or Discard.
**Anatomy:** modal sheet — title "Unfinished session from 47 minutes ago" + 2-3 partial Set rows preview + Resume CTA + Discard secondary.
**States:** Pending · Resume tapped (returns to Active Session intact) · Discard tapped (confirmation, then partial Sets archived).
**Variants:** Short-abandon (< 2 h) · Long-abandon (> 24 h — Discard preselected).
**A11y:** Modal traps focus. "Unfinished session from 47 minutes ago. 3 partial sets. Resume or discard?"
**Interaction:** Blocks Active Session entry until choice made. Discard has a confirmation step.

#### 13. Empty States set (cross-surface)

**Purpose:** surface-specific empty-state illustrations + copy + next-action CTA. 21st-magic won't get these right by default.
**Anatomy:** centered — small orb glyph + 1-line plain-language heading + 1-line context + primary CTA + optional secondary link.
**Variants:** Empty Logger ("Pick an exercise to start logging") · Empty PR feed · Empty branch dashboard (Owner) · Empty client list (Trainer) · Empty trainer roster (Staff) · Empty audit log (Owner).
**A11y:** Each has descriptive ARIA + a hard-focusable CTA. No "ghost" decoration with no action.
**Interaction:** Primary CTA navigates to resolution (e.g. Empty Logger → exercise picker).

### Component Implementation Strategy

- **Token discipline.** Components import via Layer B (`--accent`, `--brand-locked-*`). Brand-locked allowlist (orb, wordmark, PR-watermark) may import Layer A `--vis-amber-*` directly. ESLint `no-restricted-syntax` blocks raw hex in `/components/**`.
- **Composition over inheritance.** Custom components compose Tamagui primitives — never fork the library. Edge cases get a sibling custom component.
- **21st-magic workflow.** Every new component: inspiration → builder/refiner → 5-step "adapt to Vis tokens" checklist → merge.
- **Accessibility default.** Tamagui + CDK provide ARIA primitives; Vis-custom components ship explicit ARIA labels. CI gate via pa11y-ci + Node `wcag-contrast` script.
- **State machine per component.** Every component with > 2 states has a documented state diagram. UI state derived from backend signals where possible.
- **Cross-platform parity.** Components shared across RN + Angular (KPI cards, Empty States, Branch-Scope Chip on Admin) ship same props, same a11y labels, same state names.

### Implementation Roadmap

Five phases. Each gated by the SM it unlocks. Phase 0 + 0.5 are foundation (per step-06). Feature work starts Phase 1.

| Phase | Focus | Components | SM gates |
|---|---|---|---|
| **0 + 0.5** | Foundation | tokens.ts package, Tamagui + Babel plugin, Angular CDK + Tailwind, Lucide, Geist, Reanimated + Gesture Handler + blur + Skia on EAS dev client, WCAG contrast CI, ESLint guardrails, 6 gesture maps documented | none — pure infra |
| **1** | Active Session core (the moat) | Orb (5 states) · Connection Orb · 6 Equipment-Aware Loggers · Active Session Set Row + Provenance · Presence Avatar Stack · Conflict Toast · Ghost Set Row (placeholder; AI confidence chip reserved v1.1) · Rest timer | SM-1 WALS · SM-3 Co-Edit ≥ 25% · SM-C6 set-save p95 |
| **2** | Sharing + PR loop | PR Moment cinematic · PR Card composer (3 aspect ratios) · Native share-sheet integration · Vis watermark + per-user toggle | SM-5 PR-Card share ≥ 30% · SM-C10 no-share < 80% |
| **3** | Recovery + risk | Recovery Orb (12 muscle segments) · Per-muscle weekly set-count chip · Risk Banner (ACWR amber + red) · FCM ACWR push · Progress Tab (line charts + heatmap + sparkline) | SM-2 D7 ≥ 35% · SM-7 trainer-monitoring ≥ 50% |
| **4** | Multi-role + Admin Web | Branch-Scope Chip (identity switcher) · Owner KPI cards + sparklines · Branch dashboard tables (solid) · Audit-log query · Trainer Leaderboard · Staff CSV import + PT activation + payment log | SM-7 + SM-8 ≥ 2/brand/week |
| **5** | Lifecycle + empty states | Onboarding Questionnaire (shared, path-aware shell) · Session Recovery Sheet · Empty States set (6 surfaces) · Plan-block expiry FCM cadence · PT-relationship state machine UI | SM-6 onboarding ≥ 60% · SM-C8 no-log < 50% · SM-4 Solo D30 |

---

## UX Consistency Patterns

Seven pattern categories with concrete rules. Apply uniformly across Client App, Trainer App, and Admin Web — only hit-target sizing differs per platform.

### Button Hierarchy

- **One primary action per screen.** Multiple primaries = no primary. Use secondary for alternates.
- **Primary** uses `--accent` fill (Tangerine v1; user-themable v1.1). Text uses `--accent-on`, precomputed for ≥ 4.5:1 contrast.
- **Secondary** is outlined (transparent fill, `--accent` border + text). Same accent color.
- **Ghost / tertiary** for inline actions (no border, accent text only). Use sparingly.
- **Destructive** uses `#B53A1A` (light) / `#F58A6A` (dark). Always requires confirmation step for irreversible actions.
- **Disabled** uses `rgba(--gc-text, 0.5)`. Never the only path forward — disabled CTAs without an explanation create dead-ends.
- **Loading state on primary:** spinner inside button + disable interaction. Don't replace label with spinner — keep label visible for context.
- **Icon-only buttons** only when label is universal (close ×, back ←). Always with explicit `aria-label`.
- **Hit targets:** ≥ 44 × 44 pt mobile; ≥ 32 × 32 web; ≥ 24 × 24 mobile-formatted admin views.

### Feedback Patterns

Three tiers: toast (ephemeral), banner (persistent contextual), inline (per-field validation).

**Toasts — ephemeral, auto-dismiss:**

- Auto-dismiss 4 s (success / info), 8 s (warn), persistent (error until dismissed or retried).
- Bottom-anchored above nav on mobile; top-right on web.
- Stack vertically when multiple; max 3 visible; older collapse with "+N more."
- Tap = persistent (don't auto-dismiss while user interacts).
- Live region announce; throttled to avoid screen-reader spam.

**Banners — persistent, contextual:**

- Inline within the relevant view, never floating.
- Persistent until user takes action OR explicit dismiss.
- Always include a clear primary action — never a banner without a path forward.
- Tone discipline: amber = "consider X" / red = "auto-X recommended" — never "you're failing."

**Inline (per-field):** see Form Patterns below.

### Form Patterns

- **Single column.** Never side-by-side fields on mobile. On web, max 2 columns and only when fields are short (city + state).
- **Label above field**, not floating-inside. Floating labels are inconsistent with screen-reader behaviour at scale.
- **Required marker** = `*` after the label, not "*Required" copy.
- **Helper text below field** — explanatory by default; switches to error tone when validation fails.
- **Validate on blur**, not on every keystroke. Exception: format-restricted fields (numeric, OTP) validate on input.
- **Error state:** red border + plain-language error in helper slot. Never modal alerts for form errors.
- **Submit behaviour:** if validation fails on submit, focus the first invalid field + announce error via live region.
- **Onboarding Questionnaire:** show "X of 10 fields" progress; skip-link visible for advanced section; state persists across abandonment.

### Navigation Patterns

- **Mobile:** 4 bottom-tabs max, thumb-zone respect. Active tab uses `--accent` + bold weight.
- **Web (Admin):** left sidebar with collapsible groups. Active section uses `--accent` indicator + bg tint.
- **Branch-Scope Chip** always visible in nav when user has multi-scope identity. Top-right mobile; top-left web.
- **Back navigation** = standard system back (gesture / device button) + in-app left-chevron header arrow.
- **Deep links** land on destination + correct scope. If scope mismatch, prompt to switch before opening.
- **Tab badges** for unread / urgent (max 3 surfaces — overusing kills the signal). E.g. Trainer App "Clients" tab badge when an ACWR alert fires.
- **FCM tap-to-restore:** tapping a push opens app to the exact relevant surface with branch scope already set.

### Modal · Sheet · Overlay Patterns

- **Modals only for:** irreversible actions (discard, delete), blocking decisions (Session Recovery Sheet), full-screen onboarding moments (Questionnaire).
- **Sheets for:** contextual choices (Branch-Scope identity switcher), filter sheets, action menus.
- **Toasts for:** ephemeral feedback (Set commit, Brienne is editing).
- **Banners for:** persistent advice (ACWR Risk Banner, plan-block expiry warning).
- **No modal stacking.** One modal at a time. New modal request while another open → defer until first closes or replace with confirmation.
- **Sheets can dismiss via:** tap backdrop, swipe-down (mobile), Escape (web), explicit close button (always present).
- **Modal focus trap** via Angular CDK `FocusTrap` or Tamagui equivalent. Cannot tab out of modal.

### Empty + Loading States

- **Skeleton first.** If the structure of loaded content is known (Set rows, KPI cards, list items), render skeletons matching the structure. Don't show a spinner.
- **Spinner only when structure unknown** (initial app boot, error state recovery).
- **Never both.** Empty state OR loading state — never stacked.
- **Every empty state has a CTA.** Empty without a next action = dead end.
- **Skeleton motion** = subtle shimmer 1.5 s loop. Reduce-motion = static skeleton.
- **Per-surface empty states** ship as component (per step-11): empty logger, empty PR feed, empty branch dashboard, empty client list, empty trainer roster, empty audit log.

### Search + Filter Patterns

- **Debounce input 200 ms.** Fires after the user stops typing. Reduces server load + flicker.
- **Filter chips scroll horizontally** on mobile, sticky under nav. Active chip = `--accent` fill + `--accent-on` text.
- **Multi-select chips** when filters are non-exclusive (e.g. "On Plan" + "Flagged ACWR"). Single-select when mutually exclusive.
- **Sort is secondary.** Sort dropdown lives in a small affordance to the right of the search bar — not a top-level filter.
- **Empty search results** use the Empty States set ("No clients match 'samwell123' — try a broader name or clear filters").
- **Shared pattern** across: Trainer client list, Admin member list, Plan Template library (FR-31), Trainer Marketplace (FR-34, v1.5).
- **Search history** v2 — not v1.

---

## Responsive Design & Accessibility

### Responsive Strategy

Vis is 3 apps with two different responsive postures.

| App | Platform | Posture | Responsive strategy |
|---|---|---|---|
| Client App | React Native (iOS + Android) | mobile, one-handed, mid-session | No breakpoint responsive. Vertical stack adapts to safe-area insets. Optimised for portrait. Landscape supported on Active Session for tablet-sized phones; otherwise locked. |
| Trainer App | React Native (iOS + Android) | mobile, two-hand authoring + one-hand in-session | Same as Client — no breakpoint responsive. Tablet support v1.1 (iPad: 2-column on Trainer Today with schedule left + client detail right). |
| Admin Web | Angular (browser) | desktop primary, mouse / keyboard | Desktop-primary responsive. 4 breakpoints (see below). Owner Cockpit (Direction 6) optimised for desktop; degrades cleanly to tablet (collapses 3-up KPI grid to 2-up); mobile fallback for read-only access (full edit on desktop only). |

**Cross-app principles:**

- Safe-area insets respected on iOS + Android — no content under notch / bottom-bar.
- Bottom-tab nav reserves 56 px on mobile RN apps — content lives above.
- Thumb-zone discipline on mobile — primary Set-row controls live in bottom 50% of screen on common phone form factors (Pixel 6a, Galaxy A54, iPhone 14).
- Web fluid layout via Tailwind utility classes + CSS `clamp()` for typography scale.
- No magic-number media queries — only the 4 breakpoints below are valid; documented in `tokens.css` as CSS custom properties.

### Breakpoint Strategy (Admin Web)

Desktop-primary. Four breakpoints.

| Breakpoint | Range | Use |
|---|---|---|
| Mobile fallback | 320–767 px | Single column. Tables stack as cards. KPI grid 1-up. Read-only mode (no edit / no audit query). Emergency owner check on phone. |
| Tablet | 768–1023 px | 2-column where applicable. KPI grid 2-up. Edit enabled. Front-desk Staff uses this at the desk. |
| Desktop (primary) | 1024–1439 px | Full layout. KPI grid 3-up. Sidebar fully expanded. Owner morning glance happens here. |
| Wide desktop | 1440 px+ | Page content max-width 1280 px centered. Wide screens get whitespace, not more density. |

**Rules:**

- Mobile-first CSS using `min-width` media queries. Default styles target Breakpoint 1.
- Use relative units (`rem`, `%`, `vw`) over fixed pixels in Admin Web Tailwind config.
- Page content max-width 1280 px regardless of screen.
- Card content max-width 760 px for prose; full-width for tables and dashboards.
- Image / asset optimisation via Angular lazy-loading + WebP. Avatars: SVG when possible.
- No horizontal scroll except on filter chip rows (intentional, sticky).

### Accessibility Strategy

**Target: WCAG 2.1 AA on Admin Web v1.** Mobile equivalent enforced by spec.

**What Vis ships:**

- Color contrast ≥ 4.5:1 body text against all background tokens. Token-build-time validation; CI fails on drift.
- Large text ≥ 3:1 (Display + Title sizes).
- Keyboard navigation fully supported on Admin Web; mobile via VoiceOver gestures / TalkBack swipes.
- Screen-reader labels on every interactive element (Tamagui ARIA + Angular CDK + explicit labels on Vis-custom components).
- Hit targets ≥ 44 × 44 pt mobile; ≥ 32 × 32 web.
- Focus indicators always visible — 2 px outline in `--accent`; never `outline: none`.
- Reduce-motion support — orb breathing, PR cinematic, Connection Orb pulse, AI ghost slide-in all degrade gracefully.
- Reduce-transparency support — Liquid Glass collapses to solid warm-tinted surface; blur removed.
- Color blindness — critical state distinctions never rely on color alone. Recovery Orb chips carry text labels; Conflict Toast carries caption.
- Skip links on Admin Web (skip to main content, skip to nav).
- Form labels always above field, never floating, with explicit `for` / `id` association.

**What Vis does NOT target v1:**

- WCAG 2.1 AAA — not required v1; AA is industry standard for fitness apps.
- Full braille display support — relies on platform screen reader pass-through.
- Audio description for video — no video content v1.

**Custom Vis accessibility:**

- Orb state announcement in plain language — "Recovery: 78%, increasing," not "Orb state: green-amber-2."
- Equipment-Aware Logger ARIA — "Cable, slab 8, 40 kilograms" on focus. Drag gestures have keyboard equivalents (arrow keys = slab ± 1).
- Connection Orb live region — "Trainer is editing. Last edit by Brienne, 12 seconds ago." Throttled to avoid spam.
- Privacy ∩ accessibility — body measurements + biometric reads NEVER announced via VoiceOver unless user is on the specific surface (avoid passing health info to nearby observers).

### Testing Strategy

**Automated (CI on every PR):**

- `pa11y-ci` on Angular Admin Web. Checks page-level a11y rules. Fails build on AA violations.
- `wcag-contrast` Node script on RN tokens. Reads `tokens.contrast-matrix.json`; fails if any (text, bg) pair drops below 4.5:1.
- ESLint a11y plugin (`eslint-plugin-jsx-a11y` for RN, `@angular-eslint/eslint-plugin-template` for Angular). Catches missing alt text, role mismatch.
- Lighthouse on Admin Web routes — accessibility score ≥ 95 gate.
- Tamagui accessibility lint — built-in dev warnings for missing labels.

**Manual (before each release):**

- Screen-reader testing: VoiceOver (iOS), TalkBack (Android), NVDA (Windows) on Admin Web. Walk core flows: Active Session, Onboarding Questionnaire, Owner Dashboard.
- Keyboard-only navigation on Admin Web — every CTA reachable without mouse. Tab order respects logical reading order.
- Color-blindness simulation via Chrome DevTools (protanopia, deuteranopia, tritanopia). Walk Recovery Orb + ACWR banner + Conflict Toast — confirm legible without color cue.
- Device testing on real hardware: Pixel 6a, Galaxy A54, iPhone 14, iPad mini (v1.1). Network throttled to 3G to validate set-save p95 under degraded conditions.
- Reduce-motion + reduce-transparency: toggle in iOS / Android settings + verify all Vis-custom components degrade.

**User testing (post-MVP, ongoing):**

- Include users with disabilities in beta testing cohorts. Specifically: low-vision (contrast + screen-reader), motor-impaired (thumb-zone + voice fallback).
- India-specific device profiles: Redmi 12, Realme mid-tier, OPPO. India's 3G/4G transition network conditions.
- Trainer cohort testing on Active Session under realistic gym-floor conditions (sweaty hands, noise, phone propped on bench).

### Implementation Guidelines

For Dev agent + future contributors. Consolidates rules from prior steps.

**Responsive development:**

- Mobile-first CSS via Tailwind `min-width` media queries.
- Use relative units (`rem`, `%`, `vw`) over fixed pixels in Admin Web. Mobile RN uses StyleSheet with point-based sizing per platform conventions.
- Test touch targets ≥ 44 × 44 pt with Reactotron / Flipper inspector — failing targets flag in dev console.
- Image / asset optimisation: SVG for icons, WebP for photos, lazy-load below-fold. Avatars: prefer SVG initials over photo when bandwidth-constrained.
- Cold-start budget: ≤ 2.0 s on Pixel 6a / Galaxy A54 — verify via Tamagui Babel plugin telemetry (per step-06).

**Accessibility development:**

- Semantic HTML on Admin Web — `<nav>`, `<main>`, `<article>`, `<section>`, `<header>`, `<footer>`. Tamagui RN components ship correct accessibility roles by default.
- ARIA labels on every interactive element. Custom Vis components: explicit `accessibilityLabel` (RN) / `aria-label` (Angular). Live regions via `cdkAriaLive` + Tamagui equivalent.
- Keyboard navigation via Angular CDK `FocusKeyManager`. Custom logic for Equipment-Aware Logger drag gestures (arrow keys = step ±1; Enter = commit).
- Focus management: on modal open → trap focus inside; on modal close → restore focus to trigger. Angular CDK `FocusTrap` handles this; RN via Tamagui `Dialog` primitive.
- Skip links on Admin Web — first interactive element on page is "Skip to main content."
- High-contrast mode support via `prefers-contrast: more` media query — increases border weights, removes subtle gradients.
- Reduce-motion via `prefers-reduced-motion: reduce` — all orb loops, PR cinematic, Connection Orb pulse, AI ghost slide-in degrade to static. Only enter / exit fades remain.
- Reduce-transparency via `prefers-reduced-transparency: reduce` — Liquid Glass collapses to solid warm-tinted surface; `backdrop-filter` removed entirely.

**Per-feature workflow (already in step-06):**

- 21st-magic MCP scaffold → 5-step adapt-to-Vis-tokens checklist (hex → tokens, shadow stack, brand-ref PNG check, WCAG contrast, light + dark screenshot diff). No merge without all 5.
- Reviewer assesses accessibility as part of code review — required questions: "Does this work with VoiceOver / TalkBack? Does this respect reduce-motion?"
- QA validates on real hardware before each release.

---

<!-- UX design content will be appended sequentially through collaborative workflow steps -->
