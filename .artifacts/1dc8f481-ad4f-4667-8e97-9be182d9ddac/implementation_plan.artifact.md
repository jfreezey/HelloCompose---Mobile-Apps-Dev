# Improvement Plan for `showSnackbar` in BmiScreen

The current implementation of `showSnackbar` in `BmiScreen.kt` has a bug where it attempts to call a method on the `SnackbarHostState` class rather than an instance. Additionally, the project's `MainScreen` in `MainActivity.kt` uses a `Scaffold` but doesn't have a `SnackbarHost` configured.

This plan addresses these issues by hoisting the `SnackbarHostState` to the `MainScreen` and passing it to `BmiScreen`, ensuring a robust and idiomatic implementation.

## User Review Required

> [!IMPORTANT]
> The changes involve modifying `MainActivity.kt` to hoist the `SnackbarHostState`. This is the recommended practice for apps with multiple screens sharing a single `Scaffold`.

## Proposed Changes

### [MainActivity](file:///C:/Users/Jennifer/Downloads/MATERI KULIAH/Semester 5/Mobile Apps Dev/Praktikum/ch04_starter/app/src/main/java/com/example/ch04starter/MainActivity.kt)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Jennifer/Downloads/MATERI KULIAH/Semester 5/Mobile Apps Dev/Praktikum/ch04_starter/app/src/main/java/com/example/ch04starter/MainActivity.kt)
- Instantiate `SnackbarHostState` using `remember`.
- Pass the `SnackbarHostState` to the `Scaffold`'s `snackbarHost` parameter.
- Pass the `SnackbarHostState` to `BmiScreen`.

### [BmiScreen](file:///C:/Users/Jennifer/Downloads/MATERI KULIAH/Semester 5/Mobile Apps Dev/Praktikum/ch04_starter/app/src/main/java/com/example/ch04starter/BmiScreen.kt)

#### [MODIFY] [BmiScreen.kt](file:///C:/Users/Jennifer/Downloads/MATERI KULIAH/Semester 5/Mobile Apps Dev/Praktikum/ch04_starter/app/src/main/java/com/example/ch04starter/BmiScreen.kt)
- Update the `BmiScreen` function signature to accept `snackbarHostState: SnackbarHostState`.
- Add a `snackbarTrigger` state (a counter) to ensure the snackbar shows on every click, even if the BMI value hasn't changed.
- Update `LaunchedEffect` to observe `snackbarTrigger` and use the passed `snackbarHostState` instance.
- Increment `snackbarTrigger` in the "Hitung BMI" button's `onClick`.

## Verification Plan

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate to the "BMI" tab.
- Enter weight and height.
- Tap "Hitung BMI".
- **Verify**: A snackbar appears at the bottom with the message "Hasil BMI: [Kategori]" and stays for approximately 3 seconds.
- Tap "Hitung BMI" again without changing sliders.
- **Verify**: The snackbar reappears.
- Change a slider and tap "Hitung BMI".
- **Verify**: The snackbar appears with the updated category.
