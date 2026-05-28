# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Required Skills

Always invoke these skills before any frontend or coding work — no exceptions:

- **`/frontend-design:frontend-design`** — invoke before building or modifying any UI component, screen, or layout. Establishes aesthetic direction, design quality bar, and animation/typography standards.
- **`/karpathy-guidelines`** — invoke before any implementation task. Enforces simplicity-first thinking, surgical edits, and goal-driven execution to avoid common LLM coding mistakes.
- **`/react-native-best-practices`** (callstack plugin) — invoke before any React Native work in `trainer-app/` or `client-app/`. Covers JS thread, Hermes, memory leaks, animation performance.
- **`/react-native-skills`** (gigs plugin) — invoke alongside react-native-best-practices for comprehensive React + RN + Expo pattern coverage (130+ rules).

All skills must be invoked at the start of the task, not after. If a task touches both design and code (which most tasks in this project do), invoke all relevant skills.

- **`/caveman`** — invoke at the start of every session, no exceptions. Reduces token usage by ~75% by dropping filler words while preserving full technical accuracy.

## Documentation Format: HTML, not Markdown (MANDATORY)

All documentation artifacts produced for this project — research reports, PRDs, UX specs, architecture docs, story files, retrospectives, plans, brainstorming outputs, validation reports, distillates, anything intended for a human to read — **must be written as standalone `.html` files**, not `.md`.

Rationale: Anthropic engineering lead Thariq Shihipar's "The unreasonable effectiveness of HTML" (claude.com/blog/using-claude-code-the-unreasonable-effectiveness-of-html, May 2026). HTML enables tabs, tables, SVG illustrations, responsive layout, navigation, interactive controls (sliders, draggable cards, copy buttons), and stays readable past 100+ lines where Markdown collapses. There is no official Anthropic skill for this — it is a project-level convention enforced here.

Rules:

- **File extension:** `.html`. Self-contained — inline `<style>` and inline `<script>`, no external CDN deps unless explicitly approved.
- **Styling:** match the Vis brand palette. Reference the CSS vars listed in this file (`--vis-amber-primary`, `--gc-bg-elevated`, etc.) embedded inline at the top of each doc, plus a light/dark `prefers-color-scheme` media query. Warm ivory bg (`#F9F6F0`), never pure white. Cinematic amber accents.
- **Typography:** system font stack (`-apple-system, BlinkMacSystemFont, "Segoe UI", system-ui, sans-serif`). Generous line-height (1.6–1.7). Max content width ~760px for prose, full-width for tables/diagrams.
- **Structure:** `<header>` with title + date + author, sticky table-of-contents `<nav>` for docs >300 lines, semantic `<section>` blocks. Use `<details>`/`<summary>` for collapsible long-form. Use `<table>` for comparisons (not ASCII tables).
- **Interactivity when helpful:** tabs for parallel options, side-by-side diff blocks, draggable Kanban for backlog views, sliders for parameter tuning, "copy to clipboard" buttons on code/spec blocks.
- **Code blocks:** `<pre><code>` with minimal monospace styling. Syntax highlighting optional — keep dependency-free.
- **Mockups & diagrams:** inline SVG (preferred), or `<canvas>` only if needed. No external image hosts.
- **Frontmatter equivalent:** every doc starts with a `<header>` block carrying title, date, author (`Gauravprakashshinde`), and a one-line abstract.

**Narrow exceptions — keep as Markdown:**

- `README.md`, `CLAUDE.md`, `PLAN.md` at repo root (tool-readable conventions).
- `*.md` files BMad/superpowers skills require by name to drive their own workflows (e.g. BMad story files, sprint-status files where the skill machinery reads `.md`). When a skill writes `.md`, leave it as `.md`, but also generate a companion `.html` rendering at the same path for human reading.
- Git commit messages, PR descriptions, inline code comments.

**Naming:** `<topic>-<YYYY-MM-DD>.html`. Place inside the same output folder the producing skill would have used for `.md`.

## UI Reference Images (MANDATORY)

Whenever the user asks for any UI update, change, or new component, **first open and read** these reference images via the `Read` tool:

- `docs/brand-ref/reference_light.png` — light theme canonical UI (home card, recovery orb, list cards, login screens)
- `docs/brand-ref/reference_dark.png` — dark theme canonical UI (same screens)

