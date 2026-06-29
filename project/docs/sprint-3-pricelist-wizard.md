# Sprint 3 Pricelist Wizard

## Purpose

The pricelist creation wizard replaces the old single-page creation flow for new pricelists with a backend-persisted multistep process. Each step saves to the server, so a creator can leave the application, log out, and continue an unfinished draft later from the draft list.

Existing pricelist lifecycle, edit, status, version, and offer flows remain separate. The old edit form is still used for editing existing draft pricelists and for backwards compatibility with the legacy `/api/pricelists` endpoints.

## Backend Endpoints

All wizard endpoints are under `/api/cenovnici` and require `ROLE_PRICELIST_CREATOR`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/cenovnici/wizard` | Start a new backend draft wizard. |
| `GET` | `/api/cenovnici/wizard/drafts` | Return unfinished drafts for the current user. |
| `GET` | `/api/cenovnici/{id}/wizard` | Load the current wizard state for a draft. |
| `PUT` | `/api/cenovnici/{id}/wizard/basic-info` | Save region, segment, currency, and period. |
| `PUT` | `/api/cenovnici/{id}/wizard/team-access` | Save private/team access. |
| `PUT` | `/api/cenovnici/{id}/wizard/items` | Save selected product variants. |
| `PUT` | `/api/cenovnici/{id}/wizard/thresholds` | Save item quantity thresholds. |
| `GET` | `/api/cenovnici/{id}/wizard/summary` | Load review summary and validation messages. |
| `POST` | `/api/cenovnici/{id}/wizard/finish` | Complete the wizard after final validation. |

## Wizard Steps

1. `BASIC_INFO`
   Saves region, customer segment, ISO currency, period start, and period end.

2. `TEAM_ACCESS`
   Saves either private access (`teamId = null`) or a selected team. The selected team must be accessible to the current user.

3. `ITEMS`
   Saves selected active product variants. Duplicate variants are rejected.

4. `THRESHOLDS`
   Saves thresholds per item. Thresholds must be consecutive, non-overlapping, have positive prices, and end with an open-ended final threshold.

5. `REVIEW`
   Loads backend summary and validation messages. The user can finish only when the summary is ready.

6. `COMPLETED`
   Marks the wizard complete while the pricelist remains in `DRAFT`.

## Validation

Basic info validates required region, customer segment, currency, period start, period end, and period order.

Team access allows private drafts and validates that selected teams belong to or include the current user.

Items validate at least one active variant and no duplicate variants.

Thresholds validate that every selected item has thresholds, quantities are continuous, the final threshold is open-ended, and higher-volume thresholds do not increase price.

Finish runs final validation using the existing pricelist business rules, including active variants, threshold validation, and blocking overlap checks against `IN_REVIEW` and `ACTIVE` pricelists.

Validation and business-rule errors are returned as JSON with an `error` message and are displayed in the current wizard step.

## Draft Continuation

Unfinished wizard drafts are stored on the backend as `DRAFT` pricelists with `creationCompleted = false` and a `creationStep`.

After logout/login, the frontend loads unfinished drafts with `GET /api/cenovnici/wizard/drafts`. The pricelist list page displays the draft id, available basic info, current step, last edited date, and a `Nastavi kreiranje` button. Continuing a draft opens `/pricelists/create/{id}` and reloads the current wizard state from the backend.

Browser local state is not the source of truth.

## Lifecycle Relation

The wizard only creates and completes a `DRAFT` pricelist. Completing the wizard does not publish the pricelist.

Lifecycle remains:

`DRAFT -> IN_REVIEW -> ACTIVE -> ARCHIVED`

An incomplete wizard draft cannot move from `DRAFT` to `IN_REVIEW`. After the wizard is finished, the owner can submit the completed draft for review through the existing status transition flow. Existing owner/team access, inactive variant, overlap, and status-transition rules still apply.

## Manual Demo Script

1. Log in as a user with `ROLE_PRICELIST_CREATOR`.
2. Open the content workspace and click `Create new pricelist`.
3. Confirm the wizard starts and a draft id is shown.
4. In `Osnovni podaci`, choose a region, enter customer segment, currency, period start, and period end.
5. Click `Sacuvaj i nastavi` and confirm the wizard advances to team access.
6. Select private access or choose an available team, then save and continue.
7. Add one or more active product variants in the items step. Confirm duplicates are prevented in the UI.
8. Save and continue to thresholds.
9. Add thresholds for every item. Leave the final threshold max empty for an open-ended range.
10. Enter invalid thresholds once, such as a gap or non-open-ended final threshold, and confirm the backend error appears in the step.
11. Correct thresholds and save.
12. Leave the page or log out before finishing.
13. Log in again as the same creator.
14. Open the pricelist list page and confirm the unfinished draft appears.
15. Click `Nastavi kreiranje` and confirm the wizard resumes from the backend current step.
16. Open review, confirm the summary loads, and click `Zavrsi`.
17. Return to the pricelist list and submit the completed draft to review.
18. Confirm the status changes to `IN_REVIEW`.

## Acceptance Checklist

- Multiple wizard steps are implemented.
- Each step persists through a backend API endpoint.
- Drafts can be continued after logout/login.
- Incomplete drafts stay in `DRAFT`.
- Incomplete drafts cannot move to `IN_REVIEW`.
- Final validation uses existing pricelist business rules.
- Frontend follows the prototype flow as closely as practical inside the existing app design.
- Backend wizard/lifecycle tests cover continuation, authorization, validation, and submit guards.
- Frontend production build passes.

## Verification Notes

- Frontend build command: `npm.cmd run build`.
- Backend test command: `mvn test` from `project`, or the equivalent IDE/Maven runner when Maven is available on PATH.
- Manual verification requires a running backend, authenticated pricelist creator, configured regions, available product variants, and optionally teams.
