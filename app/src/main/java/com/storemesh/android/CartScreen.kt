package com.storemesh.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CartScreen(lines: List<CartLine>, products: List<Product>, onChange: (String, Int) -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier) {
    val names = products.associateBy { it.id }
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Saved cart", style = MaterialTheme.typography.headlineMedium)
                    Text("Synced to your account", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onClear, enabled = lines.isNotEmpty()) { Text("Clear") }
            }
        }
        if (lines.isEmpty()) item { Text("Your cart is empty. Add a product from the shop.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(lines.size) { index ->
            val line = lines[index]; val product = names[line.productId]
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(product?.name ?: "Unavailable product", style = MaterialTheme.typography.titleMedium)
                        Text(product?.formattedPrice() ?: line.productId, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (product != null) Text("Line total: ${product.lineTotal(line.quantity)}", style = MaterialTheme.typography.labelLarge)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onChange(line.productId, -1) }) { Text("−") }
                        Text(line.quantity.toString(), modifier = Modifier.padding(top = 12.dp))
                        OutlinedButton(onClick = { onChange(line.productId, 1) }) { Text("+") }
                    }
                }
            }
        }
        if (lines.isNotEmpty()) item {
            val total = lines.sumOf { line -> names[line.productId]?.priceMinor?.times(line.quantity.toLong()) ?: 0L }
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Subtotal"); Text("USD ${"%.2f".format(total / 100.0)}", style = MaterialTheme.typography.titleMedium) }
                Text("Checkout is completed from the order flow.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            } }
        }
    }
}

private fun Product.lineTotal(quantity: Int): String = "${currency.ifBlank { "USD" }} ${"%.2f".format(priceMinor * quantity / 100.0)}"
