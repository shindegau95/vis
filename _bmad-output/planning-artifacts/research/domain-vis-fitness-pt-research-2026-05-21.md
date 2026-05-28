---
stepsCompleted: [1, 2, 3, 4, 5, 6]
inputDocuments:
  - docs/superpowers/specs/2026-05-04-vis-design.md
  - PLAN.md
  - prototype/
workflowType: 'research'
lastStep: 6
status: completed
research_type: 'domain'
research_topic: 'Personal training + progressive-overload science + multi-stakeholder gym operations (trainer ↔ client ↔ staff ↔ owner)'
research_goals: 'Feed Vis PRD + UX + metric model with grounded, citable findings on training science, JTBD personas, and AI-coached competitive landscape (Motra/Train/Bevel class apps). Global scope. Peer-reviewed primary + practitioner secondary sources.'
user_name: 'Gauravprakashshinde'
date: '2026-05-21'
web_research_enabled: true
source_verification: true
---

# Research Report: domain

**Date:** 2026-05-21
**Author:** Gauravprakashshinde
**Research Type:** domain

---

## Research Overview

Domain research for Vis — a 3-app fitness platform (RN client + RN trainer + Angular admin + Spring Boot backend) operating across multiple gym branches. Focus on training science, JTBD personas across four stakeholders (client, trainer, staff, owner), and competitive landscape of AI-coached progressive-overload apps. Findings will feed PRD, UX design, and the metric/data model.

Depth weighting:
- 40% competitive scan (AI-coached PT apps + multi-stakeholder PT-business apps)
- 35% training science + metrics
- 25% personas + JTBD

---

## Domain Research Scope Confirmation

**Research Topic:** Personal training + progressive-overload science + multi-stakeholder gym operations (trainer ↔ client ↔ staff ↔ owner)
**Research Goals:** Feed Vis PRD + UX + metric model. Global scope. Peer-reviewed primary + practitioner secondary sources.

**Domain Research Scope:**
- Industry Analysis — AI-coached lifting apps, PT-SaaS, gym-management category, feature/gap matrix
- Training Science + Metrics — load progression, metric catalog, surfacing strategy
- Personas + JTBD — client/trainer/staff/owner archetypes with functional+emotional+social jobs
- Regulatory + Data — HealthKit, Health Connect, GDPR, India DPDPA, FCM residency
- Technology Trends — auto-regulation, CV form analysis, RIR/VBT, wearables, AI nutrition
- Economic Factors — global market, India context, pricing patterns
- Value Chain — engagement + ops loops, churn + retention drivers

**Research Methodology:**
- All claims verified against current public sources via live web search
- Multi-source validation for critical claims
- Confidence tags for uncertain information
- Inline citations + full reference list at document end

**Output Format:** Companion HTML file at `domain-vis-fitness-pt-research-2026-05-21.html` is the human-readable artifact per project HTML-first documentation rule (CLAUDE.md). This `.md` retains BMad workflow state.

**Scope Confirmed:** 2026-05-21

---

## Industry Analysis

Vis straddles three adjacent markets. None of them captures it alone — the multi-stakeholder lens (client + trainer + staff + owner) is structurally what most competitors are missing.

### Market Size and Valuation (2026 baseline)

| Market | 2026 size (global) | CAGR | Source |
|---|---|---|---|
| Consumer fitness apps | USD 12.12B (2025) → ~13.92B (2026) | 13.4% (2026–2033) | Grand View Research |
| Consumer fitness apps (alt estimate) | ~USD 9.89B by 2030 | 1.75% (2026–2030) | Statista |
| AI Personal Trainer | USD 7.23B (2025) → 8.32B (2026) → 18.74B (2032) | 14.57% | 360iResearch |
| AI Personal Trainer (alt) | USD 16.86B (2025) → 48.79B (2032) | 16.3% | alt market source |
| AI in Fitness & Wellness | — | 19.3% (2026–2035) | InsightAce Analytic |
| Gym management software | USD 2.23B (2026) → 4.02B (2032) | 10.24% | Market Research Future |

**Confidence:** market size figures vary widely across firms (`MEDIUM`). Direction of travel — AI-coached lifting growing 1.4–2× faster than baseline fitness apps — is consistent across all sources (`HIGH`).

**Implication for Vis:** the AI-coached and PT-SaaS segments are the fastest-growing within the broader fitness app market. Vis's trainer-app and AI exercise/macro suggestion (already in spec) sits in the highest-CAGR pocket.

### Market Dynamics and Growth

**Growth drivers (high confidence):**
- 52% of personal trainers already use AI for programming (2026 State of the PT Industry Report via Trainerize blog). Adoption past tipping point.
- Demand for individualized training + nutrition + mental-wellness in one product. Bundled apps outgrow single-purpose ones.
- Post-pandemic hybrid PT model (in-person + remote check-ins via app) — drives need for trainer-side software that mirrors client-side.
- Wearable saturation (Apple Watch, Whoop, Oura, Garmin) creates HRV/sleep/recovery data supply — apps that consume it differentiate.
- Auto-regulation (RIR, e1RM, velocity-based) entering mainstream consumer apps via Bevel, Train Fitness AI, JuggernautAI.

**Growth barriers:**
- iOS-dominant adoption (51.99% of fitness-app revenue) — Android UX often deprioritized. Vis is RN, neutral.
- North America captures 39.82% of revenue — India + APAC remain underserved at premium tier (opportunity for Vis given Mumbai-anchored branches).
- Trainer-side software is a sticky B2B sale; consumer-side apps churn fast.
- Privacy regimes (GDPR, India DPDPA, HIPAA-adjacent state laws in US) raising compliance bar for biometric data.

### Market Structure and Segmentation

**By product category (Vis-relevant slice):**

1. **AI-coached consumer lifting apps** — Bevel, Train Fitness AI, Motra, Fitbod, JuggernautAI, RP Hypertrophy, Boostcamp, Caliber, Strong, Hevy. Solo lifter. Auto-progression + form analysis emerging.
2. **PT-SaaS / coaching platforms** — Trainerize (broadest), TrueCoach (16,000+ coaches), Everfit (200,000+ coaches across 190 countries), MyPTHub, 12REPS, FitBudd, Trainero. Trainer-first; client gets a companion app.
3. **Gym management / club software** — Mindbody, Glofox (ABC), Zen Planner, PushPress, Wodify, Mariana Tek, Hapana, Virtuagym. Staff + owner ops; member-facing is thin.
4. **Hybrid / chain platforms** — Mariana Tek, Hapana, Virtuagym, FitnessForce (India). Closest to Vis's multi-branch + multi-stakeholder ambition but weak on trainer-side coaching depth.

**By revenue contribution (Grand View, 2025):**
- Exercise & weight loss: 53.69% of fitness-app revenue (largest)
- iOS platform: 51.99% revenue share
- Smartphones (vs wearables-primary): 66.70%
- Geographic: North America 39.82%, then Europe, then APAC

**Implication for Vis:** the multi-stakeholder, multi-branch, AI-coached, hybrid-PT segment is structurally underserved — gym-management platforms own the staff/owner story but lack training-science depth; PT-SaaS owns trainer-side but treats branches as a CRM tag; AI-lifting apps own the science but skip the gym institution entirely. Vis is one of the few converging all three.

### Industry Trends and Evolution (2026)

- **AI from feature to default.** Glofox launched AI workout recommendations in 2025; Mindbody integrating with smart equipment for real-time usage. Trainerize, Everfit, TrueCoach all shipped AI programming assistants in 2025–2026.
- **Auto-regulation goes mainstream.** RIR/RPE-based progression, velocity-based training, and e1RM-driven load suggestion moved from elite-strength niche (JuggernautAI, RTS) to consumer apps (Bevel, Train, Motra).
- **Recovery-aware programming.** HRV + sleep + soreness inputs alter same-day session prescriptions. Whoop, Oura, Garmin integrations now table-stakes for premium tier.
- **Computer-vision form check.** Bevel, Train Fitness AI, Sency-powered apps offering rep counting + bar-path + depth analysis from phone camera.
- **Hybrid coaching dominant.** Pure remote ("online PT") and pure in-person are both declining as % share; in-person + remote check-ins is the growth segment — Vis is built for this from day one.
- **Vertical integration into equipment + wearables.** Mindbody × smart-equipment partnerships; Apple Fitness+ × Apple Watch. Software-only platforms must integrate harder.
- **Multi-stakeholder consolidation slow.** Despite the obvious benefit, very few platforms model trainer + client + staff + owner as first-class roles with role-scoped data. This is Vis's white space.

