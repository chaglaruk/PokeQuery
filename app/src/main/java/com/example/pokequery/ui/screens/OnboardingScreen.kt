package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
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
import com.example.pokequery.ui.components.OnboardingHero

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("PokeQuery", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Safe search strings for Pokémon GO", color = TextSecondary, fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Hero illustration (only used here)
            OnboardingHero(modifier = Modifier.fillMaxWidth().height(250.dp))
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 3 Trust indicators
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TrustIcon(Icons.Default.Lock, "No login")
                TrustIcon(Icons.Default.CheckCircle, "Offline-first")
                TrustIcon(Icons.Default.Share, "Copy-only")
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start building", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TrustIcon(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(48.dp).background(CardPremium, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TealPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
