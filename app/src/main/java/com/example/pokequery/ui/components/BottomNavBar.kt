package com.example.pokequery.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.pokequery.theme.BottomNavBackground
import com.example.pokequery.theme.BottomNavSelected
import com.example.pokequery.theme.TextSecondary
import com.example.pokequery.theme.TealPrimary
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = BottomNavBackground, contentColor = Color.White, modifier = androidx.compose.ui.Modifier.height(80.dp)) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Builder") },
            label = { Text("Builder", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BottomNavSelected, unselectedIconColor = TextSecondary, indicatorColor = Color.Transparent, selectedTextColor = BottomNavSelected, unselectedTextColor = TextSecondary)
        )
        NavigationBarItem(
            selected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BottomNavSelected, unselectedIconColor = TextSecondary, indicatorColor = Color.Transparent, selectedTextColor = BottomNavSelected, unselectedTextColor = TextSecondary)
        )
        // Note: Adding Knowledge base to the nav bar as per mockup (Builder, Favorites, Knowledge, Settings)
        NavigationBarItem(
            selected = currentRoute == "knowledge",
            onClick = { onNavigate("knowledge") },
            icon = { Icon(Icons.Default.Info, contentDescription = "Knowledge") },
            label = { Text("Knowledge", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BottomNavSelected, unselectedIconColor = TextSecondary, indicatorColor = Color.Transparent, selectedTextColor = BottomNavSelected, unselectedTextColor = TextSecondary)
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BottomNavSelected, unselectedIconColor = TextSecondary, indicatorColor = Color.Transparent, selectedTextColor = BottomNavSelected, unselectedTextColor = TextSecondary)
        )
    }
}
