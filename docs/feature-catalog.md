# 뉴딩굴키보드 전체 기능 카탈로그

Updated: 2026-09-03 KST
Scope: 현재 `main` 소스, 설정 문자열, 기능 감사, Remote/레이아웃 문서와 2026-09 S01-S09 완료 상태를 기준으로 정리한다.

## 상태 표기

- **완료**: 저장소 구현과 자동 검증이 완료된 기능.
- **런타임 확인**: 에뮬레이터 또는 실제 앱 surface에서 동작 증거까지 확보한 기능.
- **외부 확인 필요**: 구현은 완료됐지만 실제 Windows 수신, 실기기 TalkBack 등 저장소 밖 검증이 남은 기능.
- **연구/실험**: 코드와 설정 경로는 있으나 closed-beta 핵심 기능으로 간주하지 않는 실험 surface.

## 1. 2026-09 제품 확장에서 개선된 핵심 기능

### S01 · 되돌릴 수 있는 텍스트 작업
- Enter 제스처에서 선택 영역 또는 현재 문장을 대상으로 `교정 / 다듬기 / 짧게 / 공손하게 / 번역` 작업 surface를 연다.
- 로컬 결정론적 교정은 네트워크 없이 동작한다.
- 적용 전후 원문 범위와 선택 위치를 저장해 `원문 복원` 및 Undo로 정확히 되돌릴 수 있다.
- 비밀번호, 숫자, raw-key, Remote 등 민감하거나 비호환인 입력란에서는 기능 자체를 차단한다.

### S02 · AI/provider 경로와 개인정보 경계
- UI와 입력 코드가 특정 AI 업체에 종속되지 않는 provider abstraction을 사용한다.
- 현재 제공되는 테스트 provider는 온디바이스 전용이며 외부 네트워크 provider는 포함하지 않는다.
- provider 결과는 자동 적용하지 않고 반드시 미리보기 후 사용자가 `적용`해야 한다.
- timeout, cancel, malformed/empty result, unavailable provider를 원문 변경 없이 처리한다.
- 요청 대상은 최대 2,048자로 제한되고 요청/결과는 저장하지 않는다.
### S03 · 앱별 입력 프로필
- 앱 패키지별로 언어, 숫자줄, composing, 텍스트 편의, Remote를 `자동/켜기/끄기` 방식으로 재정의한다.
- 비밀번호/숫자/raw 입력의 안전 정책은 사용자 앱 프로필보다 우선한다.
- 앱별 프로필의 실제 적용 상태는 빠른 설정과 키 자체 상태로 확인할 수 있으며, 일반 입력에서는 `한글/영문 · Dingul/QWERTY` 전용 상단 상태 행을 예약하지 않는다.
- Chrome URL과 Google Messages 실제 입력 surface, Remote 자동 진입/이탈까지 런타임 확인했다.

### S04 · 통합 Text Tools
- 최근 클립보드, 저장/고정 문구, 예약 문구를 하나의 패널에서 제공한다.
- 항목 고정/해제, 이름 변경, 내용 편집, 삭제, 순서와 영속 저장을 지원한다.
- 클립보드 기록은 최대 10개, 항목당 4,096자 제한을 적용한다.
- 민감 입력란에서는 패널 진입과 기록 사용을 모두 차단한다.

### S05 · 버전형 백업/복원
- 설정, 앱별 프로필, 사용자/외부 테마, Text Tools/예약 문구, 선택적 로컬 환경설정을 JSON 파일로 내보내고 복원한다.
- 스키마 버전과 migration 경로를 두고, 항목별 선택 복원과 선택 초기화를 지원한다.
- malformed 파일은 기존 설정을 손상시키지 않고 거부하며 복원은 원자적으로 적용한다.
- 클립보드 기록, 진단/입력 학습 로그 같은 민감·일시 데이터는 백업 대상에서 제외한다.

