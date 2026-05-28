---
title: PRD ↔ Domain Research Reconciliation
status: draft
created: 2026-05-22
owner: Gauravprakashshinde
inputs:
  - _bmad-output/planning-artifacts/research/domain-vis-fitness-pt-research-2026-05-21.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/addendum.md
purpose: Check whether the assembled PRD reflects the load-bearing findings from the 6-step domain research. Surface gaps where research evidence should shape PRD claims or be cited.
---

# PRD ↔ Domain Research Reconciliation

## 1. Brief

Domain research is a 723-line, six-step study covering (a) market structure across three converging clusters (AI-coached lifting, PT-SaaS, gym ops), (b) competitive deep-dive on ~20 named apps with a white-space matrix, (c) regulatory layers (HealthKit, Health Connect, GDPR, DPDPA), (d) a six-family training-science metric catalog with formulas + evidence, (e) eleven named JTBD personas across four roles, (f) synthesis with 10 strategic takeaways + architecture/UX implications.

The PRD is broadly aligned with the research's strategic direction — it already reflects the convergence thesis (six role-contexts, multi-branch RBAC), the real-time co-edit moat (FR-1), the auto-regulation training stack (RIR/RPE/e1RM as first-class), the recovery orb, and the on-device biometric line. The Vision §1 even names "trainer ↔ client real-time in-person collab + multi-branch gym ops are the structural moats" — verbatim from research takeaway #2.

But the PRD's evidentiary base is largely implicit. Specific named competitors, specific formulas, specific Schoenfeld/IOC/Apple/MeitY sources, and several training-science choices (volume ceiling, ACWR alerts, e1RM-formula range) that the research surfaces as load-bearing are either un-cited, un-quantified, or absent from the FRs and SMs. The Open Questions list raises items the research already answered with evidence (e.g. e1RM formula choice).

Net: the strategy is honored; the **evidence** isn't. Most gaps are about anchoring claims to the research so downstream artifacts (UX spec, architecture) and reviewers can trust the targets and formulas. A handful are missing FRs / metrics with material product impact.

## 2. Honored (research findings the PRD already reflects)

| Research finding | Where PRD reflects it |
|---|---|
| Convergence thesis — unify client/trainer/staff/owner | §2.1 six role-contexts, §1 Vision assertion #2 |
| Real-time in-person co-edit is the unique moat (red across all 3 clusters) | FR-1, §1 Vision, SM-3 |
| Last-write-wins per set row (correct v1; CRDT overkill) | FR-1, §7.2, Glossary "Co-Edit" |
| WebSocket transport with offline-queue fallback | §7.2, FR-1 |
| Auto-regulation (RIR/RPE) as first-class per-set input | FR-3, FR-4, Glossary "Set" |
| e1RM as a surfaced metric driving PR detection | FR-10, Glossary |
| Multi-branch role-scoped data (branch_id on entities; Owner unscoped) | §2.1 table, FR-20–23, Glossary "Tenant/Brand/Branch" |
| AI = trainer-only v1 for cost containment; Claude proxy + prompt caching | FR-13, §7.3 (≥80% cache hit, $0.40/WAU ceiling) |
| Biometric data stays on-device, read-only, never persisted | §5 Non-Goals (Apple Health / Google Fit write-back) |
| DPDPA in force; encryption at rest; right-to-erasure | §7.4 |
| Defer EU to v2; single-region asia-south1 v1 | §5 Non-Goals, §7.9 |
| Trainer cert verification + verified badge | FR-19, FR-24 |
| AI cost guardrail as a counter-metric ($0.40/WAU, pivot at $1) | SM-C1, §8 Pivot Triggers |
| WhatsApp templates v1 = generate-and-copy; Business API direct-send → v2 | FR-18, §5 |
| Recovery orb v1 surfaces readiness but does not auto-override prescription | FR-35 (color-coded chips; no auto-override language in FR-12–14) |
| Cinematic on-brand UX as differentiator (orb language) | §1 Vision (beauty serves usability) |
| Trainer marketplace as scope-pressure deferral candidate | §6.2 v1.5 candidates, FR-34 NOTE FOR PM |
| Apple Watch / wearables deferred post-v1 | §5, Addendum §5 |
| 11 named personas distilled to 6 v1 personas (Aarav/Rohit/Priya/Karan/Anita/Vikram) | §2.1 |
| Real-time disconnect rate as counter-metric | SM-C2 (>5%) |

## 3. Gaps (ranked by load-bearing impact)

### Tier A — load-bearing, should be incorporated before PRD finalize

