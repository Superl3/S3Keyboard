# 적응형 다음 키 터치 판정 계획

기준일: 2026-08-25

## 목표

딩굴 입력에서 사용자의 확정 입력과 명시적 교정 이력을 로컬로 누적하고,
현재 문맥에서 다음에 누를 가능성이 높은 키를 이용해 인접 키 경계의 터치
판정을 제한적으로 보정한다. 첫 번째 대표 문제는 `ㅎ`과 `. .` 공백 키의
경계 오입력이다.

이 기능은 화면에 보이는 키캡 크기를 바꾸지 않는다. 키의 안전 중심부는
항상 기존 기하학 판정을 따르고, 둘 이상의 키가 합리적인 후보가 되는 좁은
경계에서만 예측 점수를 사용한다.

## 기존 기반

새로운 원문 입력 수집기를 만들지 않고 다음 구현을 재사용한다.

- `HangulKeyboardView`: 터치다운/릴리스 좌표, 기하학 후보, shadow 후보와
  입력 동작을 수집한다.
- `TypingEventJournal`: 입력, 삭제, 대체 입력을 연결하고 `accepted_tap`,
  `accepted_slide`, `wrong_origin_key` 등의 라벨을 만든다.
- `TouchBiasStore`: 입력 학습 데이터를 앱 전용 `SharedPreferences`에 저장하고
  지연 flush와 학습 초기화를 제공한다.
- `DingulTouchProfile`: 키·동작별 입력/교정 횟수와 평균 터치 오프셋을
  집계한다.
- `LocalDataControlsController`: 사용자가 입력 로그와 터치 보정을 확인하고
  초기화하는 단일 경로를 제공한다.

현재 저널은 진단용 최근 이벤트 240개에 적합하지만, 매 터치마다 JSON 이벤트를
검색하는 장기 예측 모델로 사용해서는 안 된다. 저널의 확정 라벨을 소비하는
작고 제한된 집계 모델을 별도로 둔다.

## 핵심 설계

### 1. 의미적 키 ID

원문 문자열 대신 배열과 키 역할이 안정적으로 표현되는 ID를 사용한다.

```text
dingul:ㅎ:tap
dingul:space:tap
dingul:center-vowel:left
dingul:delete:tap
```

ID는 표시 라벨이나 테마와 분리한다. 배열이 바뀌면 별도 namespace를 사용하며,
테마·아이콘·표시 override는 학습 상태에 영향을 주지 않는다.

### 2. 전이 집계 모델

`NextKeyTouchModel`은 직전 확정 입력 하나를 문맥으로 사용하는 1차 Markov
모델로 시작한다.

```text
ContextKey
  layoutId
  keyboardMode
  previousKeyId
  previousAction

CandidateStats
  nextKeyId
  nextAction
  acceptedCount
  correctedToCount
  correctedFromCount
  lastUpdatedEpoch
```

오토마타 상태는 첫 버전의 필수 키로 사용하지 않는다. 한글에서 `ㅎ`은 초성과
종성 모두 유효하므로 조합 상태만으로 공백 의도를 확정할 수 없다. 필요하면
후속 단계에서 `empty`, `composing`, `committed`, `after-space`처럼 원문을
포함하지 않는 거친 상태 분류만 약한 점수로 추가한다.

### 3. 학습 표본 규칙

모델은 모든 출력 결과를 그대로 학습하지 않는다.

- `accepted_tap`, `accepted_slide`: 정상 전이를 낮은 가중치로 증가한다.
- `wrong_origin_key`: 원래 후보의 `correctedFromCount`와 대체 후보의
  `correctedToCount`를 증가한다.
- 삭제 후 대체 입력이 아직 없는 경우: 보류하며 전이에 반영하지 않는다.
- 예측 보정으로 재배정된 애매한 터치: 자기강화 방지를 위해 정상 표본에서
  제외한다.
- 키 안전 중심부에서 발생한 입력: 가장 신뢰도 높은 정상 표본으로 사용한다.
- 비밀번호, 숫자, URI, 이메일, web-edit 및 원격/raw-key surface: 첫 버전에서는
  전이 학습을 중지한다.

`ㅎ → 삭제 → 공백`은 저널의 replacement 연결을 통해 다음처럼 반영한다.

```text
이전 확정 키 -> ㅎ: correctedFrom +1
이전 확정 키 -> space: correctedTo +1
```

### 4. 경계 후보 판정

현재 `TouchResolver`의 안전 중심부 우선 규칙을 유지한다. 예측은 다음 조건을
모두 만족할 때만 실행한다.