### S06 · 테마 관리
- 42개 내장 테마를 검색하고 `전체 / 즐겨찾기 / 최근`, `라이트 / 다크`, 재질별로 필터링한다.
- 테마 즐겨찾기와 최근 사용 이력을 저장한다.
- 시스템 라이트/다크 모드에 연결할 테마 페어를 지정해 자동 전환할 수 있다.
- A/B 두 테마를 지정해 즉시 번갈아 적용하며 비교할 수 있다.
### S07 · Windows Remote 모드 제품화
- Parsec, Moonlight, Microsoft RDP, Chrome Remote Desktop, Steam Link, AnyDesk, TeamViewer 계열을 인식하는 Remote 프로필을 제공한다.
- Remote 중에는 PC형 QWERTY surface와 숫자줄을 강제하되 사용자의 일반 한글/영문·테마 설정은 덮어쓰지 않는다.
- `Ctrl / Win / Alt / Shift`를 one-shot 또는 lock modifier로 조합하고 한 번에 모두 해제할 수 있다.
- Esc, Tab, Shift+Tab, Ctrl+Tab, Alt+Tab, Insert/Delete/Home/End/PgUp/PgDn, F1-F12, Ctrl+Enter, 방향키와 Windows IME 전환 단축키를 제공한다.
- Android에서 생성/수락된 key-event 수와 사용자가 표시한 실제 성공/실패를 분리한 호환성 리포트를 만든다.
- 실제 Windows 세션이 모든 키를 받은지는 **외부 확인 필요**다.

### S08 · 안전 진단과 개인정보 정리
- release-safe 진단은 입력 원문, 클립보드 내용, 저장 문구, AI 요청/결과, provider 자격 증명, raw package name, 좌표를 수집하지 않는다.
- 최근 세션의 whitelist snapshot과 최대 12개의 동작 `범주`만 보존한다.
- 진단 내용을 복사하거나 JSON 문서로 내보낼 수 있다.
- `진단/입력 학습 초기화`는 진단 snapshot, 동작 범주, 터치 보정/학습 로그만 지우고 일반 설정·테마·Text Tools는 유지한다.
- Privacy notice, Play Data Safety draft, closed-beta 문서와 실제 저장 동작을 맞췄다.

### S09 · 통합/릴리스 게이트
- 표준 `check.ps1`를 호출한 현재 디렉터리와 무관하게 repo root에서 동작하도록 고쳤다.
- 42개 테마 계약, 4개 재질, 70개 설정 소비 지점, unit test/lint/debug assemble을 최종 gate에서 통과했다.
- Dingul, 일반/민감/웹 입력 field, Chrome, Google Messages와 4개 재질의 영문/한글 런타임 geometry를 다시 확인했다.
- release script도 repo root 기준으로 실행되며, 실제 keystore 정보가 없으면 `verifyClosedBetaSigning`에서 명시적으로 중단한다.

## 2. 전체 사용자 기능 카탈로그
### 2.1 한글 Dingul 입력
- 5방향 `탭 / 위 / 아래 / 왼쪽 / 오른쪽` 제스처를 사용하는 Dingul 배열을 제공한다.
- 초성·중성·종성, 복합 종성, 모음 문맥을 처리하는 자체 Hangul automata로 조합한다.
- 모음 시작 음절, 종성 뒤 모음 등 Dingul 입력 흐름에서 필요한 자동 재조합을 처리한다.
- 우측 특수 기능열과 하단 제어열, 네 종류의 방향 모음 키를 지원한다.
- `..` 위치를 선택적으로 `공백/전송` 키처럼 표시하면서 방향 모음 `ㅛ/ㅑ/ㅠ/ㅕ`는 유지할 수 있다.
- 한글 배열을 Dingul 또는 QWERTY 방식으로 선택할 수 있다.
- 한글 숫자줄은 독립적으로 켜고 끌 수 있다.

