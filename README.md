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
