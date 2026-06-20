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
import com.example.pokequery.theme.BackgroundDark
import com.example.pokequery.theme.TealPrimary

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = BackgroundDark, contentColor = Color.White) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentRoute == "builder",
            onClick = { onNavigate("builder") },
            icon = { Icon(Icons.Default.Build, contentDescription = "Builder") },
            label = { Text("Builder") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
        )
    }
}