### 2.2 한 손가락 연속 Dingul 입력
- 첫 터치 뒤 손가락을 떼지 않고 `선택 → 확정 → 자유 이동 → 다음 키 선택`을 반복한다.
- 첫 키는 중앙 대기 또는 즉시 방향 슬라이드로 확정하고, 후속 키는 중앙 dwell 후 탭값 또는 방향값을 확정한다.
- `안정형 / 균형형 / 빠른형` 속도 preset과 세부 dwell 시간 조절을 제공한다.
- 중앙 선택 영역에 진입/이탈 hysteresis와 이동 안정 범위를 적용해 손떨림과 천천히 스치는 오선택을 줄인다.
- 방향 확정 뒤 실제 손가락 위치를 다음 gesture origin으로 사용하고 동일 키 재진입 보호를 제공한다.
- 값이 없는 방향이나 내부 `NOOP`은 문자를 만들지 않고 자유 이동으로 복귀한다.
- 좁은 우측 특수열은 연속 입력 중 tap-only로 유지해 방향 오인식을 막는다.
- 설정 안의 7단계 `바로 연습`에서 네 모음군, 특수열, 같은 키 재선택과 문장 입력을 저장 없이 연습할 수 있다.
### 2.3 영문 QWERTY 입력
- 영문 QWERTY와 영문 Dingul 레이아웃을 선택할 수 있다.
- QWERTY는 소문자 tap, 위 방향 대문자, 매핑된 기호/보조 입력과 Shift를 지원한다.
- Shift tap은 다음 한 글자에만 적용되고 Shift long-press는 Caps Lock을 전환한다.
- 영문 숫자줄은 독립적으로 켜고 끌 수 있으며 기본값은 켜짐이다.
- 현재 단어 후보 표시와 안전한 exact typo correction을 제공한다.
- 경계 키에서 자동 교정을 적용할 수 있고 사용자가 이 기능을 끌 수 있다.
- 영어에서 space 두 번으로 마침표를 입력하는 옵션을 제공한다.
- spacebar 좌우 이동으로 cursor를 옮기고, backspace 방향 제스처로 단어 단위 삭제를 지원한다.

### 2.4 공통 편집/삭제/Enter 동작
- delete와 cursor 이동은 길게 누를 때 반복 입력되며 시작 지연과 반복 간격을 설정할 수 있다.
- 선택 영역이 있으면 선택 텍스트를 우선 교체/삭제하고 Hangul 자동 결합과 영문 추천은 개입하지 않는다.
- grapheme 단위 삭제로 emoji modifier, 국기, ZWJ sequence, 결합 문자, 분해형 한글을 가능한 한 한 덩어리로 지운다.
- API 23 fallback도 supplementary code point를 UTF-16 단위로 올바르게 계산한다.
- Enter 표시는 입력란의 IME action에 맞춰 전송/검색/완료/다음/이동/줄바꿈으로 바뀐다.
- raw-key/Enter fallback은 편집기가 key event를 받지 않았을 때만 text fallback을 사용해 중복 입력을 막는다.
- `TYPE_NULL` 입력란에는 ASCII raw-key fallback을 제공한다.

### 2.5 입력란별 안전/호환 정책
- 일반, password, number, phone, date, URL, email, web-edit, search, multiline, raw-key surface를 구분한다.
- password/number-like 입력은 저장된 언어 모드를 바꾸지 않은 채 런타임에서 ASCII/QWERTY와 숫자줄을 우선한다.
- composing이 불안정한 URL/web/raw 계열은 commit-only 또는 raw-key 경로를 사용한다.
- 민감 필드에서는 Text Tools, 음성 입력, 텍스트 provider, 클립보드 기록 등 원문 노출 가능 기능을 차단한다.
### 2.6 레이아웃과 인체공학
- `왼손 / 양손 / 오른손` 배치와 Dingul 인체공학 preset `기존 / 안정 / 인체공학 / 적극 보정`을 제공한다.
- Dingul 12키 가운데 정렬, 좌측 보조 기능열, 우측 기능열 축소, 균일 간격, 터치 판정 보정을 조합할 수 있다.
- 한글/영문 키보드 높이, 좌우 padding, 상단/하단 padding, 키 간격, 숫자줄 간격과 Dingul 특수열 폭을 독립 조절한다.
- Layout Editor에서 preview handle, `-/값/+`, 직접 입력으로 geometry를 편집하고 `적용` 전까지 실제 설정을 바꾸지 않는다.
- preview와 실제 IME가 같은 `KeyboardLayoutCalculator` geometry를 사용한다.
- 시각적인 key gap과 실제 hit rectangle을 분리해 외형 조절이 터치 판정을 불필요하게 좁히지 않는다.
- hit slop, touch Y offset, slide 시작 거리, Dingul 모음 제스처 민감도를 조절할 수 있다.
- palm rejection과 방향 lock, key surface 전환 시 진행 중 gesture 종료를 통해 잘못된 연속 입력을 줄인다.

