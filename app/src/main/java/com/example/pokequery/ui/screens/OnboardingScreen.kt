package com.example.pokequery.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.R
import com.example.pokequery.theme.*

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Full background image
        Image(
            painter = painterResource(id = R.drawable.bg_night_map),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Dark overlay to ensure text legibility
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("PokeQuery", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Safe search strings for Pokémon GO", color = Color.White.copy(alpha=0.9f), fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Central Hero illustration from asset
            Image(
                painter = painterResource(id = R.drawable.hero_onboarding_search_shield),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
            
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
        Box(modifier = Modifier.size(48.dp).background(CardPremium.copy(alpha=0.8f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TealPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, color = Color.White.copy(alpha=0.9f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