Use them as the visual source of truth for layout, spacing, typography, orb structure, card composition, and tone. Match against the corresponding theme. Never iterate on UI without first re-checking these references — your idea of "matches" drifts otherwise.

## Brand Palette

Canonical orange + neutral palette. Per-theme — never hard-code anchor hexes; reference the CSS vars (`var(--vis-amber-primary)`, `var(--gc-bg-elevated)`, etc.) so theme flip works.

| Usage                  | Dark hex  | Light hex | CSS var                  | Notes                                             |
| ---------------------- | --------- | --------- | ------------------------ | ------------------------------------------------- |
| Primary glowing orange | `#FF7A1A` | `#E06313` | `--vis-amber-primary`    | Main orb fill + CTA. Light is deeper for AA text. |
| Deep emissive orange   | `#FF5E00` | `#FF5500` | `--vis-amber-deep`       | Inner glow / hotspots / liquid base.              |
| Ember (dark tangerine) | `#D9590B` | `#F26200` | `--vis-amber-ember`      | Orb liquid floor / dome dark. Vibrant dark tangerine — never brown. |
| Soft warm orange       | `#FF9B4A` | `#FFA366` | `--vis-amber-soft`       | Gradient transitions.                             |
| Highlight orange       | `#FFC27A` | `#FFE3C2` | `--vis-amber-highlight`  | Reflections / specular / glare.                   |
| Burnt orange shadow    | `#A94400` | `#C45100` | `--vis-amber-shadow`     | Depth + lower orb shading.                        |
| Ambient glow orange    | `#FF6A00` | `#FF6A00` | `--vis-amber-glow`       | Outer bloom — low opacity on light bg.            |
| Warm cream / glass tint| `#1A1310` | `#FFF3E8` | `--vis-cream`            | Dark = warm-charcoal glass tint; light = page tint.|
| Smoked glass brown     | `#2A1812` | `#EFE1D5` | `--vis-smoke`            | Upper orb dark glass overlay.                     |
| Page background        | `#050505` | `#F9F6F0` | `--gc-bg`                | Main application background. Page bg is NEVER pure white. |
| Elevated card surface  | `#0E0E0E` | `#FFFFFF` | `--gc-bg-elevated`       | Cards. Light theme cards MAY use pure `#FFFFFF` (only exception to no-white rule). |

Source-of-truth tokens: `prototype/src/tokens.js` (`TOKENS.light` / `TOKENS.dark`) + CSS variables in `prototype/src/index.css`. When adding components, reference `var(--gc-accent)` / `var(--gc-bg-elevated)` / `var(--vis-amber-*)` — never inline new hexes.

### Aesthetic rules (non-negotiable)

- **Cinematic amber, not gaming neon.** The orange should read as molten amber + sunset emissive lighting. `#FF7A1A`, `#FF6A00`, `#FFC27A` do the heavy lifting. Avoid pure neon orange or oversaturated digital-orange.
- **Physically plausible glow falloff.** Layer three drop-shadows in distance order: `0 0 20px rgba(255,106,0,0.35)`, `0 0 60px rgba(255,106,0,0.18)`, `0 0 120px rgba(255,122,26,0.12)`. The far halo must be larger AND fainter — don't stack equal-intensity shadows.
- **Never use pure white `#FFFFFF` for page background or large fills.** Page bg is warm ivory (`#F9F6F0` / `#FFF3E8`). Two narrow exceptions: (a) elevated **card surfaces** in light theme may use `#FFFFFF` for pop against the warm bg; (b) white-on-orange CTA text — even there prefer `#FFF6EA`.
- **Shadows are peach-tinted, not gray.** In light theme, drop shadows are `rgba(169,68,0,0.X)` or `rgba(50,30,15,0.X)` — never neutral black. Backgrounds are beige-ambient (`#FFF3E8`), not stark white.

### Orb visual recipe

The signature orb is built from layered gradients (see `OrbFill` in `prototype/src/components/Primitives.jsx`):

```css
/* Body — warm radial centered bottom-of-orb */
background:
  radial-gradient(circle at 35% 30%, #FFC27A 0%, transparent 18%),
  radial-gradient(circle at 50% 80%, #FF7A1A 0%, #FF5E00 45%, #A94400 100%);

/* Liquid fill — 4-stop cinematic amber */
background: linear-gradient(180deg,
  #FFB36B 0%, #FF8A2A 30%, #FF6A00 70%, #D94E00 100%);

/* Smoked-glass top overlay (dark theme only) */
background: linear-gradient(180deg,
  rgba(255,255,255,0.16) 0%,
  rgba(255,255,255,0.05) 20%,
  rgba(42,24,18,0.82) 100%);
backdrop-filter: blur(24px);

/* Cinematic outer glow */
box-shadow:
  0 0 20px  rgba(255,106,0,0.35),
  0 0 60px  rgba(255,106,0,0.18),
  0 0 120px rgba(255,122,26,0.12);
```

