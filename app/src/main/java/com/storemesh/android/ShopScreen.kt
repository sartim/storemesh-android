package com.storemesh.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(accessToken: String, onLogout: () -> Unit) {
    val drawer = rememberDrawerState(DrawerValue.Closed); val scope = rememberCoroutineScope(); val snackbar = remember { SnackbarHostState() }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }; var query by remember { mutableStateOf("") }; var showOrders by remember { mutableStateOf(false) }; var selected by remember { mutableStateOf<Product?>(null) }
    LaunchedEffect(Unit) { runCatching { StoreMeshApi().products(accessToken) }.onSuccess { products = it }.onFailure { snackbar.showSnackbar("Could not load products") } }
    val filtered = products.filter { query.isBlank() || it.name.contains(query, true) || it.description.contains(query, true) }
    ModalNavigationDrawer(drawerState = drawer, drawerContent = { ModalDrawerSheet { Column(Modifier.padding(24.dp)) { Text("StoreMesh", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Spacer(Modifier.height(24.dp)); listOf("Shop", "My orders").forEach { item -> Text(item, Modifier.fillMaxWidth().clickable { showOrders = item == "My orders"; scope.launch { drawer.close() } }.padding(vertical = 16.dp)) }; HorizontalDivider(Modifier.padding(vertical = 12.dp)); Text("Sign out", Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(vertical = 16.dp), color = MaterialTheme.colorScheme.error) } } }) {
        Scaffold(topBar = { TopAppBar(title = { Text(if (showOrders) "My orders" else "Shop") }, navigationIcon = { IconButton({ scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, "Menu") } }, actions = { IconButton({}) { Icon(Icons.Default.ShoppingBag, "Cart") } }) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
            if (showOrders) OrdersScreen(accessToken, Modifier.padding(padding)) else Column(Modifier.padding(padding).fillMaxSize()) { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Search products") }, leadingIcon = { Icon(Icons.Default.Search, null) }); LazyVerticalGrid(GridCells.Adaptive(160.dp), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filtered) { ProductCard(it) { selected = it } } } }
        }
    }
    selected?.let { ProductDetailsDialog(it) { selected = null } }
}

@Composable private fun OrdersScreen(token: String, modifier: Modifier = Modifier) { var orders by remember { mutableStateOf<List<Order>>(emptyList()) }; var error by remember { mutableStateOf<String?>(null) }; LaunchedEffect(token) { runCatching { StoreMeshApi().orders(token) }.onSuccess { orders = it }.onFailure { error = "Unable to load your orders." } }; Column(modifier.fillMaxSize().padding(20.dp)) { Text("My orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) else if (orders.isEmpty()) Text("No orders yet.", Modifier.padding(top = 20.dp)) else orders.forEach { Card(Modifier.fillMaxWidth().padding(top = 12.dp)) { Column(Modifier.padding(16.dp)) { Text(it.orderId, fontWeight = FontWeight.Bold); Text(it.status.removePrefix("ORDER_STATUS_").lowercase().replaceFirstChar { c -> c.uppercaseChar() }, color = Color.Gray); Text("${it.currency} ${"%.2f".format(it.totalMinor / 100.0)}") } } } } }
@Composable private fun ProductCard(product: Product, onClick: () -> Unit) { Card(onClick = onClick) { Column(Modifier.padding(14.dp)) { Box(Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFEAF1F8)), contentAlignment = Alignment.Center) { Text(product.name.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = Color(0xFF245B85)) }; Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1); Text(product.description, color = Color.Gray, maxLines = 2, style = MaterialTheme.typography.bodySmall); Text(product.formattedPrice(), color = Color(0xFF145A32), fontWeight = FontWeight.Bold) } } }
@Composable private fun ProductDetailsDialog(product: Product, onDismiss: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text(product.name) }, text = { Column { Text(product.formattedPrice(), fontWeight = FontWeight.Bold); Text(product.description, Modifier.padding(top = 8.dp)) } }, confirmButton = { Button(onClick = onDismiss) { Text("Add to cart") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }) }
