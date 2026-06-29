package com.caglar.pokequery.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.theme.BottomNavBackground
import com.caglar.pokequery.theme.BottomNavSelected
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.CardPremium

private data class NavTab(val route: String, val labelRes: Int, val icon: ImageVector)

private val tabs = listOf(
    NavTab("builder", com.caglar.pokequery.R.string.nav_home, Icons.Default.Home),
    NavTab("favorites", com.caglar.pokequery.R.string.nav_favorites, Icons.Default.Favorite),
    NavTab("history", com.caglar.pokequery.R.string.nav_history, Icons.Default.History),
    NavTab("knowledge", com.caglar.pokequery.R.string.nav_knowledge, Icons.Default.Info),
    NavTab("settings", com.caglar.pokequery.R.string.nav_settings, Icons.Default.Settings)
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = BottomNavBackground, contentColor = Color.White, modifier = Modifier.height(80.dp)) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onNavigate(tab.route) },
                icon = { Icon(tab.icon, contentDescription = androidx.compose.ui.res.stringResource(tab.labelRes), modifier = Modifier.size(26.dp)) },
                label = { Text(androidx.compose.ui.res.stringResource(tab.labelRes), fontSize = 11.sp, fontWeight = if (currentRoute == tab.route) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealPrimary,
                    unselectedIconColor = TextSecondary,
                    indicatorColor = TealPrimary.copy(alpha = 0.15f),
                    selectedTextColor = TealPrimary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
