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

The debug build uses `http://10.0.2.2:8080` by default. Start the local BFF
and port-forward it to `localhost:8080` before signing in. The app calls the
BFF's REST endpoints and loads active products from `GET /api/v1/products`.
For a physical device, change `API_BASE_URL` in `app/build.gradle.kts` to the
host machine's LAN address.

The `Continue with StoreMesh` action uses native AppAuth with Authorization
Code + PKCE and the public `storemesh-android` Keycloak client. The callback
is `com.storemesh.android://oauth/callback`; access and refresh tokens are
stored through the encrypted `SessionStore` backed by Android Keystore.

The debug Keycloak issuer is `http://10.0.2.2:8081/realms/storemesh`, so the
local forwarding script must expose Keycloak on port `8081` before testing
native sign-in. A physical device should use an HTTPS ngrok BFF origin and a
device-reachable Keycloak issuer; override the debug values locally rather
than committing tunnel URLs.

For a physical-device demo, expose only the BFF through an authenticated
ngrok tunnel:

```sh
ngrok http 8080
```

Verify `https://YOUR-NGROK-DOMAIN.ngrok-free.app/healthz` before configuring
the app. Keep the tunnel URL in local build configuration and never expose
gRPC, databases, Redis, or observability dashboards.

The native API client supports the authenticated customer's persisted cart and
customer-scoped order creation. The current native cart screen provides
quantity controls, line totals, subtotal display, and checkout with
post-success cart clearing. Catalog and cart reads use the BFF GraphQL
contract; REST remains available for compatibility. Pull requests and main
pushes run unit/build validation and a hosted Android emulator smoke test.

## Releases

Run the **Android release** workflow manually. `semantic-release` determines
the next `MAJOR.MINOR.PATCH` from Conventional Commits, stamps `versionName`
and a monotonic Android `versionCode`, creates the GitHub release/tag, and
builds the release APK as an artifact.

## Android Studio run configuration

Open the repository root, allow Gradle sync, and select the `app` run
configuration. In Android Studio, set **Gradle JDK** to the bundled JDK 17 and
set **Gradle distribution** to **Wrapper**. The committed wrapper uses Gradle
9.5.0, which is required by AGP 9.3.0. AGP 9 provides Kotlin support directly;
the project retains the Compose compiler plugin. Select the
existing `Medium_Phone` API 35 emulator and press Run.

If Android Studio shows **Module not specified**, delete the empty run
configuration and choose **Run > Edit Configurations > + > Android App**. Set
the module to `StoreMesh.app` (shown as `app` in some Android Studio versions),
select `Medium_Phone_API_35`, and apply. The repository also includes a shared
`StoreMesh` configuration under `.run/StoreMesh.xml` after Gradle sync.
## API transport

The app uses the Go BFF as its only API origin. Catalog and cart reads use the
authenticated GraphQL endpoint (`/api/v1/graphql`) for API composition; REST
remains available for feature flags, checkout, and compatibility operations.
The GraphQL client is isolated in `GraphQLClient.kt` so transport and parsing
can be tested independently from Compose screens.
