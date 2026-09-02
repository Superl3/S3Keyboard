# S03 - App Profiles and State Indicators

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- `EditorPolicy.java`, profile/catalog classes, `KeyboardPreferences.java`
- `S3KeyboardService.java`, `MainActivity.java`, settings UI helpers
- `docs/remote-mode.md`

## Scope
1. Audit existing automatic editor/app policy and package overrides before adding UI.
2. Add per-app override storage with schema/versioning and safe defaults.
3. Expose current effective profile: standard/browser-search/webview/messaging/password/number/url/email/remote.
4. Add “Always use for this app” overrides for language preference, number row, composing/text conveniences, remote mode where valid.
5. Add compact runtime indicators for active mode only: Hangul/English, Dingul/QWERTY, Remote, Caps, one-finger.
6. Keep indicators visually minimal and theme-safe; never obscure keys or alter touch bounds.
7. Add reset-to-auto per app and global clear overrides.
8. Add tests for precedence: editor policy < app override < hard sensitive-field restrictions.

## Runtime verification
- Chrome profile reflects browser/search behavior.
- Messages profile reflects messaging behavior.
- password/number restrictions cannot be overridden unsafely.
- remote app preset auto-enters/exits without leaking state to normal apps.
- indicators track real runtime state and disappear when inactive.

## Done gate
Automatic checks + real-app runtime evidence pass. Mark S03=DONE and update handoff.