## Context7 (Library Docs)

Always use the `context7` MCP tool when working with any library, framework, SDK, or API — even well-known ones (React Native, Expo, Spring Boot, Angular, Tailwind, etc.). Training data may be stale. Fetch current docs before writing library-specific code.

## 21st-dev/magic (UI Components)

Always use the `21st-dev/magic` MCP server for any UI work — new components, screens, layouts, or refinement of existing components. No exceptions.

- `mcp__magic__21st_magic_component_builder` — generate a new component from intent
- `mcp__magic__21st_magic_component_inspiration` — fetch design references before building
- `mcp__magic__21st_magic_component_refiner` — improve/polish an existing component
- `mcp__magic__logo_search` — fetch brand/company logos as JSX/TSX/SVG

Workflow for any UI task:
1. Call `21st_magic_component_inspiration` first to ground the design.
2. Call `21st_magic_component_builder` (new) or `21st_magic_component_refiner` (existing) to produce the component.
3. Adapt the output to the project's tokens (`var(--gc-*)`) and CSS-module conventions before committing.

This applies to `prototype/`, `admin-web/`, `client-app/`, and `trainer-app/`.

---

## Linear Issue Workflow

Whenever the user asks to work on any issue:

1. **Fetch the issue** from Linear using the MCP tools (`get_issue`) to load the full description and acceptance criteria.
2. **Set status to "In Progress"** (`save_issue`) before starting work.
3. **Implement** the feature/fix, verifying each acceptance criterion as you go.
4. **Set status to "In Review"** once implementation is complete and all acceptance criteria are met.

Never start coding without first fetching and reading the Linear issue.

---

## Commit Convention

Every commit message **must** be prefixed with the Linear story number in the format `VIS-<story-number>: <message>`. Ask the user for the story number if it is not known. Always push after committing.

Example: `VIS-42: add user authentication filter`

---

## Self-Correction Protocol

Whenever you make a mistake in this project — wrong assumption, incorrect tool usage, bad approach, or anything the user corrects — immediately add a lesson to the **Lessons Learned** section at the bottom of this file. Format:

```
- **[Category]** What went wrong → What to do instead.
```

Do this before moving on. The goal is that future sessions never repeat the same mistake.

---

## Resume Instructions

If context is lost mid-session, read `PLAN.md` first, then the spec at `docs/superpowers/specs/2026-05-04-vis-design.md`, then the plan file for the current phase. Pick up from the first unchecked task.

Phase plans are in `docs/superpowers/plans/`:
- `2026-05-04-phase-0-foundation.md` — auth + DB schema + all three app shells
- `2026-05-04-admin-web.md` — Angular admin web
- `2026-05-04-trainer-app.md` — React Native trainer app
- `2026-05-04-client-app.md` — React Native client app

Phases must execute in order (0 → 1 → 2 → 3). Check `PLAN.md` for current status.

---

## Project Structure

```
backend/          Java 21 + Spring Boot 3 REST API
client-app/       React Native — PT member app (iOS + Android)
trainer-app/      React Native — Personal Trainer app (iOS + Android)
admin-web/        Angular — Branch Staff and Owner web app
```

---

## Build & Run Commands

### Backend (`backend/`)

```bash
# Start local PostgreSQL
docker-compose up -d

# Run the backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/vis
export DATABASE_USER=vis
export DATABASE_PASSWORD=vis
export FIREBASE_CREDENTIALS_PATH=./firebase-service-account.json
export FIREBASE_PROJECT_ID=your-firebase-project-id
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=FirebaseAuthFilterTest

# Run a single test method
mvn test -Dtest=UserServiceTest#getByFirebaseUid_existingUser_returnsResponse

# Build Docker image
mvn package -DskipTests
docker build -t vis-backend .

# Run Flyway migrations manually
mvn flyway:migrate
```

### React Native Apps (`client-app/`, `trainer-app/`)

