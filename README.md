# 뉴딩굴키보드 연구 프로토타입

This workspace contains a clean-room Android input method prototype for studying a Korean gesture-keyboard UX. It is built from the described interaction model and screenshot, not from proprietary APK code or assets.

The prototype includes Dingul-style Hangul gesture input, English QWERTY input,
Windows remote-mode key mapping, theme/icon customization, and English QWERTY
tap-typing assistance for typo suggestions, quick punctuation, and word-level
editing.

## Setup

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\install-android-env.ps1
```

The setup script installs portable tools under `.android-tools/`:

- Temurin JDK 17
- Android SDK command-line tools
- Android platform tools
- Android 35 platform and build tools
- Gradle 8.10.2 wrapper

## Build

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\build-debug.ps1
```

The debug APK is produced at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

For a closed-beta release build, provide signing properties outside source control and run:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\build-release.ps1
```

Expected Gradle properties:

```text
HANGUL_IME_KEYSTORE=C:\path\to\closed-beta.jks
HANGUL_IME_KEYSTORE_PASSWORD=...
HANGUL_IME_KEY_ALIAS=...
HANGUL_IME_KEY_PASSWORD=...
```

## Verify

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1
```

This runs the Hangul automata unit tests and rebuilds the debug APK.
It also validates the built-in theme JSON catalog and checks that the generated
web theme index is current.

For direct local Gradle use with an existing Android SDK/JDK, see
`docs\development.md`.

Before pushing theme/icon work, also run:

```powershell
rtk node --check web-theme-builder/app.js
rtk powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\render-theme-previews.ps1
rtk git diff --check
```

## Web Theme Builder

Open `web-theme-builder\index.html` in a browser to edit schemaVersion 1 theme
JSON that can be imported by the app theme editor.

Built-in themes use `themes\*.json` as the source of truth. After adding or
editing one, run:

```powershell
rtk node tools/sync-themes.mjs --generate --report
```

This validates theme JSON, regenerates Android built-in presets, and refreshes
the web builder's generated theme contract and theme index.

External modifier/display icon pack authoring is documented in
`docs\icon-pack-import.md`.

## Icon Assets

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\generate-icons.ps1
```

The icon pipeline reads clean-room path data from `tools\icons\icons.json` and generates Android vector drawables under `app\src\main\res\drawable\`.

## Device Install

After enabling USB debugging on a device or starting an emulator:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\install-debug.ps1
```

Then open the app once, enable `뉴딩굴키보드` in Android keyboard settings, and select it as the active input method.

## 사용 안내

- 앱 설정은 `빠른 시작 → 연속 입력 → 레이아웃 → 입력감 → 표시` 순서의 단계형 화면으로 구성된다. 일반 세로 화면에서는 앱 제목, 검색, 단계 탐색을 상단에 고정하고 선택한 단계의 내용만 스크롤한다. 검색은 단계 키워드뿐 아니라 실제 화면의 옵션 이름과 도움말을 인덱싱하며, `키보드 높이`처럼 여러 단어도 순서와 무관하게 찾는다. 가로 화면처럼 높이가 낮거나 입력 테스트 키보드가 열린 경우에는 화면 전체가 하나의 스크롤로 전환된다. 단계나 검색 결과를 바꾸면 해당 내용의 시작점으로 이동하며, 세부 옵션은 용도별 접이식 묶음 안에 정리되어 있다. 화면이 회전하거나 다시 생성돼도 선택 단계, 전체 보기, 검색어를 복원한다.
- 레이아웃의 패딩과 키 간격은 `− / 값 / +` 아이콘 조절기로 1dp씩 바꾸며, 최솟값과 최댓값에서는 해당 방향 버튼이 비활성화된다.
- `빠른 시작`은 Android 키보드 상태를 `활성화 필요 → 전환 필요 → 사용 중`으로 안내한다. 비활성 상태에서는 아직 의미가 없는 `전환`을 비활성화하며, 활성화 뒤에는 첫 버튼이 `키보드 관리`로 바뀐다. 빌드 정보와 디버그 옵션은 `Android/IME` 단계에 분리되어 있다.
- 빠른 시작에는 현재 테마 이름이 표시된다. 테마 선택 카드에서는 `현재 설정`과 QWERTY/Dingul 모양을 동시에 비교할 수 있으며, 테마 기본값 복원은 레이아웃·입력 설정이 유지된다는 확인을 거친다. 테마 편집기는 상단 실시간 미리보기와 전역/키별 편집 영역을 분리한다.
- 키보드 높이, 슬라이드 힌트, 입력 프리뷰와 사용자 글꼴은 선택 테마와 별도 설정이다. 이 값을 바꿔도 현재 테마 선택은 유지된다.
- Dingul 한 손가락 연속 입력의 동작과 연습 방법은 `docs\one-finger-input.md`에 정리되어 있다.
- `연속 입력` 단계의 `바로 연습`은 네 모음 키, 우측 특수열, 동일 키 재진입, 문장 입력을 7단계로 기기 안에서 저장 없이 비교해준다.
- 자동 검증 범위와 실기기에서 남은 1차 마무리 기준은 `docs\feature-audit.md`에서 확인할 수 있다.
- 실기기 판정은 `docs\manual-test-checklist.md`의 순서대로 진행하면 기본 배열, 네 인체공학 프리셋, 연속 입력, 편집기 호환성, QWERTY와 Remote 회귀를 한 번에 확인할 수 있다.
- 옵션 키를 탭하면 전체 앱 설정을 열고, 위로 밀거나 길게 누르면 빠른 설정을 연다. 빠른 설정은 손 배치, 연속 입력, 숫자줄, 테마를 먼저 표시하며 Remote와 진단 도구는 아래에 둔다. 내용이 화면보다 길 때만 패널 내부가 스크롤된다.
- 빠른 설정의 선택 버튼은 시각적 강조와 Android 접근성의 `selected` 상태를 함께 사용한다. 원격 전달 테스트는 한 행에 최대 4개, 48dp 높이 버튼으로 배치해 작은 화면에서도 단축키 이름과 터치 영역을 유지한다.
- TalkBack에서는 각 가상 키의 탭뿐 아니라 상·하·좌·우 밀기와 길게 누르기를 사용자 정의 작업으로 선택할 수 있다.

