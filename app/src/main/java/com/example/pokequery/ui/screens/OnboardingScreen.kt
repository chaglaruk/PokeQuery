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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PokeQuery",
            style = MaterialTheme.typography.displaySmall,
            color = TealPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Safe search strings for Pokémon GO",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark, RoundedCornerShape(16.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingItem("No login", "We never ask for your account details.")
            OnboardingItem("Offline-first", "All strings are generated instantly on your device.")
            OnboardingItem("Copy-only", "We generate text. You paste it in the game.")
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Start building", color = Color.White, fontWeight = FontWeight.Bold)
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