### 2.7 햅틱과 입력 보조
- 전체 입력 진동을 켜고 끌 수 있고 입력 종류별 차등 햅틱을 선택할 수 있다.
- 햅틱 길이와 tick 사이 간격을 조절하며 이벤트를 queue해 짧은 연속 입력에서도 피드백을 유지한다.
- 즉시 delete 패턴을 이용해 bounded touch/slide correction을 기기 안에서 학습할 수 있다.
- 입력 보조 모드는 `사용자 지정 / 깔끔 / 학습 / 디버그` 구성을 제공한다.
- 터치 보정과 입력 로그는 로컬에만 두고 설정에서 각각 또는 함께 지울 수 있다.

### 2.8 빠른 설정과 런타임 제어
- 옵션 키 tap은 전체 앱 설정을 열고, 위 slide 또는 long-press는 IME 내부 빠른 설정을 연다.
- 빠른 설정 상단에는 사용 손, 연속 입력, 숫자줄, 현재 테마 등 일반 기능을 우선 배치한다.
- 현재 앱 입력 프로필에서 언어/숫자줄/composing/텍스트 편의/Remote 값을 즉시 확인·변경하고 앱별 설정을 자동 상태로 되돌릴 수 있다.
- Remote test와 테마·진단 도구는 접힌 보조 영역으로 두고 화면 높이를 넘을 때만 내부 scroll한다.
- 선택 버튼은 시각 강조와 Android accessibility `selected` 상태를 함께 노출하며 최소 48dp touch target을 사용한다.
### 2.9 Text Tools, 클립보드, 예약 문구, 음성 입력
- 좌측 보조열에서 `클립보드 / Android 음성 입력 / Undo / 도구`에 빠르게 접근할 수 있다.
- Text Tools는 저장/고정 문구, 예약 문구, 최근 클립보드를 한 surface에 통합한다.
- 저장 항목의 pin/unpin, rename, edit, delete와 최근 기록 전체 삭제를 지원한다.
- 예약 문구는 tap 및 좌/우/위 slide 슬롯에 사용자가 원하는 문구를 배정할 수 있다.
- 클립보드에서 붙여넣기 전 진행 중 Hangul composition을 확정해 편집기 상태 충돌을 줄인다.
- 시스템 Undo를 지원하지 않는 편집기에서는 무반응 대신 안내를 표시한다.
- Android 표준 음성 인식 Activity를 호출하고, 동일 앱의 입력 연결이 복구된 뒤에만 결과를 commit한다.
- password/raw-key/Remote surface에서는 음성 입력을 시작하지 않는다.

