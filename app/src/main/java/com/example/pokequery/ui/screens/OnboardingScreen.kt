package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pokequery.theme.BackgroundDark
import com.example.pokequery.theme.CardDark
import com.example.pokequery.theme.TealPrimary

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Dark navy night-map background
    ) {
        // Decorative Hero Map Area
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(color = TealPrimary.copy(alpha = 0.15f), radius = 250f, center = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.1f))
            drawCircle(color = TealPrimary.copy(alpha = 0.05f), radius = 400f, center = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.3f))
            drawLine(color = TealPrimary.copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.3f), end = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.1f), strokeWidth = 10f)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "PokeQuery",
                style = MaterialTheme.typography.displayMedium,
                color = TealPrimary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Safe search strings for Pokémon GO",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OnboardingItem("No login", "We never ask for your account details.")
                OnboardingItem("Offline-first", "All strings are generated instantly on your device.")
                OnboardingItem("Copy-only", "We generate text. You paste it in the game.")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Start building", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OnboardingItem(title: String, description: String) {
    Column {
        Text(text = title, color = TealPrimary, fontWeight = FontWeight.SemiBold)
        Text(text = description, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}
