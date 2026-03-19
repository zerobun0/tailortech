# TailorTech

TailorTech is an open-source Android app for men's body measurements, global size mapping, and fit risk guidance.

## Current app features

- Material 3 dark UI.
- Room-backed measurement engine using your full measurement schema.
- Dual-unit support with centimeter and inch rendering.
- Calculated metrics:
  - Inseam = Inner Thigh to Knee + Knee to Ankle
  - Total Arm Length = Shoulder to Elbow + Elbow to Wrist
- Dashboard with horizontal stats bar: Height, Chest, Waist, Hip, Inseam.
- Measurement Studio tab with guided categories (no silhouette view).
- Browse and Guided Session modes for measurement capture.
- Smart Import Assistant in Studio:
  - paste raw measurement text
  - AI/local parsing to detect values automatically
  - flags uncertain fields for double-check
  - shows missing required fields and completion percentage
  - one-tap import of detected values
- Progress tracking per category and overall session completion.
- Required/optional badges with expected-range validation hints.
- Quick-edit list covering all schema measurement points.
- Settings panel in the top-right app bar:
  - unit switching (cm/in)
  - measurement reminder cadence
  - Gemini API key override
- Size engine output for UK/US, EU, and China/Asia sizes.
- Country selector for "My Country" size mapping (UK, US, DE, FR, IT, CN, JP, KR, RU).
- Fit insights rules for Drop, Rise alert, and Thigh alert.
- Gemini Quick AI panel on Dashboard for clothing fit risk analysis.
- Supports product text paste, optional image URL input, and master prompt modes (Fit Check, Buy Risk, Style Match, Research).
- Includes an AI chat assistant for follow-up fit/sizing questions in-session.

## Design tokens

- Background: #0d0d0d
- Surface: #161616
- Border: #2a2a2a
- Primary Accent: #c8ff00
- Secondary Accent: #ff6b35
- Typography: Syne and DM Mono included in app resources.

## Build notes

- Gradle wrapper is included and verified with `:app:assembleDebug`.
- Build requires Android SDK path in `local.properties`.

## Gemini setup

You can configure Gemini in either of these ways:

- In-app Settings panel (top-right) with Gemini API key override.
- `~/.gradle/gradle.properties` (or project `gradle.properties` for local testing):

`GEMINI_API_KEY=your_key_here`

The advisor is available in the Dashboard tab.

## Release policy

- GitHub releases publish source plus APK only.
- Bundle zip artifacts are not used.

## First run behavior

- New installs start with empty measurement values (no prefilled personal data).

## Structure

- app/src/main/java/com/tailortech/app/data
- app/src/main/java/com/tailortech/app/domain
- app/src/main/java/com/tailortech/app/ui
- app/src/main/java/com/tailortech/app/ui/theme
