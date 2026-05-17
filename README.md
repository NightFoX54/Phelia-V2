# Phelia

Phelia is an Android marketplace app. People can browse products, buy items, and manage orders. Store owners can run a shop inside the app. Admins can manage users, stores, and platform settings.

The app is built with **Kotlin** and **Jetpack Compose**. It uses **Firebase** for login, database, file storage, and backend functions.

---

## What you can do in the app

| Role | Main features |
|------|----------------|
| **Customer** | Browse products, cart, checkout, orders, favorites, profile |
| **Store owner** | Dashboard, products, orders, store profile |
| **Admin** | Users, stores, applications, support tickets, catalog, analytics |

---

## What you need before you start

1. **Android Studio** (recent version is best — the project uses Android Gradle Plugin 9.x and compile SDK 36).
2. **JDK 17** (Android Studio usually includes this).
3. An **Android device** or **emulator** (API 24 or higher).
4. A **Firebase project** connected to this app. The repo includes `app/google-services.json`. If you use your own Firebase project, replace that file with yours from the [Firebase Console](https://console.firebase.google.com/).

---

## How to run the project

### Option A — Android Studio (recommended)

1. Clone or download this repository.
2. Open Android Studio → **File → Open** → select the project folder (`Phelia-V2`).
3. Wait for Gradle sync to finish. This can take a few minutes the first time.
4. Connect a phone with USB debugging, or start an emulator (**Device Manager**).
5. Click the green **Run** button (or press `Shift + F10` on Windows/Linux, `Control + R` on Mac).

The app should install and open on your device.

### Option B — Command line

From the project root folder:

```bash
chmod +x gradlew
./gradlew :app:installDebug
```

Then open the app **Phelia** on your device.

To build without installing:

```bash
./gradlew :app:assembleDebug
```

The APK will be here:

`app/build/outputs/apk/debug/Phelia-debug-v1.0.apk`

---

## First time in the app

1. Create an account on the **Sign up** screen, or sign in if you already have one.
2. Your role (customer, store owner, or admin) comes from Firebase. New users are usually **customers**.
3. Store owners and admins need the correct role set in Firestore / Firebase Auth (this is usually done by your team, not inside the app).

---

## Project structure (short overview)

```
app/src/main/java/com/example/myapplication/
├── navigation/          # App routes and navigation
├── ui/screens/          # Screens (home, cart, profile, admin, store, auth, …)
├── viewmodel/           # ViewModels for UI logic
├── data/repository/     # Firebase and data access
└── ui/theme/            # Colors, theme, dark mode
```

There is also a `figma ui/` folder with a separate web UI prototype. It is **not** required to run the Android app.

---

## Common problems

**Gradle sync failed**  
- Check your internet connection.  
- In Android Studio: **File → Invalidate Caches → Invalidate and Restart**.

**`gradlew: Permission denied`**  
Run: `chmod +x gradlew`

**App crashes on login or loading data**  
- Make sure `app/google-services.json` matches your Firebase project.  
- Enable **Authentication** (Email/Password) and **Cloud Firestore** in Firebase Console.

**Emulator is slow**  
- Use a device with more RAM in AVD settings, or test on a real phone.

---

## Tech stack

- Kotlin, Jetpack Compose, Material 3  
- Navigation Compose, ViewModel, Coroutines  
- Firebase Auth, Firestore, Storage, Functions  
- Coil (images)

---

## License

Check with the project owner if this repository is private or has a specific license.