### 2.10 테마 선택·편집·외부 테마
- 내장 테마 소스는 42개 JSON preset이며 Android 생성 소스와 Web Theme Builder 계약을 동기화한다.
- 지원 재질은 정확히 `solid / soft_keycap / frosted / acrylic` 네 가지다.
- 테마 선택 카드에서 현재 설정과 QWERTY/Dingul preview를 동시에 비교한다.
- 전역 Alpha/Modifier/Accent 역할색, 눌림색, 키보드 배경, border/depth, panel gradient를 편집한다.
- roundness, border width, visual gap, depth height, surface gradient, 주/보조 글자 크기와 굵기/기울임을 조절한다.
- Android 플랫폼 blur 기반 frosted/glass 표현을 지원하며 화면 capture나 Accessibility 화면 소스를 사용하지 않는다.
- 키별 foreground/background override와 숫자줄 역할 배치, Space/물음표/command 계열 accent placement를 지원한다.
- modifier icon pack과 key display override pack을 적용할 수 있고 사용자 변경 시 `현재 설정`으로 전환한다.
- 외부 JSON 테마 폴더, 클립보드 JSON import/export, 현재 테마 저장을 지원한다.
- 키보드 사진 또는 일반 이미지에서 테마 JSON을 만들기 위한 AI용 prompt 문구를 복사할 수 있다.
### 2.11 표시와 키 시각 요소
- 주/보조 글자, slide hint, spacebar hint와 초보자용 입력 preview를 각각 표시/조절한다.
- 사용자 글꼴은 기본, Noto Sans KR, Noto Serif KR, D2Coding 선택지를 제공한다.
- 테마 글꼴 크기/굵기를 따르거나 사용자 표시 설정을 별도로 유지할 수 있다.
- key face gradient, depth, point keycap, icon/display style과 motion effect 강도를 조절한다.
- input preview animation은 작은 scale-in과 release fade만 사용해 실제 입력 판정과 분리한다.
- 한/영 전환은 짧은 저강도 wash와 상태선 fade로 표시한다.

### 2.12 Windows Remote 키보드 상세
- 하단 기본 순서는 `Ctrl · Win · Alt · Space · Lang · Menu · Enter`이며 Remote 중에는 handedness와 무관하게 고정된다.
- modifier tap은 다음 키 1회에 적용되고 long-press는 lock이며, lock 중 같은 modifier tap으로 해제한다.
- Space slide는 상/하/좌/우 방향키를 보낸다.
- Lang tap은 설정된 Windows IME shortcut, long-press는 Android 내부 한/영 전환이다.
- Enter long-press는 `Ctrl+Enter`를 보낸다.
- 숫자줄 down slide `1..0`은 `F1..F10`, up slide는 `1=Esc, 9=F11, 0=F12`를 제공한다.
- QWERTY의 일부 key slide로 Tab, Shift+Tab, Ctrl+Tab, Alt+Tab, Insert/Delete/Home/End/PgUp/PgDn을 제공한다.
- Windows IME shortcut은 `Alt+Shift / Ctrl+Space / Win+Space / LanguageSwitch` 중 선택할 수 있다.
- Remote compatibility test pad와 JSON report로 local accepted-event와 manual Windows 결과를 함께 기록한다.

### 2.13 진단, 리포트, 로컬 데이터 제어
- 입력 이상 신고용 redacted issue report를 클립보드로 복사할 수 있다.
- Remote 호환성 report는 app family, shortcut별 전송 수, local complete 여부, manual pass/fail/unknown을 기록한다.
- release-safe diagnostics는 build/session/profile 상태와 허용된 action category만 기록한다.
- 로컬 데이터 화면에서 clipboard history, 입력 로그, touch correction, remote test log를 범위별로 삭제할 수 있다.
- `로컬 데이터 모두 삭제`와 진단/입력 학습 전용 reset을 구분해 불필요한 사용자 설정 손실을 막는다.
### 2.14 접근성
- 키보드 전체와 각 가상 key에 접근성 이름과 현재 mode 정보를 제공한다.
- TalkBack에서 tap뿐 아니라 `위 / 아래 / 왼쪽 / 오른쪽 / 길게`를 custom accessibility action으로 실행할 수 있다.
- 가상 key focus lifetime과 hit-bound 기반 focus 영역을 관리한다.
- 설정의 선택 button, 접이식 section, stepper는 selected/expanded 상태와 현재 값을 accessibility에 노출한다.
- 실제 기기에서 TalkBack traversal 순서와 gesture conflict는 **외부 확인 필요**다.