```bash
cd client-app  # or trainer-app

# Install dependencies
npm install
npx pod-install  # iOS only

# Run on iOS simulator
npx react-native run-ios

# Run on Android emulator
npx react-native run-android

# Start Metro bundler
npx react-native start
```

### Admin Web (`admin-web/`)

```bash
cd admin-web
npm install

# Dev server
ng serve

# Build for production
ng build --configuration production

# Run tests
ng test

# Run a single spec
ng test --include='**/firebase.service.spec.ts'
```

---

## Key Architecture

### Authentication Flow

Firebase Auth issues tokens in all three apps. Every API request includes `Authorization: Bearer <firebase-id-token>`. The backend `FirebaseAuthFilter` validates the JWT, extracts the Firebase UID, then `UserService` looks up the user's role and branch in PostgreSQL and attaches it to the Spring Security context.

A user with a valid Firebase token but no matching database record is treated as pending (not registered by admin yet).

### Multi-Branch Scoping

Every core entity is scoped to a `branch_id`. The roles enforce query scope:
- `STAFF` — can only read/write data for their `branch_id`
- `TRAINER` — same branch scope
- `CLIENT` — same branch scope
- `OWNER` — no branch filter, sees all branches

Branch enforcement is applied at the service layer, not controller layer.

### Backend Package Structure

```
in.vis/
  config/       FirebaseConfig, SecurityConfig, CorsConfig
  filter/       FirebaseAuthFilter (validates JWT, sets SecurityContext)
  model/        JPA entities (Branch, User, ...)
  enums/        Role (CLIENT | TRAINER | STAFF | OWNER)
  repository/   Spring Data JPA repositories
  service/      Business logic + branch enforcement
  controller/   REST endpoints
  dto/          Request/response records
  exception/    GlobalExceptionHandler, typed exceptions
```

### Testing Approach

- Unit tests: Mockito for services and filter logic
- Controller tests: `@WebMvcTest` slice tests with `MockBean` — Firebase filter is mocked to skip JWT validation
- Integration tests: Testcontainers PostgreSQL (configured via `spring.datasource.url=jdbc:tc:postgresql:16:///vis` in `application-test.properties`)

### Firebase Config Files

Firebase config is not stored in code:
- Android: `google-services.json` → `android/app/`
- iOS: `GoogleService-Info.plist` → `ios/`
- Backend: `firebase-service-account.json` in `backend/` (gitignored)

### Key Constants

- `app.owner-name=vis` in `application.properties` — change in one place for a different gym chain
- Backend API base URL in React Native apps is `http://localhost:8080` in dev, Cloud Run URL in production
- Angular environments are in `admin-web/src/environments/`

### Active Session Real-Time Sync

Both Trainer App and Client App can update the same session simultaneously (set rows). The spec specifies last-write-wins per set row. This is not yet implemented — flag the approach (polling vs WebSocket) when reaching Phase 2/3.

---

## Out of Scope (v1)

- In-app payments or payment gateway (payments are in-person; staff log amounts in Admin Web)
- Group classes
- In-app messaging (WhatsApp handles trainer–client communication)
- Apple Health / Google Fit data write-back (read-only on device, not stored in backend)
- Video content
- Member-facing web portal

---

## Lessons Learned

- **Prototype styling** Used inline `style={{}}` objects throughout all JSX components → Write all static styles in CSS modules (`.module.css`). Only use inline styles for truly dynamic computed values (percentage widths, SVG dashoffsets, data-driven color selection). Use CSS custom properties (`var(--gc-*)`) for all token values so the cascade does the work.

- **Linear MCP** `save_cycle` does not exist — the MCP server only supports `save_milestone`. To create actual Cycles (sprints), the user must create them in the Linear web UI; then issues can be assigned via MCP. Do not promise cycle-based sprints without confirming the tool exists first.
- **Linear issue list size** Fetching all team issues with `list_issues` at limit 250 can exceed token limits (~87K chars). Fetch by project or filter by specific criteria instead of pulling the full team backlog at once.
- **Linear title consistency** When creating issues across phases, verify the separator convention (colon vs dash) matches all other issues in the same phase before saving. Inconsistencies require a second pass of `save_issue` calls to fix.
- **BMad stories must be mirrored to Linear** Every BMad story created via `bmad-create-story` or `bmad-dev-story` must have a corresponding Linear issue created in the matching Epic project. Create it at story start (In Progress), update to In Review when dev done, update to Done after code review passes. Never complete a story workflow without syncing Linear.