**A1. No citations or evidence anchoring in the PRD.** The PRD never names Schoenfeld, IOC consensus on ACWR, Epley/Brzycki, Apple/MeitY, the 2025 autoregulation network meta-analysis, or the 52% trainer-AI-adoption stat. The research's whole purpose is to ground the PRD's targets in citable evidence; the PRD's "inputs:" frontmatter lists the research file but no FR, NFR, or SM cites it. Downstream reviewers and the Finalize step cannot tell which claims are inferred vs. evidence-backed. **Fix:** add a "Research Anchors" subsection or inline `[research: §Step-N-anchor]` tags on FR-10 (Epley), FR-12/13 (autoregulation ranking), §7.4 (DPDPA penalty + timeline), §8 SM targets.

**A2. e1RM formula is listed as an Open Question and an Assumption — research already answered it.** The PRD (FR-10, §10 Assumptions) flags Epley as `[ASSUMPTION]` and OQ-3 lists Epley/Brzycki/Lombardi/Wendler as user-selectable alternatives. Research §A2 gives a specific evidence-backed recommendation: **Epley for r ≤ 5, Brzycki for r 6–10, average at r=5 boundary** — ±5–10% accuracy. This is a load-bearing product decision the research closed. **Fix:** lift the recommendation into FR-10; drop OQ-3 or narrow it to a UX-toggle question.

**A3. Missing FR / metric: Schoenfeld volume ceiling (set count per muscle / week).** Research §A1 surfaces "Set count per muscle/week" as **Surface tier — primary progress signal** with a specific evidence-backed ceiling (~20 sets/muscle/week, diminishing returns above, inverted-U at extremes). The PRD's Progression Index (Glossary) doesn't reference it; FR-36 (Progress Tab) shows volume only as a "weekly volume" mention without a per-muscle breakdown or ceiling alert. This is the single most-cited training-science number in the report and the lever a trainer most often pulls. **Fix:** add an FR (or extend FR-36) for per-muscle weekly set count with a soft alert when approaching/exceeding ~20.

**A4. Missing FR / trainer-side alert: ACWR (Acute:Chronic Workload Ratio) overtraining flag.** Research §A4 explicitly recommends "trainer dashboard amber alert at ACWR > 1.5" citing IOC 2016 consensus (0.80–1.30 lower injury risk; >2.0 higher). It also appears in the persona-to-feature mapping for Rohit (P1) and Vikram (P2). The PRD has no equivalent. ACWR is a meaningful trainer-side differentiator vs. competitors that only show raw tonnage. **Fix:** add an FR under Block C or Block E for trainer-side workload alerts; mark P1 or P2; tie to SM-7.

**A5. Progression Index formula is in research but not in PRD.** Research §A6 gives the explicit formula:
`ProgressionIndex = w_e1rm·ΔeRM + w_vol·ΔVolume + w_quality·ΔRPE_efficiency` with three trainer-tunable weights per client goal (strength / hypertrophy / endurance). The PRD's Glossary defines Progression Index as "a numeric score derived from `actual` set data over the last N sessions" — unspecified. This is the headline derived metric. **Fix:** lift the formula into the Glossary or FR-12; add the trainer-tunable weights as an explicit FR consequence (goal-tag on plan drives default weights).

**A6. India tech-context anchors for §7.5 Performance NFRs are missing.** Research §B8 specifies: 4G+ widely available, Tier-1 5G, ~50ms RTT to Mumbai, ~200ms to US/EU, Android ~95% of India market. The PRD §7.5 names "Pixel 6a, Galaxy A54" mid-tier targets but doesn't anchor the latency budget or the Android skew. SM targets (2,500 WALS D90) are also unanchored — no market-size math from research §Industry Analysis informs them. **Fix:** add Android-first language to §7.5; cite the 50ms-RTT figure as the basis for the 250ms set-save p95 budget.

**A7. Missing competitor-specific anti-positioning in §1 Vision or §5 Non-Goals.** Research names specific differentiators Vis is **counter-positioning** against: Motra's wrist-only leg-exercise underdetection (justifies why Vis doesn't bet on auto-detect v1); Caliber's HRV-not-in-coaching-loop gap (justifies Vis's recovery orb feeding trainer suggestion); Trainerize's "branch = CRM tag, not first-class" (justifies multi-branch RBAC); Bevel/Motra's solo-only ceiling (justifies the gym-institution wedge). The PRD's Vision states moats generically without naming what's being beaten. **Fix:** add a "Competitive anti-positioning" callout under §1 with the four counter-claims tied to specific named competitors.

### Tier B — should be incorporated, but lower-stakes than Tier A

**B1. Sleep <6h next-day strength deficit (§A3) should justify a Recovery FR consequence.** Research says sleep <6h → measurable strength + RPE deficit next day. This is the evidence base for the recovery orb shading trainer's next-session intensity. FR-35 just lists the orb's color logic without explaining what feeds it beyond "the last Session that trained that group" — missing the sleep/HRV inputs research treats as first-class.

