# DreamMind Mobile Fix Plan

## Goal

Turn the current design-copy app into a functional demo app without changing the visual direction. The priority is visible UX correctness, backend wiring, and removing dead-looking controls.

## Fix Order

1. **Shared backend contracts**
   - Add mobile DTOs and Retrofit calls for `GET /profile/me`, `PUT /profile/me`, `GET /onboarding/me`, and `PUT /onboarding/me`.
   - Add lightweight repositories for profile and onboarding.
   - Keep fake data only as fallback content when the backend is unavailable.

2. **Auth and onboarding**
   - Use a shared onboarding ViewModel across the auth/onboarding flow so selections survive route changes.
   - Save goal, target bedtime, target wake time, and sync choice with `PUT /onboarding/me`.
   - Route authenticated users with incomplete onboarding back to onboarding instead of Home.
   - Make Google and forgot-password controls show clear "not configured" feedback instead of silently doing nothing.
   - Add password visibility toggles.

3. **Reusable UI actions**
   - Add optional action callbacks to `ScreenHeader` and `SectionTitle`.
   - Wire header/profile/settings actions to navigation or visible feedback.

4. **Home**
   - Show loading/error state from the latest-session API.
   - Make the profile/avatar header action navigate to Profile.
   - Make routine rows and "View all" route to useful app areas instead of being dead visuals.

5. **Sleep Log**
   - Make bedtime, wake time, quality, activity chips, custom tag, notes, and date selection editable.
   - Send those edited values to `POST /sleep-sessions`.
   - Open the newly-created session detail after saving.

6. **Alarm**
   - Make alarm time, smart wake window, sound, and vibration editable.
   - Persist all editable fields to `PUT /alarm/me`.
   - Display AM/PM correctly from the actual alarm time.
   - Schedule a simple local Android alarm notification after a successful save.

7. **Analytics**
   - Make latest-session detail navigation reliable even when Home has not loaded first.
   - Keep period switching backend-backed and make chart taps intentionally open the latest available detail.
   - Wire header avatar to Profile.

8. **Session Detail**
   - Render loading and error states.
   - Use backend bedtime/wake time/activity/notes data when available.
   - Fix stage bar weights to use actual segment durations rather than a hardcoded 480-minute denominator.

9. **Coach**
   - Remove optimistic messages when a send/analyze call fails.
   - Restore failed typed messages into the composer.
   - Auto-scroll to the newest message.
   - Make suggestion chips send or analyze directly.
   - Format backend timestamps consistently.

10. **Profile**
    - Load real profile data from `GET /profile/me`.
    - Use backend/member-since data and analytics-derived stats where available.
    - Make settings/camera/header rows produce visible feedback.
    - Make bedtime reminder toggle work locally.
    - Show the real app version from `BuildConfig`.

## Verification

- Run `./gradlew testDebugUnitTest`.
- Run `./gradlew assembleDebug` if unit tests pass.
- Manually verify login with `alex@example.com / password123` against `http://192.168.254.132:3000/`.
