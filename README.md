# DJI Mavic 3E — LoS Survey Android App

Production-grade Android application for telecom Line-of-Sight (LoS) drone
surveys using **DJI Mavic 3E + RTK module**, designed to run on the
**DJI RC Pro** controller (Android-based).

Two survey types are supported, end-to-end automation: CSV/XLSX in →
drone flight → photo capture → metadata + PDF report out.

---

## Survey Types

| Type | Input | Drone Behaviour | Captures / Site |
| --- | --- | --- | --- |
| **Tower** | site coords + tower coords + heights + azimuth | hover 10 m from tower at each height, face tower, shoot | 1 per height |
| **Greenfield** | site coords + survey height | hover, rotate 30° steps, shoot | 12 per site |

Type is **auto-detected** from CSV columns.

---

## Tech Stack

- Kotlin + Jetpack Compose + Material 3 (custom dark "tactical" theme)
- DJI MSDK v5 (Aircraft + Aircraft-Provided + NetworkImp)
- Room (SQLite) + Hilt DI + Kotlinx Serialization + Coroutines/Flow
- Mapbox Maps SDK 11 + Apache POI (xlsx) + OpenCSV + iText 7 + Coil

---

## Project Structure

```
app/src/main/java/com/lossurvey/drone/
├── MainActivity.kt                Compose host
├── LOSSurveyApplication.kt        Hilt + DJI bootstrap
├── ui/
│   ├── theme/                     LOS dark theme (Color, Typography, Shape)
│   ├── navigation/                Compose Navigation
│   ├── screens/                   Home, Upload, MissionDetail, Flight, Report
│   └── components/                MissionCard, TelemetryBar, PreflightChecklist…
├── data/
│   ├── db/                        Room entities + DAOs + AppDatabase
│   ├── models/                    Mission, Site, DroneState, CaptureMetadata…
│   ├── parser/                    CSV / XLSX parser w/ auto type detection
│   └── repository/                MissionRepository (Room)
├── drone/
│   ├── DJIManager.kt              MSDK lifecycle + droneState flow
│   ├── RTKManager.kt              RTK module status
│   ├── WaypointMissionBuilder.kt  Tower offset + Greenfield panorama planners
│   ├── CameraController.kt        Lens / pitch / capture
│   ├── TelemetryMonitor.kt        Battery / GPS / RTK warnings
│   └── MissionExecutor.kt         End-to-end orchestrator (with simulator)
├── report/PdfReportGenerator.kt   iText7 PDF
├── storage/                       ProjectFolderManager + MetadataWriter
└── di/                            Hilt modules
```

---

## Output Layout (per mission)

```
Projects/{MissionName}/
├── survey_input.csv
├── metadata.json
├── flight_log.json
├── report_{MissionName}.pdf
└── images/
    ├── SITE001_H17M_AZ090.jpg
    ├── …
    └── GF001_GF_AZ030.jpg
```

---

## Setup

1. **DJI App Key** — register at <https://developer.dji.com>, then replace
   `manifestPlaceholders["DJI_APP_KEY"]` in `app/build.gradle.kts` with your
   real key.
2. **Mapbox Token** — get a free public token from <https://mapbox.com> and
   set `mapbox_access_token` in `app/src/main/res/values/strings.xml`.
3. **MSDK v5 dependencies** — uncomment the three `com.dji:dji-sdk-v5-*`
   lines in `app/build.gradle.kts` and add DJI's Maven repo if needed
   (`https://developer.dji.com/api-reference-v5/android-api/`).
4. **Fonts** — `Rajdhani` (Bold + Medium) and `IBM Plex Mono` (Regular) are
   loaded via Google Fonts downloadable provider — no `.ttf` files required.

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Simulator Mode

The app ships with `SimulatorConfig.ENABLED = true` so it runs end-to-end
**without** a physical drone or the DJI MSDK dependencies — useful for UI
iteration on a phone or emulator. Mission execution synthesizes captures
(stamped JPEG placeholders) and emits realistic telemetry.

Flip `SimulatorConfig.ENABLED = false` once the MSDK is wired in.

---

## CSV Examples

`samples/sample_tower.csv` and `samples/sample_greenfield.csv` are bundled
in the repo. Push them to the controller via `adb push` and load them from
the **NEW MISSION → PICK CSV/XLSX** flow.

---

## Validation & Safety

The pre-flight checklist blocks `START MISSION` until:

- Drone connected, battery ≥ 30 %, GPS ≥ 4/5
- RTK fixed (or operator approves GPS fallback)
- Mission has at least one site

`TelemetryMonitor` exposes warnings as a `Flow<List<TelemetryWarning>>` so
the UI can react to low-battery / weak-GPS / RTK-searching states in real
time.

---

## Build Order (matches the spec)

1. **Foundation** — Gradle, MSDK init, Room
2. **Data** — Parser + UI screens
3. **Flight** — Waypoint builders, camera, telemetry, FlightScreen
4. **Output** — Metadata + flight log + PDF + folder manager
5. **Polish** — Map preview, error recovery, preflight UI