### Competitive Dynamics

- **Market concentration:** Fragmented. Trainerize, TrueCoach, Mindbody, Glofox each have meaningful share in their slice but no one owns the full stack across consumer + trainer + ops.
- **Competitive intensity:** High in PT-SaaS and AI-coached lifting (low switching cost for trainers, even lower for solo lifters). Moderate in gym management (high switching cost — onboarding all members, payments, schedules).
- **Barriers to entry:**
  - Low: building a single-stakeholder lifting tracker (commodity).
  - Medium: AI programming layer (LLM costs falling; expertise still differentiating).
  - High: full multi-branch + multi-role + payments + reminders + AI + wearables, in one product. This is Vis's moat if executed.
- **Innovation pressure:** Quarterly model upgrades expected. Consumer apps ship AI features every 6–12 weeks. PT-SaaS slower (B2B).

**Vis positioning hypothesis (to validate in Step 3 competitive deep-dive):** Vis is positioned closest to **Trainerize + Mariana Tek + a slice of Bevel**, but for an under-served segment (multi-branch boutique gyms with on-site PT + light AI assistance + India-localized). Differentiation lives in three places: (1) trainer ↔ client real-time set logging during in-person session, (2) role-scoped multi-branch ops, (3) cinematic on-brand UX (your prototype's orb language) that consumer-grade design polish — most gym-mgmt software is visually dated.

### Sources

- [Grand View Research — Fitness Apps Market](https://www.grandviewresearch.com/industry-analysis/fitness-app-market)
- [Statista — Fitness Apps Worldwide](https://www.statista.com/outlook/hmo/digital-health/digital-fitness-well-being/health-wellness-coaching/fitness-apps/worldwide)
- [360iResearch — AI Personal Trainer Market](https://www.360iresearch.com/library/intelligence/ai-personal-trainer)
- [InsightAce Analytic — AI in Fitness and Wellness Market](https://www.insightaceanalytic.com/report/ai-in-fitness-and-wellness-market/2744)
- [Market Research Future — Gym Management Software Market](https://www.marketresearchfuture.com/reports/gym-management-software-market-26474)
- [Trainerize — 2026 State of the PT Industry / AI for PTs](https://www.trainerize.com/blog/ai-for-personal-trainers/)
- [Trainerize — ABC Trainerize vs Everfit vs TrueCoach vs MyPTHub](https://www.trainerize.com/blog/abc-trainerize-vs-everfit-vs-truecoach-vs-mypthub/)
- [ptwill.com — 12REPS vs TrueCoach vs Everfit vs MyPTHub vs Trainerize](https://ptwill.com/blog/12reps-vs-truecoach-vs-everfit-vs-mypthub-vs-trainerize-which-fitness-app-is-actually-worth-your-money-in-2026/)

---

## Competitive Landscape

**Note:** Your initial brief named "Motra/Train/Bevel" as references. Confirmed via App Store: **Train Fitness was rebranded to Motra in 2025**. Same product. So treating as two distinct competitors: **Motra (= Train Fitness)** and **Bevel**.

### Three competitive clusters

Vis must beat — or absorb the best of — three clusters that today live as separate categories.

#### Cluster A — AI-Coached Consumer Lifting (solo lifter)

| App | Differentiation | Pricing | Vis-relevant strength | Weakness Vis can exploit |
|---|---|---|---|---|
| **Motra** (ex–Train Fitness) | Neural Kinetic Profiling: auto-detect 470+ exercises from Apple Watch motion. AI Coach chat. ChatGPT/Claude data export. | Free + premium | Strongest auto-tracking IP. Recovery-aware routine generation. | iOS-Watch only. Leg exercises poorly detected (wrist-only). Solo lifter — no trainer/gym layer. |
| **Bevel** | Bevel Intelligence 3.0 (rebuilt 2026-05): proactive AI coach with web search, chart gen, training plans + 700 exercises, biological-age tracking. | Core free; AI coaching paid | Best AI-coach conversational UX. Holistic (recovery + nutrition + workouts). | Generalist health, not strength-focused. No trainer↔client model. |
| **Fitbod** | Workout generator from 1,600+ exercise library. Auto progressive overload, RIR + Max Effort Days, recovery-driven. | $15.99/mo, $95.99/yr | Strongest auto-programming for hands-off lifter. | Solo only. Generic visuals. No coach integration. |
| **JuggernautAI** | Elite-strength autoregulation: RPE/RIR drives load + volume + de-load. Readiness inputs (sleep/soreness/motivation). | $34.99/mo, $349.99/yr | Best autoregulation engine. Citable scientific lineage (Chad Wesley Smith, Quinn Henoch). | Powerlifter-only. UI dated. No trainer dashboard. |
| **RP Hypertrophy** | Renaissance Periodization meso-cycle volume progression. RIR + post-session feedback (pump/soreness/joint pain) → next-week adjustment. | ~$24.99/mo | Strongest hypertrophy science. Schoenfeld/Israetel lineage. | Hypertrophy-only. No real-time logging during in-person PT. |
| **Boostcamp** | 70+ pre-built programs (PHUL, PHAT, nSuns, Reddit PPL). RPE/1RM%/volume tracking. | Free + premium | Best program library. | Library, not coaching engine. Solo. |
| **Hevy** | Lifting log + social + Hevy Trainer (B2B). Programs with progressive overload baked in. Hits-target → load up, miss → hold/drop. | $2.99/mo, $74.99 lifetime | Cheapest. Strong social loop. | 400+ exercises (small). Self-directed. No real PT layer. |
| **Strong** | Frictionless logger. No AI. | One-time / freemium | Fastest log UX. | Blank canvas — no progression intelligence. |

**Pattern:** Cluster A nails science + auto-progression + recovery, fails at trainer↔client + multi-user.

#### Cluster B — PT-SaaS / Online Coaching (trainer ↔ remote client)

| App | Differentiation | Pricing | Vis-relevant strength | Weakness Vis can exploit |
|---|---|---|---|---|
| **Trainerize** (ABC) | Modern 2026 client app: unified calendar (workouts + habits + classes carousel). Real-time chat with typing indicators. Auto-tags clients (low activity, new PB, etc.) on trainer dashboard. Custom-branded white-label client app. | Tiered SaaS, ~$5–$250/mo trainer | Closest analog to Vis trainer↔client architecture. Recently shipped UX overhaul. | Designed for **remote** PT. No real-time in-person set-by-set collab. Branches = tag/CRM, not first-class. No gym-ops/admin role. |
| **TrueCoach** (Xplor) | 16,000+ coaches. Programming + messaging + video form review. | Tiered SaaS | Strong async coaching UX. | Async-first. No in-person session screen. Solo trainer brand — no multi-branch hierarchy. |
| **Everfit** | 200,000+ coaches, 190 countries. Workouts + meal plans + habits + community + automation. | Tiered | Broadest scope (workout + nutrition + habit + community). | Same gap: remote-first, no multi-stakeholder branch model. |
| **MyPTHub** | Trainer-branded client app. Programming + nutrition + messaging. | Tiered | Cheaper white-label. | Same gaps. |
| **Caliber** | Premium 1:1 strength-focused remote coaching, science-led. | $50–$300+/mo | Strong science framing + 1:1 attention. | High price. Wearable HRV/sleep **not in coaching loop** (self-report). Limited beyond strength. |
| **Future** | Generalist premium 1:1 remote PT. | ~$149+/mo | Strong coach diversity. | Generic vs strength-focus. Frequent communication only — no in-session collab. |

**Pattern:** Cluster B nails trainer↔client async loop, fails at (a) real-time in-person session collab, (b) multi-branch role hierarchy, (c) automated biometric integration into the programming loop. **Vis's spec already addresses all three.**

#### Cluster C — Multi-Location Gym/Boutique Ops (staff ↔ owner)

| App | Differentiation | Pricing | Vis-relevant strength | Weakness Vis can exploit |
|---|---|---|---|---|
| **Mariana Tek** (Xplor) | Multi-location boutique-fitness ops: scheduling, billing, attendance, POS with location-specific pricing, role-based access. **Launched Appointments 2026-05** to add 1:1 PT services. | Enterprise SaaS | Closest structural comp for Vis multi-branch admin. Just moved into 1:1 PT — validates Vis thesis. | Member-facing app thin. No training-science depth. Visually corporate. No AI programming. |
| **Hapana** | Multi-site + franchise growth focus. One software across many locations. | Enterprise SaaS | Strong franchise/chain ops. | Same: ops-first, training-thin. |
| **Mindbody** | Largest. Smart-equipment integrations (2025). | Enterprise SaaS | Brand + integration depth. | Dated UX. Generalist (wellness, yoga, salon). Heavyweight. |
| **Glofox** (ABC) | AI workout recommendations (shipped 2025). Multi-location. | Enterprise SaaS | First ops platform with AI workouts. | Still ops-led — AI is bolt-on. |
| **Zen Planner / PushPress / Wodify** | CrossFit / boutique vertical. | Tiered | Vertical specialization. | Narrow vertical. |
| **Virtuagym** | Ops + member app + content. | Tiered | Bundle attempt. | Spread thin across many features. None great. |
| **FitnessForce** | India-focused gym mgmt. | Local | India context, payments. | UX dated. No training-science. No AI. |

**Pattern:** Cluster C nails ops + billing + multi-branch, fails at training depth + member-facing UX. Mariana Tek's 2026-05 Appointments launch is a strong signal that the convergence Vis bets on is real and starting.

### Vis White-Space Matrix

Six capabilities × three clusters. Green = strong, yellow = partial, red = absent. Where the column reads red, that's where Vis must out-execute.

| Capability | Cluster A (AI-coached) | Cluster B (PT-SaaS) | Cluster C (Multi-branch ops) | Vis must own |
|---|---|---|---|---|
| Auto progressive overload (RIR/e1RM/RPE) | ✅ strong | 🟡 trainer-driven | ❌ | ✅ |
| Recovery / HRV / readiness-driven programming | ✅ Motra/Bevel/JAI | ❌ Caliber gap explicit | ❌ | ✅ |
| Trainer ↔ client async loop | ❌ | ✅ strong | ❌ | ✅ |
| **Trainer ↔ client real-time in-person session collab** | ❌ | ❌ | ❌ | **✅ unique to Vis** |
| Multi-branch + role-scoped data (staff/owner/trainer/client) | ❌ | 🟡 (single-org) | ✅ Mariana/Hapana | ✅ |
| Cinematic on-brand member UX (consumer-grade polish) | ✅ Bevel/Motra | 🟡 Trainerize 2026 redesign | ❌ dated | ✅ (your prototype orb language) |

**Two cells are red across all three clusters:** (1) real-time in-person session collab, (2) cinematic UX in a multi-stakeholder product. **These are Vis's defensible differentiators.**

### Business Models and Value Propositions

| Model | Examples | Notes for Vis |
|---|---|---|
| Pure consumer subscription | Motra, Bevel, Fitbod, Hevy, JuggernautAI, RP, Boostcamp | $3–$35/mo. Low ARPU but volume. India price-sensitivity matters. |
| PT-SaaS per-trainer subscription | Trainerize, TrueCoach, Everfit, MyPTHub | $5–$250/mo per trainer. B2B sticky. |
| Premium 1:1 coaching | Caliber, Future | $50–$300/mo per client. Concierge.  |
| Enterprise multi-location ops | Mariana Tek, Hapana, Mindbody, Glofox | Annual contract. High TCV. Slow sales cycle. |
| Hybrid (Vis hypothesis) | — | Gym pays for ops + trainer seats; client app free as retention layer. Validates against Mariana Tek + Trainerize blend. |

### Entry Barriers Vis Faces

- **High:** trainer adoption (incumbents have 10K–200K coaches; trust + workflows established). Mitigation: own the gym institution first, trainers come bundled with gym contract.
- **Medium:** AI programming credibility (need named scientific frame: Schoenfeld/Helms/Israetel/RP). Mitigation: open methodology, citable.
- **Medium:** wearable integration breadth (Whoop, Oura, Garmin, Polar, Apple Watch, Health Connect). Mitigation: prioritize Apple HealthKit + Health Connect first; defer brand-specific.
- **Low:** consumer logging UX (Strong/Hevy already commoditized this).
- **Lowering:** AI coaching cost (LLM costs falling — your AI exercise/macro layer cheaper to ship than 2 years ago).

### Switching Costs

- Cluster A (consumer): low — lifters switch logging apps in a session.
- Cluster B (PT-SaaS): medium-high — trainer rebuilds programs, retrains clients on a new UI. Vis's trainer-app must beat Trainerize on day-one switching cost (CSV import? video-import?).
- Cluster C (ops): very high — payment, schedule, member data all live there. Vis admin-web entry sells against pain of migrating, not features.

### Ecosystem and Partnerships (signals for Vis roadmap)

- **Wearables:** Apple HealthKit + Google Health Connect (Vis spec already includes — read-only). Tier-2: Whoop (HRV gold standard), Oura, Garmin.
- **Payments (India):** UPI, Razorpay, Stripe India — your spec keeps payments in-person, but member-facing payment surfaces will come post-v1.
- **AI:** Claude (Anthropic) is the obvious choice given the project tooling already in use. Per-user prompt caching for cost.
- **Equipment:** future — Mariana Tek × Technogym, Mindbody × smart equipment. Long-tail.

### Sources

- [Motra (App Store)](https://apps.apple.com/us/app/motra-ai-workout-fitness-coach/id1548577496)
- [Motra (motra.com)](https://www.motra.com/)
- [Train Fitness AI (trainfitness.ai)](https://trainfitness.ai/)
- [Bevel — autonomous.ai review](https://www.autonomous.ai/ourblog/bevel-app-review)
- [Bevel 3 — Gadgets and Wearables](https://gadgetsandwearables.com/2026/05/16/bevel-3/)
- [Bevel official](https://www.bevel.health/)
- [Fitbod vs Hevy vs Strong — SensAI](https://www.sensai.fit/blog/hevy-vs-strong-vs-fitbod)
- [Findyouredge — Best Strength Apps 2026](https://www.findyouredge.app/news/best-strength-training-apps-2026)
- [JuggernautAI feature breakdown](https://ideausher.com/blog/fitness-app-development-like-juggernautai/)
- [RP Hypertrophy review — dr-muscle.com](https://dr-muscle.com/rp-hypertrophy-app-review/)
- [Trainerize — 2026 Modern App Update](https://www.trainerize.com/blog/trainerize-product-update-new-modern-app-experience/)
- [Mariana Tek — Multi-Location](https://www.marianatek.com/features/multi-location/)
- [Mariana Tek Appointments 2026-05](https://xplor.com/press/marianatek-appointments/)
- [Hapana — Multi-Site](https://www.hapana.com/platform/multiple-locations)
- [Caliber review — Fitness Drum](https://fitnessdrum.com/caliber-app-review/)
- [Caliber review — BarBend](https://barbend.com/caliber-fitness-app-review/)

---

## Regulatory Focus

Four regulatory layers materially shape Vis design. Three are about data, one is about credentials.

### Layer 1 — Apple HealthKit (iOS)

| Aspect | Rule | Vis implication |
|---|---|---|
| Read vs write | Separate authorization per data type, per direction. App granted write → also gets read for what it wrote. | Vis Client App needs read-only authorization for heart rate, HRV, sleep, active energy, workouts, body mass. No write needed v1. |
| User control | Per-data-type toggle in iOS Privacy settings. Revocable anytime. | UI must degrade gracefully when permission revoked. No silent failure. |
| Encryption at rest | Stored in Data Protection class "Protected Unless Open". Released 10 minutes after device lock. | Backend never receives raw HealthKit data — already in spec. |
| Advertising | **Apps cannot use HealthKit data for advertising.** | Honored — Vis has no ads. Document in privacy policy regardless. |
| Privacy policy | Mandatory before HealthKit auth prompt. Must specify exactly what data and why. | Privacy policy must list each HealthKit read scope + purpose. |
| App Review | Apps that misuse health data are rejected. | First submission must include sample data flow doc for reviewer. |

**Confidence:** `HIGH` — Apple official sources.

### Layer 2 — Google Health Connect (Android)

| Aspect | Rule | Vis implication |
|---|---|---|
| Google Fit API sunset | Google Fit APIs supported until end of 2026, then deprecated. Health Connect replaces. | Vis Android Client App must integrate Health Connect, not Google Fit. |
| Permissions model | Granular per-data-type (e.g., `READ_HEART_RATE`, `READ_SLEEP`, `READ_STEPS`). No catch-all. | Declare only what is actually used in-app. |
| History window | Default reads 30 days before grant. Deeper history requires `PERMISSION_READ_HEALTH_DATA_HISTORY`. | For long-term trend graphs (recovery curve, sleep over months), Vis needs the history permission. |
| Platform | Part of Android Framework from API 34 (Android 14+). No additional setup. | Min SDK 34 cleanest; for older Android, Health Connect installable as separate app. Decide min-SDK during Phase 0. |
| Play Store policy | Apps must request only permissions needed for user-facing features. | Strict scoping check before Play Store submission. |

**Confidence:** `HIGH` — Android Developers official.

### Layer 3 — GDPR (EU users, even if Vis is India-anchored)

| Aspect | Rule | Vis implication |
|---|---|---|
| Health data class | Article 9 "special category". Wearable HR/sleep/HRV all qualify. | All biometric flows are special-category. |
| Legal basis | **Explicit consent** is the only realistic basis for commercial fitness apps. | Dedicated consent step, not bundled with ToS. |
| Consent UX | Specific, informed, freely given, distinct from other consents. Pre-ticked is invalid (Planet49 CJEU). | Vis onboarding needs an isolated "Health data" consent screen with clear data list + purpose per item. |
| Context-sensitive | EDPB 2023 guidance: same data treated differently by purpose. Casual HR vs cardiac monitor diverge. | Vis must self-classify each data use as recreational fitness, not diagnostic. Document in DPIA. |
| Data residency | EU subject data should stay in EU or use SCCs / adequacy decision. | If EU users in scope v1, host backend in EU region (Cloud Run europe-west*) or implement transfer mechanism. Tentative answer: defer EU launch to v2, India + global non-EU first. |
| DPIA | Required for systematic processing of health data. | Vis must produce a DPIA pre-launch if EU users in scope. |

**Confidence:** `HIGH`.

### Layer 4 — India DPDPA 2023 + DPDP Rules 2025

| Aspect | Rule | Vis implication |
|---|---|---|
| Classification | Biometric + health data = sensitive personal data. | Vis members' wearable + workout data both qualify. |
| Consent | Specific, **not bundled** with other permissions. Informed, with specified purpose + scope + duration. | India onboarding mirrors GDPR explicit-consent UX. One screen per data domain. |
| Security | Encryption at rest + transit. Access controls. Continuous monitoring for unauthorized access. | TLS 1.3 (already standard via Cloud Run); at-rest via Neon PG default. Audit logging required. |
| Third-party sharing | Sharing with insurance partners requires separate consent. | Vis must NOT silently share data with any partner without separate consent flow. v1: no third-party sharing. |
| Cross-border transfer | Government may restrict transfer to certain jurisdictions. | If EU/US infra used, monitor MeitY notifications. Indian DCs preferred long-term. |
| DPO | Required for Significant Data Fiduciaries (large platforms). Must be India-based. | Below threshold initially. Plan DPO appointment when user count crosses ~50k or upon designation. |
| Penalty | Up to INR 250 crore (~USD 30M) per breach. | Material risk — security spend is justified. |
| Timeline | Phased rollout: DPB immediate, consent manager framework after 12 months, broader compliance 18 months from Aug 2023. **As of 2026-05, full DPDPA compliance is required.** | Already in force. Cannot defer. |

**Confidence:** `HIGH`.

### Layer 5 — Sectoral / Industry Standards (not statutory)

| Standard | Relevance to Vis |
|---|---|
| ACSM / NSCA / NASM trainer certifications | Vis Trainer App should surface trainer certification fields (cert body + cert ID + expiry) and a verified badge. Increases client trust. |
| HIPAA (US) | Out of scope unless Vis acts as a Business Associate for a healthcare provider. Not applicable v1. |
| App Store + Play Store health/fitness guidelines | Apple App Store §5.1.3, §1.4 + Play Store Health Apps policy. Both prohibit unsupported medical claims, require privacy disclosures, restrict ads on health data. |
| ISO 27001 | Not required; useful signal when selling to enterprise gym chains later. |
| WCAG 2.2 AA | Accessibility — RN apps + Angular admin should target AA for screen readers + contrast. |

### Implementation checklist for Vis (drop into PRD)

- [ ] iOS: list per-data-type HealthKit read scopes; usage descriptions in `Info.plist`; sample privacy policy section per scope
- [ ] Android: declare Health Connect permissions per data type only; decide on `PERMISSION_READ_HEALTH_DATA_HISTORY`; set min SDK 34 (or fallback strategy)
- [ ] Onboarding: dedicated Health Data Consent screen, separate from ToS, per data-domain toggle (HR, sleep, HRV, steps, workouts, weight)
- [ ] Backend: confirm spec line — wearable biometric data is **read-only, on-device, not persisted to backend** (already in spec; lock it in)
- [ ] DPDPA: TLS 1.3, encryption at rest, audit logs, breach notification procedure, in-app data export + delete flows
- [ ] No third-party data sharing v1; if added later, separate consent flow
- [ ] Trainer Profile: certification fields + verified badge
- [ ] EU users: defer to v2; if needed v1, host EU region + DPIA
- [ ] DPO: appoint when user threshold approached or upon designation
- [ ] Privacy policy + DPIA template owned by founder/legal; version-controlled in repo

### Vis spec compliance check (against current spec)

Spec line: *"Apple HealthKit (iOS) + Google Health Connect (Android) — read-only, not stored in backend"* — this is the single most compliance-friendly architectural choice in the spec. It collapses a huge GDPR/DPDPA surface because:
- No special-category data ever leaves the device → no transfer compliance burden
- No backend breach can leak biometric data
- DPIA scope shrinks dramatically

**Recommendation:** keep this line non-negotiable through v1. If a product feature genuinely needs server-side biometric data later (e.g., trainer-side dashboard showing client HRV trend), introduce it as an **explicit opt-in** with a fresh consent screen and document the change in the privacy policy version log.

### Sources

- [Apple — Protecting access to user's health data](https://support.apple.com/guide/security/protecting-access-to-users-health-data-sec88be9900f/web)
- [Apple Developer — Protecting user privacy (HealthKit)](https://developer.apple.com/documentation/healthkit/protecting-user-privacy)
- [Apple Developer — Authorizing access to health data](https://developer.apple.com/documentation/healthkit/authorizing-access-to-health-data)
- [Apple Legal — Health App & Privacy](https://www.apple.com/legal/privacy/data/en/health-app/)
- [Android Developers — Health Connect Permissions and data access](https://developer.android.com/health-and-fitness/health-connect/ui/permissions)
- [Android Developers — Publish your health app on Google Play](https://developer.android.com/health-and-fitness/health-connect/publish)
- [Android Developers — Health Connect Get Started](https://developer.android.com/health-and-fitness/health-connect/get-started)
- [Momentum — GDPR Consent Requirements for Health Data](https://www.themomentum.ai/blog/gdpr-consent-requirements-health-data)
- [GDPR Advisor — Compliance for Fitness Apps](https://www.gdpr-advisor.com/gdpr-compliance-for-fitness-apps-safeguarding-personal-health-information/)
- [EY India — Decoding DPDPA 2023 and DPDP Rules 2025](https://www.ey.com/en_in/insights/cybersecurity/decoding-the-digital-personal-data-protection-act-2023)
- [KS&K — Regulation of Biometric Data under DPDPA](https://ksandk.com/data-protection-and-data-privacy/regulation-of-biometric-data-under-the-dpdp-act/)
- [MeitY — DPDPA 2023 text](https://www.meity.gov.in/static/uploads/2024/06/2bf1f0e9f04e6fb4f8fef35e82c42aa5.pdf)
- [Biometric Update — India notifies sweeping DPDP rules](https://www.biometricupdate.com/202511/india-notifies-its-sweeping-digital-personal-data-protection-rules)

---

## Technology Trends, Training-Science Metrics, and JTBD Personas

Vis's product surface depends on which metrics get computed server-side, which stay on-device, and which the trainer sees in real time. This section is the technical + behavioural foundation for those choices.

### Part A — Training-Science Metric Catalog

Six metric families. For each: definition, formula, evidence base, UX surfacing tier (Surface / Inform / Hide), and where it lives in Vis.

#### A1. Volume metrics

| Metric | Definition | Formula | Evidence | Vis tier | Where |
|---|---|---|---|---|---|
| **Set count per muscle/week** | # of working sets per muscle group per week (RIR ≤ 3) | sum across sessions | Schoenfeld 2017 meta + 2024 update: ~0.24% hypertrophy gain per added set at 12.25 weekly average. Diminishing returns above ~20 sets/muscle/wk; inverted-U at extreme volumes. | **Surface** (primary progress signal in client-app) | Backend |
| **Tonnage** | Total weight × reps × sets per session | `Σ (load × reps)` | Useful for absolute load tracking; weak hypertrophy predictor on its own. | **Inform** (secondary stat) | Backend |
| **Volume load** | Same as tonnage, scoped per muscle/week | `Σ (load × reps)` filtered | Better than raw tonnage for muscle-level progression. | **Inform** | Backend |
| **Effective reps** | Reps performed at ≥ 3–5 RIR | derived from RIR + reps | Practical hypertrophy framework (Helms / SBS). | **Hide** v1; surface for advanced toggle | Backend |

#### A2. Intensity metrics

| Metric | Definition | Formula | Evidence | Vis tier | Where |
|---|---|---|---|---|---|
| **1RM** | True one-rep max — single lift to failure | direct lift | Gold standard but high CNS cost, injury risk. Rarely tested. | **Hide** unless user logs manually | Backend |
| **e1RM (estimated 1RM)** | Calculated from sub-maximal set | **Epley:** `w × (1 + 0.0333 × r)`<br>**Brzycki:** `w / (1.0278 − 0.0278 × r)` | Both ±5–10% accuracy; most accurate at 3–6 reps. Epley better for 2–5 reps, Brzycki better for 6–10. **Recommendation: use Epley for r ≤ 5, Brzycki for r 6–10, average both at r=5 boundary.** | **Surface** (per-exercise PR tracking) | Backend |
| **% of 1RM** | Working weight ÷ e1RM | `load / e1RM × 100` | Standard intensity prescription tool. | **Inform** | Backend |
| **RPE (0–10)** | Rate of Perceived Exertion | self-report | Subjective; strong correlation with bar velocity + RIR in experienced lifters. | **Surface** (per-set input) | Backend |
| **RIR (Reps in Reserve)** | Reps left in tank at set end | self-report | 2024 evidence: RIR-based 1–2 RIR matches failure training for hypertrophy (Schoenfeld); less accurate in untrained + high-rep sets. | **Surface** (per-set input) | Backend |
| **Bar velocity (VBT)** | Concentric mean velocity | hardware (LPT) or phone CV (Metric VBT, OVR) | Network meta-analysis 2025: APRE 93% / RPE 67% / VBT 27% / %1RM 13% effectiveness ranking for max strength. VBT is differentiator for elite tier, overkill for general fitness. | **Hide** v1; **defer to v2 premium tier** | Backend |

#### A3. Recovery + Readiness metrics

| Metric | Definition | Source | Evidence | Vis tier | Where |
|---|---|---|---|---|---|
| **HRV (RMSSD)** | Heart rate variability, morning reading | HealthKit / Health Connect / Whoop / Oura | Strong autonomic-recovery proxy. Daily delta vs 7-day average is more useful than absolute value. | **Surface** (recovery orb) | On-device read |
| **Resting HR** | Lowest HR over 5 min | HealthKit / Health Connect | Trend matters more than absolute. | **Inform** | On-device read |
| **Sleep duration + stages** | Total + REM + Deep | HealthKit / Health Connect | Sleep < 6h ⇒ measurable strength + RPE deficit next day. | **Surface** | On-device read |
| **Subjective readiness (0–10)** | Self-report on energy, soreness, motivation | manual entry | JuggernautAI / RP use it; cheap and effective. | **Surface** (one-tap pre-session) | Backend |
| **DOMS / soreness** | Per-muscle 0–3 | manual entry | Drives next-session exercise selection. | **Inform** | Backend |

#### A4. Training-load metrics (workload management)

| Metric | Definition | Formula | Evidence | Vis tier | Where |
|---|---|---|---|---|---|
| **sRPE (Session RPE)** | session-level effort × duration | `RPE × minutes` (Foster method) | Validated training-load proxy; used by IOC consensus 2016 ACWR. | **Inform** | Backend |
| **ACWR** | Acute Chronic Workload Ratio | `7-day load avg / 28-day load avg` | IOC 2016 consensus: ACWR 0.80–1.30 → lower injury risk. ACWR > 2.0 → higher injury risk. 2024 critiques: methodology debated, still useful as soft signal. | **Inform** (trainer dashboard amber alert at > 1.5) | Backend |
| **Monotony + Strain** | sRPE std-dev variants | Foster | Trainer-side overtraining flag. | **Hide** v1; trainer-app v2 | Backend |

#### A5. Body composition + progress metrics

| Metric | Definition | Source | Vis tier |
|---|---|---|---|
| **Body weight** | scale | HealthKit / Health Connect / manual | Surface |
| **Body fat %** | impedance / DEXA / manual | manual / HealthKit | Inform |
| **Lean mass est.** | derived | derived | Inform |
| **Photos** | weekly check-in | manual upload | Surface (private to client + trainer) |
| **Circumference** | tape measurements | manual | Inform |

#### A6. Progressive overload signal — what Vis must actually compute

The promise of the category: "is this week harder than last?". Combine into a single **Progression Index per exercise**:

```
ProgressionIndex = w_e1rm × Δe1RM + w_vol × ΔVolume + w_quality × ΔRPE_efficiency
```

Where `RPE_efficiency = same load × same reps achieved at lower RPE`. Three knobs the trainer can weight per client goal (strength / hypertrophy / endurance).

**This single derived metric is the surfaced output of all the per-set inputs above.** It is what powers the recovery-orb-style "you progressed today" feedback that the prototype hints at.

### Part B — Technology Trends (2024–2026)

#### B1. Auto-regulation algorithms going mainstream

Five years ago RIR/RPE was elite-strength jargon. Network meta-analysis 2025 ranks APRE (Auto-Regulating Progressive Resistance) at 93%, RPE-based at 67%, VBT at 27%, fixed %1RM at 13% for max-strength outcomes. Consumer apps (Bevel, Motra, Fitbod) shipped RIR/RPE prompts in 2024–2025. **Vis must include RPE + RIR as first-class per-set inputs.** Trainer-app gets a one-tap RIR slider during in-person session; client-app post-set.

#### B2. Computer-vision rep counting + form analysis

Metric VBT uses phone-mounted CV at 60fps to track bar velocity across exercises. OVR Velocity, Sency-powered apps doing form check via phone camera. **Cost trend:** model size shrinking, latency reaching realtime on iPhone 13+ class hardware. **Vis v1 recommendation:** skip CV. **Vis v2:** ship CV rep-counting for compound lifts (squat, bench, deadlift, OHP) — high value, low SKU. Vis's trainer co-presence reduces the marginal value (trainer can count) but client-side solo sessions benefit.

#### B3. Smartwatch-only auto-tracking

Motra (Neural Kinetic Profiling) tracks 470+ exercises from Apple Watch wrist motion. Caveats: leg exercises underdetected, post-set confirmation needed. **Vis recommendation:** integrate as Tier-2 (post-v1) for solo client-app users; Tier-1 stays manual entry for trainer-led sessions where logging is the trainer's job and accuracy matters most.

#### B4. Recovery-driven programming

Bevel, Motra, JuggernautAI all consume HRV + sleep + readiness to alter same-day prescription. **Vis recommendation:** v1 surfaces HRV + sleep + readiness on the **recovery orb** (already in prototype), uses them to shade trainer's next-session intensity recommendation, but does NOT auto-override the trainer-prescribed program. Auto-override is v2 (after trust is established).

#### B5. Wearable integration strategy

- **v1:** Apple HealthKit + Google Health Connect read-only (spec). HR, HRV, sleep, active energy, workouts, body mass.
- **v2 candidates:** Whoop (best HRV; OAuth API), Oura (sleep + readiness), Garmin (multi-sport athletes), Polar.
- **Not in scope:** writing data back to wearable platforms.

#### B6. AI exercise + macro generation

Spec already includes AI exercise suggestion + macro generation. Trend confirms: 52% of trainers use AI for programming (Trainerize 2026). Recommendation:
- **Stack:** Claude (Anthropic) — already in project tooling. Use **prompt caching** for system prompts (~90% cost reduction on cached prefix).
- **Pattern:** Client-app → Backend → Claude. Never call from client direct (API key + cost control + audit log).
- **Prompts version-controlled** in repo (`prototype/prompts/` or backend resources).
- **Outputs typed** (Pydantic / Zod-equivalent in Java: records + Jackson). Reject untyped LLM output.

#### B7. Real-time collaboration tech

Spec flags this as not-yet-decided (polling vs WebSocket). **Recommendation:** **WebSocket for in-session set logging** (trainer + client both editing the same session), **polling-fallback** for reliability. Spring Boot has good WebSocket support; RN side via `react-native-websocket` or Socket.IO. Last-write-wins per set row (already in spec) is correct for v1 — CRDT is overkill.

#### B8. India tech context

- **Network:** 4G+ widely available; Tier-1 cities have 5G. Design for ~50ms RTT to Mumbai region, ~200ms to US/EU regions. Cloud Run asia-south1 (Mumbai) preferred.
- **Devices:** Android dominates (~95%). iOS ~5% but skews to higher-income gym-goer demographic. Build both, don't optimize iOS-first.
- **Payments:** UPI + Razorpay native; Stripe India for cards. Spec keeps payments in-person v1 — defer.

### Part C — JTBD Personas (compact)

JTBD framing: "When I'm in [situation], I want to [motivation], so I can [outcome]." This is what Vis hires to do for each user. Personas demographic-context, jobs functional-context.

#### Client Personas (4 archetypes)

**1. The Beginner (Aarav, 26, recently joined gym after 5 years sedentary)**
- *When* I show up not knowing what to do today, *I want* the trainer to tell me exactly what + how, *so I can* feel productive instead of lost.
- **Functional job:** know what to do next.
- **Emotional job:** feel competent, not embarrassed.
- **Social job:** signal "I belong here" to other gym members.
- **Hire Vis to:** show today's session pre-loaded by trainer; show each exercise with form video; track that I came in, even if I did less than planned.
- **Anti-job (do NOT do):** overwhelm me with HRV / RPE / e1RM jargon week one.

**2. The Recomposition Member (Priya, 33, knows the basics, wants visible change)**
- *When* I'm 4 weeks in and the scale hasn't moved, *I want* to see that the work is still working, *so I can* keep going.
- **Functional job:** know if I'm progressing on multiple dimensions (strength, body comp, photos, energy).
- **Emotional job:** trust that progress is non-linear.
- **Social job:** share milestones (selectively) with trainer.
- **Hire Vis to:** progression index per exercise + weekly trend graph + check-in photo timeline + recovery orb showing readiness improving.
- **Anti-job:** make me feel bad on a "low day".

**3. The Strength Athlete (Rohit, 29, intermediate lifter targeting 200kg squat)**
- *When* I'm in a 12-week peaking block, *I want* to see RIR/RPE per top set + e1RM trajectory, *so I can* know if I'm on schedule.
- **Functional job:** autoregulate based on readiness; protect joints; hit peak day.
- **Emotional job:** belief that this cycle will PR.
- **Social job:** credibility with trainer (talk shop on percentages).
- **Hire Vis to:** show e1RM per main lift over 12 weeks; show ACWR trend; flag readiness drops; let me self-adjust load via RIR if trainer not present.
- **Anti-job:** simplify so much I lose the data.

**4. The Longevity / Health Member (Mrs. Kapoor, 54, doctor-referred for strength + mobility)**
- *When* I think about why I'm here, *I want* to see that I'm getting stronger and moving better, *so I can* age well.
- **Functional job:** consistency, not maximization. Visible mobility/strength benchmarks.
- **Emotional job:** dignity. Not feel like a beginner among 25-year-olds.
- **Social job:** show family I'm taking care of myself.
- **Hire Vis to:** simple "you came X times this week"; sit-to-stand reps; balance + mobility scores; HRV trend.
- **Anti-job:** gamify with badges that infantilize.

#### Trainer Personas (3 archetypes)

**1. The In-Gym PT (Karan, 31, full-time at one branch)**
- *When* I'm running 8 sessions a day across 8 clients, *I want* to switch context fast and log accurately, *so I can* be present in the session, not buried in the phone.
- **Functional job:** fast set logging on the trainer-app during the session; pre-load tomorrow's session in 60s.
- **Emotional job:** look professional, not phone-zombie.
- **Social job:** client sees me using "their" tech, builds credibility.
- **Hire Vis to:** one-tap set logging; voice input for RIR/RPE; client list with today's sessions on top; quick exercise swap if equipment busy.
- **Anti-job:** force me to type during a set.

**2. The Online / Hybrid PT (Anjali, 27, mix of in-gym + remote clients)**
- *When* my remote client logs a session, *I want* to be auto-tagged if they hit a PR or skipped sessions, *so I can* follow up at the right moment.
- **Functional job:** scale to 30+ clients without dropping balls.
- **Emotional job:** be the "in their corner" coach, not generic.
- **Social job:** be retainable — clients refer me.
- **Hire Vis to:** auto-tag clients (PR / low activity / no response); messaging w/ video form-check; program builder w/ AI assist; client check-in tracker.
- **Anti-job:** waste my time on duplicate UI for the same data.

**3. The Lead / Master Trainer (Vikram, 38, mentors junior PTs at the branch)**
- *When* I review junior PTs' programming, *I want* to see what they prescribed and why, *so I can* coach them and protect clients from bad programming.
- **Functional job:** oversee programs; spot bad load progression; intervene early.
- **Emotional job:** be the gym's clinical brain.
- **Social job:** elevate gym reputation.
- **Hire Vis to:** branch-level view of all client programs; flag clients with ACWR > 1.5 or stagnant for > 3 weeks; comment on junior PT's program drafts.

#### Staff Personas (2 archetypes)

**1. The Front-Desk Staff (Meera, 24)**
- *When* a walk-in arrives or a member checks in, *I want* to take payment + activate membership + assign a trainer in under 60 seconds, *so I can* keep the queue moving.
- **Functional job:** fast member onboarding + payment logging + trainer assignment.
- **Emotional job:** look competent to the member.
- **Social job:** be the friendly face of the brand.
- **Hire Vis to:** scan QR + activate; in-person payment log (cash/card/UPI); assign trainer from available pool; send WhatsApp template welcome.
- **Anti-job:** make me leave the desk to find an answer in a separate ops tool.

**2. The Branch Manager (Suresh, 36)**
- *When* I review the branch end of week, *I want* to see retention, new joins, trainer utilization, and revenue, *so I can* intervene before metrics dip.
- **Functional job:** branch P&L visibility.
- **Emotional job:** confidence reporting up to the owner.
- **Social job:** be seen as a strong operator.
- **Hire Vis to:** branch dashboard; retention cohort chart; trainer utilization heat-map; payment reconciliation export.

#### Owner Personas (2 archetypes)

**1. Single-Branch Owner (Hiren, 41, owns 1 gym in Borivali)**
- *When* I look at my gym in the morning, *I want* to know yesterday's revenue + active members + churn risks in one glance, *so I can* spend the day on the floor not in a spreadsheet.
- **Functional job:** business at-a-glance + early churn signal.
- **Emotional job:** ownership without anxiety.
- **Social job:** be a respected gym brand in the neighborhood.
- **Hire Vis to:** owner dashboard; churn-risk member list; trainer performance ranking; in-person payment integrity audit.

**2. Multi-Branch Owner (Mr. Bhandari, 52, owns 7 branches across Mumbai + 2 Tier-2 cities — i.e., you)**
- *When* I review my chain, *I want* to compare branches on retention + revenue + training quality + complaint rate, *so I can* invest in winners + intervene in losers.
- **Functional job:** chain-level KPIs; ability to drill into any branch.
- **Emotional job:** the chain runs without my daily presence.
- **Social job:** be the brand that does PT right in India.
- **Hire Vis to:** chain-level dashboard with per-branch ranking; consolidated trainer roster; standardized program library shared across branches; consolidated payment ledger.
- **Anti-job:** force me to log into 7 separate systems.

### Sources

- [Schoenfeld dose-response meta — Semantic Scholar](https://www.semanticscholar.org/paper/Dose-response-relationship-between-weekly-training-Schoenfeld-Ogborn/0d34206f962394983054451cddd8a3b91818f732)
- [2024 Dose-Response Meta-Regression update — SportRxiv](https://sportrxiv.org/index.php/server/preprint/view/460)
- [Autoregulated RT for Max Strength — Network Meta-Analysis, PMC](https://pmc.ncbi.nlm.nih.gov/articles/PMC12336695/)
- [SET FOR SET — Autoregulation tools (RPE, RIR, Bar Speed)](https://www.setforset.com/blogs/news/autoregulation-tools-for-strength-training)
- [ScienceForSport — Acute:Chronic Workload Ratio](https://www.scienceforsport.com/acutechronic-workload-ratio/)
- [ACWR systematic review meta — PMC](https://pmc.ncbi.nlm.nih.gov/articles/PMC7047972/)
- [GymAware — VBT theory and application](https://gymaware.com/velocity-based-training/)
- [Metric VBT — barbell velocity tracking](https://www.metric.coach/articles/metric-vbt-pro-barbell-velocity-tracking-app)
- [Arvo — 1RM Formulas (Epley vs Brzycki vs Lander)](https://arvo.guru/resources/one-rep-max-formulas)
- [Strength Journeys — E1RM 7 Formulas](https://www.strengthjourneys.xyz/articles/how-do-i-calculate-my-e1rm-estimated-one-rep-max)
- [NN/g — Personas vs Jobs-to-Be-Done](https://www.nngroup.com/articles/personas-jobs-be-done/)
- [Aha — JTBD framework](https://www.aha.io/roadmapping/guide/release-management/what-is-the-jobs-to-be-done-framework)

---

## Research Synthesis

### Executive Summary

Vis is being designed at a structurally rare moment. The fitness-app market splits into three competing categories that none of the incumbents has unified: AI-coached lifting (Motra, Bevel, Fitbod, JuggernautAI), PT-SaaS (Trainerize, TrueCoach, Everfit, Caliber, Future), and multi-location gym ops (Mariana Tek, Hapana, Mindbody, Glofox). Each category is growing 10–19% CAGR. Each owns one user well and dismisses the other two as "out of scope". Mariana Tek's May 2026 launch of Appointments to add 1:1 PT into ops software is the clearest market signal yet that convergence is starting.

Vis bets on full convergence — client + trainer + staff + owner as first-class roles in one role-scoped, multi-branch product. Two capabilities are red across every cluster surveyed: (1) trainer↔client real-time in-person session collaboration, and (2) cinematic on-brand UX in a multi-stakeholder product. These are Vis's defensible moats; everything else is competitive parity that must be cleared.

Training-science direction is settled. The 2025 network meta-analysis ranks autoregulation (APRE 93%, RPE 67%) far above fixed-percentage prescription (13%) for maximal strength outcomes; Schoenfeld's 2024 dose-response update confirms volume's diminishing returns above ~20 sets/muscle/week; e1RM from sub-maximal sets (Epley/Brzycki, ±5–10%) is accurate enough to drive a Progression Index without ever loading 1RM tests. Vis's training engine should be built on RIR, RPE, e1RM, set count per muscle, and on-device HealthKit/Health Connect biometrics — not a more elaborate science layer.

Regulatory direction is also settled, and favorably. The spec's choice to keep biometric data on-device, read-only, never persisted server-side collapses ~80% of the GDPR + India DPDPA compliance surface. DPDPA is fully in force as of May 2026 with penalties up to ₹250 crore, so this single architectural choice is also the most material risk mitigation in the product.

The remaining open questions are product, not strategy: how to make in-person real-time session collab feel native; how to present a Progression Index without hiding it behind charts; how to keep cinematic UX consistent across three engineering stacks (RN client, RN trainer, Angular admin) while three different teams ship in parallel.

### Table of Contents

- Step 1 — Scope confirmation
- Step 2 — Industry analysis (3 markets, $20–25B combined 2026 size)
- Step 3 — Competitive landscape (3 clusters × ~20 apps; Vis white-space matrix)
- Step 4 — Regulatory focus (HealthKit, Health Connect, GDPR, DPDPA, sectoral)
- Step 5 — Tech trends, metric catalog, JTBD personas
- Step 6 — Synthesis + recommendations (you are here)

### Top 10 Strategic Takeaways

1. **The white space is convergence, not a better lifter.** AI-coached lifting apps own the science; PT-SaaS owns the coach workflow; gym-mgmt owns the ops. Vis is the first opinionated attempt to unify all three with role-scoped data. Don't let scope creep dilute it; keep the four roles first-class.
2. **Real-time in-person session collab is the unique moat.** No one ships it. WebSocket + polling fallback, last-write-wins per set row (already in spec), is the right architecture. This is the demoable wow moment.
3. **Auto-regulation is now table stakes.** RPE + RIR per set are first-class inputs from v1. Not optional. Trainer logs them; client confirms/edits post-set.
4. **Progression Index is the single derived metric to lead with.** Powers the recovery-orb-style progress feedback. Three weights (e1RM, volume, RPE-efficiency), trainer-tunable per client goal. Don't show the formula to the client; show the result.
5. **Recovery orb stays.** HRV + sleep + readiness from on-device sources shade trainer's intensity recommendation but do not auto-override v1. Auto-override is v2 after trust is earned.
6. **Spec's "biometric data stays on-device" is the most valuable line in the document.** Lock it non-negotiable v1.
7. **AI = Claude + prompt caching, backend-proxied.** ~90% cost cut on cached system prompts. Never expose key to client. Outputs strongly typed; reject untyped LLM payloads.
8. **Trainer adoption is the riskiest beach.** Don't try to win trainers solo against Trainerize. Win the gym institution, trainers come bundled with the gym contract.
9. **Defer EU to v2.** GDPR + Indian data-residency complexity outweighs early-market value. India + global non-EU first.
10. **The visually-dated category is an opportunity.** Vis's prototype orb language is a real differentiator in a category dominated by 2018-era SaaS UI. Treat design quality as a competitive feature, not a polish item.

### Persona-to-Feature Mapping (PRD seed)

| Persona | Top 3 features they hire Vis for | Phase priority |
|---|---|---|
| Aarav (beginner client) | Pre-loaded session, form video, consistency streak | P0 |
| Priya (recomp client) | Progression Index, photo timeline, recovery orb | P0 |
| Rohit (strength athlete client) | e1RM curve, ACWR alert, RIR-based self-adjust | P1 |
| Mrs. Kapoor (longevity client) | Visit count, sit-to-stand, balance/mobility scores | P2 |
| Karan (in-gym PT) | One-tap set logging, voice RIR input, today-sessions on top | P0 |
| Anjali (online/hybrid PT) | Auto-tags (PR/skip/silent), AI program builder, check-in tracker | P1 |
| Vikram (master trainer) | Branch program view, ACWR>1.5 flag, comment on PT drafts | P2 |
| Meera (front-desk staff) | QR-activate + payment log + trainer assign in 60s | P0 |
| Suresh (branch manager) | Branch dashboard, retention cohort, payment reconciliation | P1 |
| Hiren (single-branch owner) | Owner dashboard, churn-risk list, payment audit | P1 |
| Mr. Bhandari (multi-branch owner) | Chain dashboard, shared program library, consolidated ledger | P0 |

### Architecture Implications (for `bmad-create-architecture`)

- **Real-time sync layer:** Spring Boot WebSocket + RN client (`react-native-websocket` or Socket.IO). Polling fallback for connectivity drops. Last-write-wins per set row.
- **Biometric data:** never persisted server-side v1. Client computes HRV trends locally; sends summary stats only on opt-in (v2).
- **AI proxy:** Spring controller → Claude API with system-prompt caching. Cache key = stable per role+goal. Per-user TTL. Audit log all calls. Outputs typed (Jackson `@JsonTypeInfo` or Pydantic-equivalent in Java records).
- **Multi-branch scoping:** branch_id on every core entity (already in spec). Service-layer enforcement (already in spec). Owner role unscoped; STAFF/TRAINER/CLIENT branch-scoped.
- **Region:** Cloud Run asia-south1 (Mumbai) primary. Backup region asia-southeast1.
- **Database:** Neon PG. Migrations Flyway (already in spec). One critical table: `set_logs` with optimistic locking version column for last-write-wins.
- **Auth:** Firebase Auth (already in spec). FirebaseAuthFilter validates JWT → UserService resolves role + branch_id.
- **Wearables v1:** read-only HealthKit + Health Connect. v2: Whoop, Oura, Garmin via OAuth.
- **Observability:** structured JSON logs; per-request branch_id + role + user_id in MDC. OpenTelemetry traces for AI calls (latency + cost).

### UX Implications (for `bmad-create-ux-design`)

- Recovery orb stays. Use it for **state**, not navigation. Color + animation = readiness summary (HRV+sleep+subjective).
- Progression Index as the **headline number** on home; trend graph one tap away.
- Per-set logger: ≤ 3 taps to log weight + reps + RIR. Voice optional for trainer in-session.
- Trainer "today" view: vertical timeline of today's clients; tap to enter session; in-session view shows planned vs actual side-by-side with live trainer/client edit indicators.
- Staff front-desk: single search bar; member or phone number; activate / log payment / assign trainer in inline modal.
- Owner: at-a-glance + drill-down. Per-branch ranked list; tap branch for that branch's view (same UI as branch-manager-only view).
- Theme parity: every screen looks correct in both light and dark per `docs/brand-ref/reference_light.png` and `reference_dark.png`. Pure white avoided everywhere except elevated cards.
- Typography: system stack + 1.6–1.7 line-height; cinematic amber accent in hero CTAs only.

### Red Flags & Risks

| Risk | Severity | Mitigation |
|---|---|---|
| Trainer adoption (Trainerize incumbent) | High | Win the gym institution first; trainers come bundled. Build CSV import from Trainerize. |
| Multi-app parallel dev (RN client + RN trainer + Angular admin) drift | High | Shared design system tokens (already in spec). Single source-of-truth in `prototype/src/tokens.js`. Style review per PR. |
| Real-time sync correctness | Medium | Last-write-wins per set row is correct; test concurrent edits with Playwright + RN tests. |
| AI cost overrun | Medium | Prompt caching from day 1. Per-user daily cap. Audit log + cost dashboard. |
| DPDPA enforcement | Medium | Spec choice (on-device biometrics) removes most exposure. Implement export + delete flows + breach-notification procedure pre-launch. |
| Scope creep into v2 (auto-override, CV form, VBT) v1 | Medium | Lock v1 scope to current PRD; backlog v2 candidates explicitly. |
| Bevel/Motra ships a multi-trainer mode before Vis ships | Low–Medium | Watch product blogs quarterly; Vis's gym institution distribution + India localization remain unique. |
| iOS-only competitors (Motra) ignored by users | Low | Vis is RN, neutral. Android-first is an India advantage. |

### Open Questions for the Team (carry into PRD / brainstorming sessions)

- Does v1 Trainer App ship voice RIR input or text-only?
- What is the minimum Android SDK — 34 (clean Health Connect) or lower with fallback?
- How does the Progression Index degrade gracefully on weeks the member skipped sessions?
- Does the recovery orb show absolute readiness or 7-day-delta?
- Trainer cert verification — manual upload + admin verify, or pull from a cert-body API where one exists?
- Where does the AI macro/nutrition layer sit — Client App only, or also Trainer-supervised approval?
- WhatsApp templates — directly from Vis, or generate-and-copy?

### Recommended Next BMad Steps (your originally-stated team request, mapped)

You asked for personas, a research-the-market expert, PO, scrum master, architect, devs, UX experts, UI for RN/Angular, plus iOS/Android/Web experts. Mapping that to BMad skills + already-installed plugins:

| You asked for | BMad skill / plugin to invoke (in fresh context) |
|---|---|
| Research the apps (progressive overload + competitors) | ✅ done in this doc |
| Personas + metrics | ✅ done in this doc |
| Scientific gym expert | ✅ done via Schoenfeld/Helms/SBS/RP/JuggernautAI lineage in Step 5 |
| Product Owner / PRD writer | `bmad-prd` (writes a real PRD against this research) |
| UX expert / innovative ideation | `bmad-create-ux-design` and `bmad-brainstorming` (problem-by-problem) |
| UI expert for RN | `react-native-best-practices` + `react-native-skills` plugins + `frontend-design` skill |
| UI expert for Angular | `frontend-design` skill + Context7 MCP (live Angular docs) + 21st-dev/magic for component generation |
| Architect | `bmad-create-architecture` |
| Scrum master / Sprint planning | `bmad-sprint-planning` |
| 3 strong devs | `bmad-create-story` → `bmad-dev-story` cycle (or run multiple devs in parallel via `subagent-driven-development`) |
| Multi-role round-table for tough tradeoffs | `bmad-party-mode` (Mary + John + Sally + Winston + Amelia + Paige as subagents) |

**Recommended sequence next:** `bmad-prd` → `bmad-party-mode` (challenge PRD with 6-agent round-table) → `bmad-create-ux-design` → `bmad-create-architecture` → `bmad-create-epics-and-stories` → `bmad-check-implementation-readiness` → `bmad-sprint-planning` → dev cycle.

### Completion

Domain research complete. Six sections, all sources cited inline + listed per section.

**Companion file:** `domain-vis-fitness-pt-research-2026-05-21.html` is the human-readable artifact (HTML-first per project rule). This `.md` retains BMad workflow state.

**Status:** `completed` (2026-05-21)

