# StoreMesh Android

Native Android client foundation for StoreMesh.

## Stack

- Kotlin
- Jetpack Compose and Material 3
- Coroutines/Flow for asynchronous state
- BFF REST/JSON as the mobile-facing API boundary

## Structure

```text
app/src/main/java/com/storemesh/android/
├── MainActivity.kt
├── core/             # network, auth, and shared models
├── feature/auth/     # login and session state
├── feature/catalog/  # customer catalog and product details
├── feature/checkout/ # cart and order creation
├── feature/orders/   # customer order history
└── feature/admin/    # role-aware operations UI
```

For an Android emulator, the local BFF is typically
`http://10.0.2.2:8080/api/v1`. A physical device needs the host LAN address or
an approved tunnel.

The debug build uses `http://10.0.2.2:8080` by default. Start the local BFF
and port-forward it to `localhost:8080` before signing in. The app calls
`POST /api/v1/auth/login` and loads active products from
`GET /api/v1/products`. For a physical device, change `API_BASE_URL` in
`app/build.gradle.kts` to the host machine's LAN address.

## Releases

Run the **Android release** workflow manually with a `MAJOR.MINOR.PATCH`
version. It stamps `versionName` and a monotonic Android `versionCode`, builds
the release APK, uploads it as an artifact, and creates the matching `vX.Y.Z`
tag.

The debug build uses `http://10.0.2.2:8080` by default. Start the local BFF
and port-forward it to `localhost:8080` before signing in. The app calls
`POST /api/v1/auth/login` and loads active products from
`GET /api/v1/products`. For a physical device, replace `API_BASE_URL` in
`app/build.gradle.kts` with the host machine's LAN address.

## Android Studio run configuration

Open the repository root, allow Gradle sync, and select the `app` run
configuration. In Android Studio, set **Gradle JDK** to the bundled JDK 17 and
set **Gradle distribution** to **Wrapper**. The committed wrapper uses Gradle
8.9, which is required by AGP 8.7.3 and is compatible with Android Studio
versions that support AGP 8.7. Select the
existing `Medium_Phone` API 35 emulator and press Run.

If Android Studio shows **Module not specified**, delete the empty run
configuration and choose **Run > Edit Configurations > + > Android App**. Set
the module to `StoreMesh.app` (shown as `app` in some Android Studio versions),
select `Medium_Phone_API_35`, and apply. The repository also includes a shared
`StoreMesh` configuration under `.run/StoreMesh.xml` after Gradle sync.
