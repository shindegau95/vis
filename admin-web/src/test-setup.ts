import '@angular/compiler';
import '@analogjs/vitest-angular/setup-zone';
import { setupTestBed } from '@analogjs/vitest-angular/setup-testbed';

// admin-web stays on zone.js for Story 1c.1.
// Zoneless change detection is a deliberate future-architecture decision (Story 1c.2+).
setupTestBed({
  zoneless: false,
});
