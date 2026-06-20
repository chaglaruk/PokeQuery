package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.HeroIllustrationPlaceholder
import com.example.pokequery.ui.components.NightMapBackground

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        NightMapBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "PokeQuery ✨",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            Text(
                text = "Safe search strings for Pokémon GO",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Hero Illustration
            HeroIllustrationPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            
            // Trust Indicators Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrustColumn(Icons.Default.Security, "Private", "No login")
                TrustColumn(Icons.Default.CloudOff, "Offline-first", "Anywhere")
                TrustColumn(Icons.Default.Lock, "Copy-only", "Safe")
            }
            
            // CTA
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start building  →", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun TrustColumn(icon: ImageVector, title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
        Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}