## Emulator Demo

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\demo-emulator.ps1
```

This installs the Android emulator packages if needed, creates a local AVD under `.android-tools\avd`, installs the debug APK, enables the IME, opens the inline test field, and saves a screenshot under `captures\`.

For best-effort closed-beta app coverage on the emulator:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\smoke-ime-apps.ps1
```

The smoke script opens the local practice field and captures input-method state for Chrome, Messages, and Keep when those packages exist on the emulator image.

## Current Prototype Scope

- Custom Android `InputMethodService`
- 5-direction tap/slide key handling: center, up, down, left, right
- Optional Dingul one-finger continuous input with center dwell selection, immediate directional commit, free-roam navigation, same-key re-entry protection, and three speed presets
- Long-press key slots, currently populated for English symbols and left empty for Hangul keys
- Preview overlay for the active key gesture
- Korean Hangul automata for consonant-vowel-final composition
- Automatic correction for cases such as vowel-start syllables and final consonants followed by a vowel
- Internal Hangul/English mode toggle
- Selectable Hangul Dingul/QWERTY and English QWERTY/Dingul layouts
- English QWERTY layout with tap lowercase, up-slide uppercase, and long-press symbols
- English QWERTY tap assistance with current-word suggestions, safe exact typo correction on boundary keys, spacebar quick punctuation, and backspace word-delete slide
- Per-language top number row, default off for Hangul and on for English
- Clean-room generated vector icon pipeline for command keys and settings action buttons
- Conventional keyboard usability hacks: hit slop, touch Y offset, locked slide direction, haptic feedback, delete/cursor repeat, spacebar cursor movement, contextual Enter labels, and English double-space period
- Queued haptic ticks with adjustable duration/gap, plus bounded touch/slide correction learned from immediate deletes
- Local redacted typing pattern logging for future typo correction experiments; data stays on device and resets with input correction
- Theme system with JSON-sourced built-in presets, Dingul alpha/mod/mod-inverted role colors, per-key foreground/background overrides, optional number-row alpha/mod/accent styling, key display overrides, modifier icon packs, imported icon/display pack metadata, visual effects, and preview parity scripts
- Launcher settings for handedness, left/right margins, keyboard height, per-language number row, theme colors, key roundness/gap, Android input-method settings, and input-method picker
- Closed beta trust work: local privacy notice, Play Data safety draft, ASCII-capable IME subtype, explicit field policies, `TYPE_NULL` raw-key fallback, debug-gated demo overrides, and release build hardening

## Agent Handoff

Future coding contexts should start with `AGENTS.md` and `docs\agent-workflow.md`.
They describe the source map, theme/icon workflow, test expectations, and device
install flow.

## Closed Beta Notes

- Privacy notice draft: `docs\privacy-notice.md`
- Play Data safety draft: `docs\play-data-safety-draft.md`
- Closed beta readiness notes: `docs\closed-beta-readiness.md`
