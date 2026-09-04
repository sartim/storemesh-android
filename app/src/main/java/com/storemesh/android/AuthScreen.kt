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
fun SplashScreen() { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(104.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ShoppingBag, null, tint = Color.White, modifier = Modifier.size(58.dp)) } }; Text("StoreMesh", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp)); Text("Everything you need, delivered to you.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
fun LoginScreen(onLoggedIn: (LoginResult) -> Unit) {
    var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var loading by remember { mutableStateOf(false) }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope()
    val startOidc = rememberOidcSignIn(onLoggedIn, onError = { error = it; loading = false })
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
        Text("StoreMesh", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Welcome back", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        Text("Sign in to continue shopping", Modifier.padding(top = 8.dp, bottom = 26.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(email, { email = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(12.dp)); OutlinedTextField(password, { password = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        error?.let { Text(it, modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.error) }; Spacer(Modifier.height(20.dp))
        Button(onClick = { loading = true; scope.launch { runCatching { StoreMeshApi().login(email.trim(), password) }.onSuccess(onLoggedIn).onFailure { error = it.message ?: "Unable to sign in"; loading = false } } }, enabled = !loading && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) { Text(if (loading) "Signing in…" else "Log in") }
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) { HorizontalDivider(Modifier.weight(1f)); Text("or", Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant); HorizontalDivider(Modifier.weight(1f)) }
        OutlinedButton(onClick = { loading = true; startOidc() }, enabled = !loading, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) { Text("Continue securely with StoreMesh") }
        Text("Your data is protected with secure sign-in using OIDC and PKCE.", modifier = Modifier.padding(top = 18.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
