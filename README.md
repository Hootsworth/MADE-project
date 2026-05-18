# GATHER

GATHER is an editorial-style college events platform built with Kotlin, Jetpack Compose, MVVM + Clean Architecture, Hilt, and Firebase.

## Features

- Splash + onboarding + restricted college auth flow
- College email validation via configurable domain check (Spark-compatible)
- Email verification gate before app access
- Profile setup with interests and role (Student / Event Host)
- Home feed with editorial event cards and weekly shelf
- Discover with full-screen search, vibe filters, trending tags, and department leaderboard
- Event detail with social proof ticker, share action, and sticky RSVP CTA
- Host flow with multi-step form, draft save, live preview, and editorial poster builder
- My Events with Upcoming / Past / Hosted tabs and QR check-in code visual
- Notifications screen UI placeholder (no push in Spark mode)
- Bring-a-friend referral link generation (repository contract + implementation)
- Post-event recap wall for attendees

## Tech Stack

- Kotlin + Jetpack Compose Material 3 (custom theme)
- MVVM + Clean Architecture (domain/usecase/repository)
- Hilt DI
- Firebase Auth + Firestore (Spark-compatible)
- Navigation Compose (fade-through + slide-up transitions)
- Coil image loading
- DataStore preferences

## Project Structure

- app/src/main/java/com/afterlight/madeproject/di
- app/src/main/java/com/afterlight/madeproject/domain
- app/src/main/java/com/afterlight/madeproject/data
- app/src/main/java/com/afterlight/madeproject/ui
- app/src/main/java/com/afterlight/madeproject/utils
- firebase (rules)

## Firebase Setup

1. Create a Firebase project.
2. Add an Android app with package `com.afterlight.madeproject`.
3. Download `google-services.json` and place it in `app/google-services.json`.
4. Enable Authentication: Email/Password.
5. Enable Firestore (production mode).
6. Deploy rules:
   - `firebase deploy --only firestore:rules --project <project-id> --config firebase.json`
7. Set your college email domain in `app/build.gradle.kts`:
  - `buildConfigField("String", "COLLEGE_EMAIL_DOMAIN", '"yourcollege.edu"')`

## Firestore Model

- users/{uid}
  - email, name, year, department, interests[], role, referralCode, badgesEarned[], createdAt

- events/{eventId}
  - title, description, hostUid, coverImageUrl, category, vibes[], dateTime, venue,
    capacity, rsvpCount, tags[], status (draft/live/past), createdAt

- rsvps/{eventId}/attendees/{uid}
  - uid, rsvpAt, checkInStatus, referredBy

- eventWall/{eventId}/posts/{postId}
  - uid, imageUrl, caption, createdAt, isPinned

- departments/{deptId}
  - name, monthlyScore, eventCount, attendeeCount

## Run

1. Open in Android Studio or VS Code with Android tooling.
2. Sync Gradle.
3. Connect an emulator/device.
4. Run `app`.

## Notes

- Replace placeholder Unsplash image URLs with Firestore-hosted metadata or external URLs while on Spark mode.
- Replace `QrCodeGenerator` placeholder with ZXing for scannable QR generation.
- Add FCM + WorkManager when moving beyond current Spark-only mode.
