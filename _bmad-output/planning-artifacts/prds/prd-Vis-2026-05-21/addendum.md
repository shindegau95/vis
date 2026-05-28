---
title: Vis PRD — Addendum
status: draft
created: 2026-05-21
updated: 2026-05-21
purpose: User-contributed depth that informs downstream docs (UX spec, architecture, business model) but does not belong inside the PRD itself.
---

# Vis PRD — Addendum

Depth captured during Discovery that should flow into UX spec / architecture / business-model docs rather than the PRD. Updated as the conversation unfolds.

## 1. Equipment-aware logging UX (for UX spec)

User flagged this as the **single most important UX problem to solve**: gym-goers can't remember progression and find input a hassle. Vis's response is to make logging mimic the physical equipment, not abstract numeric entry.

### Concrete pattern user named: cable machine

- Visual weight stack rendered on screen
- User drags a pointer / pin to the slab position they used
- No typing of weight numbers
- Hits real-world muscle memory of how cable stacks are loaded

### Extensions to design (UX spec to detail)

- **Barbell:** plate-stack visualizer per side; tap plates onto bar; auto-sum to total
- **Dumbbell:** number-line slider with kg/lb toggle; common-rack increments snap
- **Machine pin-stack (non-cable):** same as cable pattern
- **Bodyweight:** body silhouette with optional weight-vest field
- **Kettlebell:** discrete-step picker matching standard kettlebell weights
- **Bands:** color-coded band picker (resistance tier)

### Design principles

- Default to last-used weight for that exercise + that user (pre-loaded)
- One-tap "same as last set" / "same as last session"
- Voice fallback for trainer in-session (≥1 second of speech → weight + reps)
- Always show next-suggested-weight (from Progression Index) as a ghost overlay

## 2. Monetization Model (for business-model doc; v1 PRD ships with no pricing)

v1 = free. Defer pricing surface entirely.

### Future model A — Direct (B2C)

- Client pays trainer (or trainer's gym) for the trainer's use of Vis on their behalf.
- Payment flow lives outside Vis OR routed via Vis as platform fee.
- Trainer onboards self-employed.

### Future model B — Gym-mediated (B2B2C)

- Gym pre-purchases N client seats as part of its membership fee.
- Gym earns margin on Vis usage.
- Org/billing entity = `gym`.

### Architecture implication (carry to architecture phase)

- Billing-entity field per user: `gym | direct | none`
- Seat-count accounting per gym
- Client may transition between billing entities over time (joined gym, left gym, kept Vis with self-employed trainer)

## 3. PT Lifecycle States (for state-machine in architecture; mirrored in PRD as FR group)

A user can be in any of these states with respect to PT:

```
NoPt  ──── invite trainer / accept ────►  PtActive
PtActive  ──── PT or client ends ────►   PtEnded (data retained)
PtEnded  ──── invite + accept new ────►  PtActive (with new trainer)
PtEnded  ──── (no new PT) ────►          NoPt (but history retained)
```

Open: what does "trainer leverage on PT off" mean operationally? Hypotheses:
- Trainer retains read-only view of past programs they wrote
- Trainer can send re-engagement message
- Client's history remains visible to client only after PT separation
- Trainer keeps a "client alumni" list separate from active roster

To be confirmed with user.

## 4. Internationalization (for architecture + UX spec)

v1 baseline:

- **Weight units:** kg primary, lb toggle. Stored canonical = kg in DB. UI converts.
- **Distance / height units:** m + ft/in toggle. Canonical = m.
- **Currency:** multi-currency at billing layer; v1 PRD has no billing so deferred. Plan currency-agnostic data model now.
- **Date format:** per locale (en-IN → DD/MM/YYYY; en-US → MM/DD/YYYY; ISO 8601 internal).
- **Time zone:** per user, with branch-level default.
- **Language:** English-only v1; i18n keys structured for future translation.

## 5. Wearable / Apple Watch app — explicitly deferred

Not in v1 PRD. Companion app (Apple Watch, Wear OS) is a post-v1 candidate when:

- v1 core trainer↔client collab is shipped + adopted
- Auto-detection accuracy proven (cf. Motra's wrist-only limitation on leg lifts)

## 6. Post-PRD GTM workstream (flagged, not lost)

Separate from this PRD. User wants guidance on:

- App Store + Play Store submission process
- Advertising / pitch strategy

To be addressed as a distinct workstream after PRD → UX → Architecture → first usable build. Recommended skill: spawn a focused GTM doc via `bmad-agent-tech-writer` or `bmad-quick-dev` post-launch-readiness.
