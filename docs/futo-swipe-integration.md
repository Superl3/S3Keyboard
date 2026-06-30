# FUTO Swipe Integration

The app has a clean fallback swipe decoder and an optional FUTO-backed decoder
behind the same `SwipeDecoder` interface.

## Runtime Shape

- Default builds do not require FUTO.
- If `app/libs/futo-swipe.aar` exists, Gradle links it locally.
- At runtime, `FutoSwipeDecoder` is loaded by reflection.
- If the AAR, native library, or model files are missing, the app falls back to
  `HeuristicEnglishSwipeDecoder`.
- QWERTY swipe traces are normalized against the English alpha-key area before
  they are passed to the model.

## License Boundary

The inspected FUTO swipe library is GPL v3. Do not copy its source or generated
AAR into the repository unless the whole distribution decision is made with that
license in mind. The repo keeps only the optional integration wrapper and local
setup script.

## Local Setup

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\setup-futo-swipe.ps1
```

This clones or updates `external/futo-swipe-library`. To build the local AAR:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\setup-futo-swipe.ps1 -BuildAar
```

FUTO's Android build requires CMake, a POSIX `make`, Android NDK r27 or newer,
and its ExecuTorch submodule.

To also clone the public model repository:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\setup-futo-swipe.ps1 -DownloadModels
```

## Model Placement

Place model files under the app private files directory:

```text
files/futo-swipe/
  honorable_sturgeon/
    model_fp32.pte
    metadata.json
  magic_macaw/
    model_fp32.pte
    metadata.json
  hungry_jellyfish/
    context_lm.pte
    metadata.json
    vocab.txt
```

The wrapper also accepts the README-style names:

```text
files/futo-swipe/
  encoder_model/model_fp32.pte
  english_decoder_model/model_fp32.pte
  english_contextlm/model_fp32.pte
```

Each model directory must include `metadata.json`. The context LM directory must
also include `vocab.txt`.
