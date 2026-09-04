package com.storemesh.android

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun StoreMeshApp(context: Context) {
    var stage by remember { mutableStateOf(AppStage.Splash) }
    var token by remember { mutableStateOf(SessionStore(context).accessToken) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(900)
        val session = SessionStore(context)
        if (token.isNullOrBlank() && !session.refreshToken.isNullOrBlank()) {
            runCatching { StoreMeshApi().refresh(session.refreshToken.orEmpty()) }.onSuccess { session.save(it); token = it.accessToken }
        }
        stage = if (token.isNullOrBlank()) AppStage.Login else AppStage.Shop
    }
    StoreMeshTheme { Surface(Modifier.fillMaxSize()) { when (stage) {
        AppStage.Splash -> SplashScreen()
        AppStage.Login -> LoginScreen { result -> SessionStore(context).save(result); token = result.accessToken; stage = AppStage.Shop }
        AppStage.Shop -> ShopScreen(token.orEmpty()) { SessionStore(context).clear(); token = null; stage = AppStage.Login }
    } } }
}

private enum class AppStage { Splash, Login, Shop }