1. 한글 딩굴 배열의 일반 TAP이다.
2. 터치가 어느 키의 안전 중심부에도 속하지 않는다.
3. 서로 인접한 두 후보가 제한된 거리 차이 안에 있다.
4. 최소 학습 표본과 확률 우위 조건을 충족한다.
5. 예측 보너스가 허용된 최대 기하학 거리보다 작다.

초기 상한값은 테스트 가능한 상수로 둔다.

```text
safeCoreInset       = 기존 hit slop 기반 중심 영역
ambiguousBand       = 최대 8dp
minimumSamples      = 8
minimumCorrections  = 3
minimumOddsRatio    = 2.5
maximumPriorShift   = 6dp 상당 점수
```

최종 후보 점수 예시는 다음과 같다.

```text
finalScore = geometryScore
           + boundedTransitionPrior
           + boundedExplicitCorrectionBonus
```

예측값은 터치다운 시 한 번 계산하고 해당 포인터 세션 동안 고정한다. 이동 중
키 영역이 흔들리거나 프리뷰가 순간적으로 바뀌지 않게 한다.

## 단계별 구현

### 단계 0: 기준선과 관측 가능성

- `ㅎ`/공백 경계의 기하학 후보, shadow 후보, 최종 후보를 재현하는 단위 테스트를
  추가한다.
- 디버그 오버레이에 문맥 ID, 기하학 후보, 예측 후보, 적용 여부와 제한 사유를
  표시한다.
- 기존 실기기 로그에서 `wrong_origin_key(ㅎ, space)` 발생 여부를 확인한다.

완료 조건:

- 예측을 적용하지 않은 상태에서 현재 판정 결과를 고정 테스트로 재현한다.
- 개인 입력 원문 없이 오판과 대체 입력을 추적할 수 있다.

### 단계 1: 전이 모델만 구현

- `NextKeyTouchModel`과 JSON codec을 추가한다.
- `TypingEventJournal`의 확정/교정 라벨을 모델 갱신 이벤트로 변환한다.
- 최대 context 수, 후보 수, count 감쇠와 learning epoch 일치를 구현한다.
- `TouchBiasStore.flushNow()`와 기존 초기화 경로에 모델을 연결한다.
- 이 단계에서는 실제 터치 판정을 변경하지 않는다.

완료 조건:

- accepted 입력은 정상 전이를 증가시킨다.
- 삭제된 입력은 정상 전이를 증가시키지 않는다.
- `ㅎ → 삭제 → 공백`은 공백의 corrected-to 통계만 올바르게 증가시킨다.
- serialize/deserialize와 손상된 JSON 복구가 결정적이다.

### 단계 2: shadow-only 평가

- 실제 출력은 기존 기하학 후보를 유지한다.
- 새 모델이 선택했을 후보를 shadow 결과로 저널에 기록한다.
- 계속 입력하면 false alarm, 삭제 후 해당 후보로 대체하면 missed opportunity로
  분류한다.
- 최소 표본, odds ratio, 거리 상한을 실기기 데이터로 조정한다.

완료 조건:

- shadow 후보가 실제 입력을 바꾸지 않는다.
- 중앙 터치에는 shadow 재배정이 발생하지 않는다.
- ㅎ/공백 경계에서 precision과 false-alarm 비율을 산출할 수 있다.

### 단계 3: 제한적 TAP 보정

- shadow 평가에서 기준을 충족한 경우에만 경계 후보 재배정을 활성화한다.
- 첫 활성 범위는 `ㅎ`과 공백의 TAP 경계로 제한한다.
- 적용된 입력에는 `predictiveApplied=true`를 남기고 정상 학습 표본에서는
  제외한다.
- 즉시 삭제 시 예측 모델에 명시적 부정 피드백을 기록한다.

활성화 기준 제안:

```text
explicit corrections >= 3
total confirmed samples >= 8
candidate odds ratio >= 2.5
shadow precision >= 0.85
geometry displacement <= 6dp
```

완료 조건:

- ㅎ 중앙 TAP과 ㅎ 방향 슬라이드는 100% 기존 결과를 유지한다.
- 공백 중앙 TAP과 공백 방향 모음은 100% 기존 결과를 유지한다.
- 학습 이력이 없는 사용자에게는 기존 판정과 동일하다.
- 예측 적용 후 즉시 삭제가 반복되면 해당 보정은 자동으로 약해진다.

### 단계 4: 일반 인접 키 확장