### 2.15 설정 앱과 onboarding
- 설정은 `빠른 시작 → 연속 입력 → 레이아웃 → 입력감 → 표시`를 중심으로 단계형 navigation을 제공하고 Remote/Android-IME/Text Tools 영역을 분리한다.
- 일반 세로 화면에서는 제목·검색·단계 navigation을 고정하고 현재 단계 내용만 scroll한다.
- 낮은 높이/가로/IME 연습 상태에서는 전체 scroll로 전환해 content 잘림을 줄인다.
- 설정 검색은 실제 option label, help text와 keyword를 index하고 여러 단어를 순서와 무관하게 찾는다.
- 회전/recreate 후 선택 단계, 전체 보기, 검색어를 복원한다.
- 빠른 시작은 Android IME 상태를 `활성화 필요 → 전환 필요 → 사용 중`으로 판정하고 다음 필요한 action만 강조한다.
- inline practice field에서 실제 IME를 바로 시험할 수 있다.
- build version/commit/build 정보를 Android/IME 단계에서 확인할 수 있다.

### 2.16 백업·개인정보·배포 안전장치
- Android 시스템 backup/device transfer 대상으로 로컬 keyboard preference와 입력 로그가 섞이지 않도록 차단한다.
- 개인정보 고지는 launcher 설정에서 접근 가능하고 Play Data Safety draft와 함께 관리한다.
- release build는 minify/resource shrink와 source control 밖 property 기반 signing을 사용한다.
- debug/demo intent override는 debuggable build에서 명시적 demo flag가 있을 때만 허용한다.
- ASCII-capable IME subtype과 구형 Android 호환 extra를 제공한다.
### 2.17 개발·검증 도구
- `scripts/check.ps1` 하나로 theme validation, material consistency, 설정 사용 감사, unit test, Android lint, debug APK build를 실행한다.
- `smoke-dingul-typing.ps1`로 실제 IME에서 Dingul key-action sequence를 검사한다.
- `smoke-ime-apps.ps1`로 synthetic field와 설치된 browser/messages/remote app surface의 IME selected/visible 상태를 기록한다.
- theme runtime capture는 QWERTY/Dingul geometry와 bottom delta를 manifest/summary와 함께 남긴다.
- Web Theme Builder에서 schema v1 JSON theme를 편집하고 Android preset/web index를 generator로 동기화한다.
- clean-room vector icon pipeline과 외부 icon/display pack authoring 경로를 제공한다.

### 2.18 연구/실험 입력 surface
- **투명 overlay 입력**: 입력창을 밀지 않고 keyboard를 화면 위에 겹치는 경로와 반투명/축소 스타일이 코드에 존재한다. 일반 IME의 기본값은 꺼짐이며 사용자가 명시적으로 켠 경우에만 사용한다.
- **시계형 radial 입력**: 한글 자음/모음/기능을 radial page로 선택하는 watch-oriented 입력 경로가 코드와 설정에 존재한다.
- 두 기능은 debug/demo testbed와 연결된 연구 surface를 포함하므로 이 문서에서는 closed-beta 핵심 완료 기능과 별도로 분류한다.

## 3. 현재 품질/검증 상태

- S01-S09 repository engineering: **완료**.
- 최종 canonical check: theme 42개/경고 0, material 4종 정합, KeyboardSettings 70 fields/unused 0, unit/lint/assembleDebug **PASS**.
- Dingul fresh smoke: 16 emitted actions **PASS**.
- 일반/password/number/URL/email/web-edit/search/multiline 및 설치된 Chrome/Google Messages field smoke **PASS**.
- solid/soft-keycap/frosted/acrylic 대표 theme의 English/Hangul runtime geometry `bottomDelta=0` **PASS**.

## 4. 저장소 밖에서 남은 출시 단계

- closed-beta signing keystore와 `HANGUL_IME_KEYSTORE`, `HANGUL_IME_KEYSTORE_PASSWORD`, `HANGUL_IME_KEY_ALIAS`, `HANGUL_IME_KEY_PASSWORD` 제공.
- Parsec/Moonlight/RDP 등 실제 Windows receiver에서 Esc/Tab/F-key/modifier/IME shortcut 수신 확인.
- 실기기에서 장시간 한 손가락 연속 입력과 TalkBack traversal/gesture 충돌 확인.
- 배포용 최종 developer entity/contact 정보를 privacy/distribution metadata에 반영.

이 네 항목은 현재 소스 구현 미완료가 아니라 keystore, 외부 receiver, 실제 기기, 배포 정보가 필요한 release/manual gate다.
