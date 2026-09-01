package com.storemesh.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StoreMeshApp(applicationContext) }
    }
}

@Composable
private fun StoreMeshApp(context: Context) {
    var stage by remember { mutableStateOf(AppStage.Splash) }
    var token by remember { mutableStateOf(SessionStore(context).accessToken) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(900)
        stage = if (token.isNullOrBlank()) AppStage.Login else AppStage.Shop
    }
    MaterialTheme { Surface(Modifier.fillMaxSize()) {
        when (stage) {
            AppStage.Splash -> SplashScreen()
            AppStage.Login -> LoginScreen { accessToken ->
                SessionStore(context).accessToken = accessToken
                token = accessToken
                stage = AppStage.Shop
            }
            AppStage.Shop -> ShopScreen(token.orEmpty()) {
                SessionStore(context).clear()
                token = null
                stage = AppStage.Login
            }
        }
    } }
}

private enum class AppStage { Splash, Login, Shop }

@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Color(0xFF112A46)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ShoppingBag, null, Modifier.size(72.dp), tint = Color(0xFFFFC857))
            Spacer(Modifier.height(18.dp))
            Text("StoreMesh", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Everything you need, together.", color = Color(0xFFD4E4F7))
        }
    }
}

@Composable
private fun LoginScreen(onLoggedIn: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
        Text("Welcome back", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Sign in to discover products and manage your orders.", modifier = Modifier.padding(top = 8.dp, bottom = 26.dp))
        OutlinedTextField(email, { email = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { password = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            loading = true
            scope.launch {
                runCatching { StoreMeshApi().login(email.trim(), password) }
                    .onSuccess { onLoggedIn(it.accessToken) }
                    .onFailure { error = it.message ?: "Unable to sign in"; loading = false }
            }
        }, enabled = !loading && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
            Text(if (loading) "Signing in…" else "Sign in")
        }
        Text("Development API: ${BuildConfig.API_BASE_URL}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 18.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopScreen(accessToken: String, onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Featured", "Deals")
    LaunchedEffect(Unit) {
        runCatching { StoreMeshApi().products(accessToken) }
            .onSuccess { products = it }
            .onFailure { snackbar.showSnackbar("Could not load products: ${it.message}") }
    }
    val filtered = products.filter { product ->
        (query.isBlank() || product.name.contains(query, true) || product.description.contains(query, true)) &&
            (selectedCategory == "All" || (selectedCategory == "Deals" && product.priceMinor < 5000) || (selectedCategory == "Featured" && product.name.hashCode() % 2 == 0))
    }
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet {
            Column(Modifier.padding(24.dp)) {
                Text("StoreMesh", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Customer shop", color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                listOf("Shop", "My orders", "Saved items").forEach { item ->
                    Text(item, Modifier.fillMaxWidth().clickable { scope.launch { drawerState.close() } }.padding(vertical = 16.dp), fontWeight = if (item == "Shop") FontWeight.Bold else FontWeight.Normal)
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Text("Sign out", Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(vertical = 16.dp), color = MaterialTheme.colorScheme.error)
            }
        }
    }) {
        Scaffold(topBar = { TopAppBar(title = { Text("Shop") }, navigationIcon = { IconButton({ scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") } }, actions = { IconButton({}) { Icon(Icons.Default.ShoppingBag, "Cart") } }) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Find your next favourite", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Curated picks, everyday deals.", color = Color.Gray)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search products") }, leadingIcon = { Icon(Icons.Default.Search, null) })
                    Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { categories.forEach { category -> FilterChip(selectedCategory == category, { selectedCategory = category }, label = { Text(category) }) } }
                }
                if (filtered.isEmpty()) Text("No products found. Check that the BFF is running on localhost:8080.", Modifier.padding(24.dp))
                else LazyVerticalGrid(GridCells.Adaptive(160.dp), Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filtered) { ProductCard(it) } }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product) {
    Card {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFEAF1F8)), contentAlignment = Alignment.Center) { Text(product.name.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = Color(0xFF245B85), fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(product.description, color = Color.Gray, maxLines = 2, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Text(product.formattedPrice(), color = Color(0xFF145A32), fontWeight = FontWeight.Bold)
        }
    }
}

data class Product(val id: String, val name: String, val description: String, val priceMinor: Long, val currency: String) {
    fun formattedPrice() = "${currency.ifBlank { "USD" }} ${"%.2f".format(priceMinor / 100.0)}"
}

data class LoginResult(val accessToken: String, val refreshToken: String)

private class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("storemesh_session", Context.MODE_PRIVATE)
    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) { prefs.edit().putString("access_token", value).apply() }
    fun clear() { prefs.edit().clear().apply() }
}
