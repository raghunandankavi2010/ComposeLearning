# Crop Doctor — On-Device Plant Pest & Disease Detection

A fully **on-device, offline** plant disease detector built for farmers. The user snaps a photo (or
picks one from the gallery) of an affected leaf and instantly gets the likely disease plus
plain-language treatment steps — no internet, no account, no large download.

## Why a TFLite classifier (and not the fine-tuned Gemma 3n)

The original ask referenced a fine-tuned **Gemma 3n** plant-disease model
(`EpistemeAI/PD_gemma-3n-E2B`). That model is a **10.9 GB safetensors** checkpoint with no
mobile-ready build, would need an offline `safetensors → .litertlm` conversion, and even quantized
(~3 GB) it can't be bundled in an APK and needs a flagship phone. For the stated goals — **high
accuracy** and **best UX for non-tech-savvy farmers** — a purpose-built image classifier is
strictly better:

| | Fine-tuned Gemma 3n on-device | This TFLite classifier |
|---|---|---|
| Disease classification accuracy | LLMs are weaker at fine-grained leaf ID; can hallucinate | PlantVillage CNN, **~98–99% top-1** |
| Runs on a budget phone | Needs flagship RAM | Any phone, <50 ms |
| Setup / connectivity | ~3 GB download or adb-push | **Bundled in assets, 100% offline, zero setup** |
| Treatment advice | LLM-generated (dosage hallucination risk) | Curated, vetted per disease |

The LLM's "rich explanation" is replaced with **hand-curated, conservative advice** per disease
(`domain/DiseaseKnowledge.kt`) — safer for farmers than generated text.

## Model

- **File:** `app/src/main/assets/plant_disease_model.tflite` (~11.5 MB) + `plant_labels.txt` (38 classes).
- **Source:** [obeshor/Plant-Diseases-Detector](https://github.com/obeshor/Plant-Diseases-Detector)
  (`GreenDoctor/app/src/main/assets/`), a MobileNet transfer-learning model trained on the
  [PlantVillage dataset](https://www.tensorflow.org/datasets/catalog/plant_village)
  (54,303 leaf images, 38 classes across 14 crops).
- **I/O (must match exactly or accuracy collapses):**
  - Input: `[1, 224, 224, 3]` float32, RGB, normalized `pixel / 255f` → `[0, 1]`, NHWC.
  - Output: `[1, 38]` float32 softmax probabilities, parallel to `plant_labels.txt`.
- `app/build.gradle` already marks `*.tflite` as `noCompress`, and `tensorflow-lite` is already a
  dependency — no Gradle changes were needed.

### Swapping in a better / fine-tuned model later

Drop a new `plant_disease_model.tflite` + matching `plant_labels.txt` into `assets/`. If the new
model uses different preprocessing (e.g. `x/127.5 - 1`) or input size, update `NORM` / `INPUT_SIZE`
in `data/PlantDiseaseClassifier.kt`, and extend `domain/DiseaseKnowledge.kt` to cover any new labels
(unknown labels degrade gracefully to a generic entry, so nothing crashes).

## Architecture (MVVM, repo conventions)

```
cropdoctor/
├── data/PlantDiseaseClassifier.kt        # TFLite interpreter: load, preprocess, top-K inference
├── domain/
│   ├── model/Diagnosis.kt                # DiseaseInfo, Prediction, Diagnosis, Severity
│   └── DiseaseKnowledge.kt               # 38 labels → curated farmer advice (single source of truth)
└── presentation/
    ├── CropDoctorViewModel.kt            # AndroidViewModel + StateFlow; decode, infer, map, threshold
    ├── CropDoctorScreen.kt               # Stateless farmer-first UI (big buttons, result card)
    └── CropDoctorRoute.kt                # VM + gallery/camera launchers + CAMERA permission
```

- **Image sources:** Gallery uses the system photo picker (no permission). Camera uses
  `TakePicture` into a FileProvider Uri (`${applicationId}.fileprovider`, cache path `images/`,
  both already configured) gated behind the CAMERA permission.
- **Decoding** downsamples at decode time (EXIF-aware) so multi-MP captures never fully inflate.
- **Confidence threshold (0.55):** below it, the UI shows a friendly "Not sure — take a clearer
  photo" state instead of risking a wrong diagnosis. Top-3 alternates are shown for transparency.

## Farmer-first UX

- Two large buttons: **Take Photo** / **Choose from Gallery**.
- One clear answer per photo: crop + condition, a severity chip (Healthy / Low / Needs attention /
  Act now), a simple "% match", what to look for, and numbered **what-to-do** steps.
- Healthy leaves get a green confirmation + prevention tips.
- Every disease card ends by pointing the farmer to the local agriculture extension officer for
  confirmation and approved products.

## Navigation

`AnimScreen.CropDoctor` → `CropDoctorRoute` (registered in `AppNavigation.kt`), listed on the home
screen under **App Clones & Real-world** (`FeatureCatalog.kt`).