**B2. JTBD anti-jobs from research are not surfaced.** Each persona in research has a `Anti-job (do NOT do)` line: Aarav "don't overwhelm with HRV/RPE/e1RM jargon week one"; Mrs. Kapoor "don't gamify with badges that infantilize"; Karan "don't force me to type during a set." These are valuable guardrails for UX spec but also for PRD's Non-Goals or per-FR consequences. None of the four anti-jobs are reflected.

**B3. Cardio logging FR-32 is bare; research §A6 / Cluster A apps offer no clear guidance because cardio isn't progressive-overload-native, but the PRD should acknowledge that and not over-promise.** Currently FR-32 just lists fields; doesn't say cardio is out of the Progression Index, doesn't say PR detection doesn't apply. This is a small ambiguity but a real one for the engineering scope.

**B4. Trainer marketplace moderation SLA (OQ-7) lacks research-grounded anchor.** Research doesn't give a specific number but consistently treats marketplace abuse as a real risk in Cluster A/B competitors. The PRD's SM-C7 (>1% profiles) is reasonable but unanchored.

**B5. Min Android SDK question is open in PRD but research gives a direction.** Research §B5 / Layer 2: "Min SDK 34 cleanest; for older Android, Health Connect installable as separate app. Decide min-SDK during Phase 0." The PRD's Open Questions has no entry for this even though wearable-readiness depends on it. The research recommendation (SDK 34) should at least be the assumption.

**B6. Hybrid PT model (in-person + remote check-ins) is the growth segment per research §Industry Trends; PRD doesn't explicitly position around it.** UJ-1 is in-person; FR-1 supports both but Vision could name the hybrid wedge.

**B7. Switching-cost analysis from research §Competitive Dynamics → "build CSV import from Trainerize" is a Red Flag mitigation.** The PRD's FR-15 has CSV import for members from Staff perspective but no Trainer-side import of programs from Trainerize/TrueCoach to ease trainer-side switching. Worth at least a v1.5 candidate.

### Tier C — nice-to-have

**C1. The 11-persona research roster collapses to 6 in PRD — fine, but the four collapsed personas (Mrs. Kapoor longevity client, Anjali hybrid PT, Suresh branch manager, Hiren single-branch owner) might warrant a brief "Persona coverage rationale" note explaining why Anita ≈ Meera+Suresh, Vikram ≈ Hiren+Bhandari, etc.

**C2. Market-size figures ($12.12B consumer fitness, $7.23B AI PT) don't appear in PRD.** Not load-bearing for product spec, but the GTM workstream (Addendum §6) will need them.

**C3. The "$30M DPDPA penalty" / "₹250 crore per breach" figure justifies the security spend implied by §7.4 + §7.8 but isn't in the PRD; a reviewer pushing back on observability budget would benefit from seeing it.

**C4. Specific named scientific lineage (Schoenfeld/Helms/Israetel/RP) is in research; PRD's Plan Template library FR-31 mentions "Vis starter library (~15 curated templates)" without naming the lineage. Citing the lineage is a trust signal that justifies the v1 template count.

## 4. Anti-gaps (research findings PRD correctly does NOT incorporate)

- **VBT (velocity-based training) is in research §A2 but explicitly tagged "Hide v1; defer to v2 premium tier."** PRD correctly omits. No fix needed.
- **CV form analysis is in research §B2 but tagged "Vis v1 recommendation: skip CV."** PRD correctly omits. Trainer co-presence reduces v1 marginal value (research's own argument).
- **Whoop / Oura / Garmin OAuth integrations** are research §B5 v2 candidates. PRD correctly omits from v1.
- **Recovery-driven auto-override of prescriptions** is research §B4 v2 candidate; PRD correctly keeps FR-12–14 trainer-driven, not auto-override.
- **Auto-detection of exercises (Motra-style Apple Watch motion)** is research §B3 Tier-2 post-v1; PRD correctly omits.
- **Smart-equipment / Technogym / Mindbody integrations** are research §Ecosystem long-tail; PRD correctly omits.
- **Multi-region failover** is research-acknowledged complexity; PRD correctly defers to v2 (§5, §7.9).
- **HRV trend computation server-side** is research v2 (opt-in); PRD correctly keeps biometrics on-device (§5, §7.4).

The PRD's deferrals match the research's recommended cut-line cleanly. The scope discipline is good — the gaps are about evidence anchoring and a small number of missing FRs (volume ceiling, ACWR alert, Progression Index formula), not about over- or under-scoping.

---

*Reconciliation produced 2026-05-22. Use to inform PRD Finalize step 7 — confirm or defer each Tier A gap, decide whether Tier B items go into v1, v1.5, or backlog.*
