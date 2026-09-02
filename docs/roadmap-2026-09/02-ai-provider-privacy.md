# S02 - AI Provider Path, Privacy, Failure Handling

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`, `01-enter-text-actions.md`
- S01 text-action classes/tests
- `docs/privacy-notice.md`, `docs/play-data-safety-draft.md`
- `KeyboardPreferences.java`, `EditorPolicy.java`, `S3KeyboardService.java`

## Scope
1. Keep the S01 local deterministic correction as a no-network fallback.
2. Add a provider interface with explicit request/result/error types; UI and InputConnection code must not depend on a vendor SDK.
3. Support correct/polish/shorter/polite/translate through the provider abstraction.
4. Add user-controlled enablement, provider configuration surface, timeout/cancel, retry, and clear error states.
5. Never transmit password/sensitive/remote/raw-key content; enforce this below UI level as well as in UI.
6. Send only the bounded text selected by S01, not surrounding unrelated content.
7. Do not silently apply results: show concise before/after or replacement preview, then Apply/Cancel; preserve Undo.
8. Do not persist prompts/results by default. If any diagnostics are retained, store metadata only and document it.
9. Add fake-provider tests for success, timeout, cancellation, malformed/empty result, provider unavailable, and sensitive-field denial.

## Runtime verification
- provider disabled: local correction still works.
- provider enabled with test/fake endpoint: each action previews and applies correctly.
- network/provider failure leaves editor text untouched.
- cancellation leaves editor text untouched.
- privacy UI and actual request gate agree.

## Done gate
Privacy notice/data-safety draft match implemented behavior; automatic checks and runtime evidence pass. Mark S02=DONE and update handoff.