- ㅎ/공백 결과가 안정된 뒤에만 다른 인접 키 쌍으로 확대한다.
- 키 쌍별 허용 여부를 semantic policy로 관리한다.
- 숫자줄, 기능열, delete, enter, 언어 전환, Remote 키는 자동 확대 대상에서
  제외한다.
- 2-key 문맥이나 거친 오토마타 상태는 1-key 모델의 데이터가 충분할 때만
  실험 플래그로 추가한다.

## 저장과 개인정보 보호

- 저장 위치는 기존 `keyboard_preferences`의 앱 전용 저장소를 사용한다.
- 네트워크 전송을 추가하지 않는다.
- 전이 집계에는 원문, 완성 단어, 앱의 입력 내용과 클립보드를 저장하지 않는다.
- context와 candidate는 semantic key ID와 동작만 포함한다.
- 모델 크기를 제한하고 오래된 count는 반감해 무한 누적을 막는다.
- `LocalDataControlsController.clearTouchBiasOnly()`와 전체 초기화가 새 모델도
  제거하도록 한다.
- 설정의 로컬 데이터 요약, 개인정보 처리 문서와 Play Data safety 초안을 함께
  갱신한다.

## 테스트 전략

### 순수 단위 테스트

- 전이 count 증가, 취소, 대체 입력과 감쇠
- 배열/모드별 namespace 분리
- 최소 표본과 odds ratio 경계값
- 최대 6dp 상당 보너스 상한
- 손상 JSON, 이전 schemaVersion과 learning epoch 불일치
- 예측 적용 표본의 자기강화 제외

### 터치 판정 테스트

- ㅎ 중앙, 공백 중앙, 두 키 경계의 좌표 표본
- 같은 좌표에서 학습 전/후 결과 비교
- key gap, hit slop, handedness와 화면 density 조합
- TAP만 보정되고 방향 슬라이드·길게 누르기는 보존되는지 확인
- 한 손가락 연속 입력의 origin/candidate 상태 머신이 영향을 받지 않는지 확인

### 통합 및 실기기 검증

- 3분 이상 일반 딩굴 입력 후 경계 오입력과 즉시 삭제 횟수 비교
- 개인 학습 초기화 후 기준선 복원
- Chrome, WebView, Messages/Notes에서 composing/delete 회귀 확인
- 디버그 shadow 모드와 활성 모드의 false-alarm 비교
- 키보드 프레임 시간과 입력 지연을 측정하고 JSON parsing이 터치 hot path에
  들어가지 않는지 확인

## 실패 시 안전장치

- 예측 모델이 없거나 손상되면 기존 `TouchResolver` 결과를 그대로 사용한다.
- 예측 계산에서 예외가 발생해도 입력 처리는 중단하지 않는다.
- 후보 재배정은 runtime 플래그 하나로 즉시 비활성화할 수 있게 한다.
- 모델 초기화는 앱 재설치 없이 설정에서 가능해야 한다.
- 중앙 안전 영역, 최대 거리와 후보 allowlist는 사용자 데이터가 변경할 수 없는
  하드 제한으로 둔다.

## 예상 변경 지점

- 새 파일: `NextKeyTouchModel`, `NextKeyTouchPolicy`, 집중 단위 테스트
- `TypingEventJournal`: 학습 소비용 확정 이벤트/교정 이벤트 표현
- `TouchBiasStore`: 모델 load/save/flush/reset
- `HangulKeyboardView`: 문맥 캡처, shadow 평가, 제한적 후보 재배정
- `TouchResolver` 또는 별도 resolver: 안전 중심부 이후의 후보 점수 API
- `LocalDataControlsController`: 새 데이터 존재 여부와 초기화
- `InputIssueReport`: 집계 수치만 포함하고 key sequence는 내보내지 않음
- `docs/privacy-notice.md`, `docs/play-data-safety-draft.md`, `docs/feature-audit.md`

## 권장 첫 구현 범위

첫 PR은 단계 0과 단계 1만 포함한다. 전이 모델을 학습하고 테스트하되 실제 키
판정은 변경하지 않는다. 두 번째 PR에서 shadow-only 평가를 추가하고, 실제 기기
자료가 충분할 때 세 번째 PR에서 ㅎ/공백 경계의 제한적 TAP 보정을 활성화한다.

이 순서를 지키면 기존 입력 수집을 재사용하면서도 오타를 학습하거나 예측 결과가
스스로 강화되는 문제를 사전에 검증할 수 있다.
