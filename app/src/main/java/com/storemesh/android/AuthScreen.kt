package com.storemesh.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SplashScreen() { Box(Modifier.fillMaxSize().background(Color(0xFF112A46)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFFFFC857)); Text("StoreMesh", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("Everything you need, together.", color = Color(0xFFD4E4F7)) } } }

@Composable
fun LoginScreen(onLoggedIn: (LoginResult) -> Unit) {
    var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var loading by remember { mutableStateOf(false) }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope()
    val startOidc = rememberOidcSignIn(onLoggedIn, onError = { error = it; loading = false })
    Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
        Text("Welcome back", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Sign in to discover products and manage your orders.", Modifier.padding(top = 8.dp, bottom = 26.dp))
        OutlinedTextField(email, { email = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(12.dp)); OutlinedTextField(password, { password = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        error?.let { Text(it, modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.error) }; Spacer(Modifier.height(20.dp))
        Button(onClick = { loading = true; scope.launch { runCatching { StoreMeshApi().login(email.trim(), password) }.onSuccess(onLoggedIn).onFailure { error = it.message ?: "Unable to sign in"; loading = false } } }, enabled = !loading && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) { Text(if (loading) "Signing in…" else "Sign in") }
        OutlinedButton(onClick = { loading = true; startOidc() }, enabled = !loading, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), contentPadding = PaddingValues(14.dp)) { Text("Continue with StoreMesh") }
        Text("Development API: ${BuildConfig.API_BASE_URL}", modifier = Modifier.padding(top = 18.dp), style = MaterialTheme.typography.labelSmall)
    }
}
