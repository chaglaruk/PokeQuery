package com.caglar.pokequery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.TealPrimary

@Composable
fun RiskBadge(riskLevel: RiskLevel) {
    val (bgColor, textColor) = when (riskLevel) {
        RiskLevel.Low -> TealPrimary.copy(alpha = 0.2f) to TealPrimary
        RiskLevel.Info -> Color.Gray.copy(alpha = 0.2f) to Color.White
        RiskLevel.Medium -> AmberWarning.copy(alpha = 0.2f) to AmberWarning
        RiskLevel.High -> CoralDanger.copy(alpha = 0.2f) to CoralDanger
    }
    
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = riskLevel.name,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScopeBadge(scopeBreadth: String) {
    Box(
        modifier = Modifier
            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = scopeBreadth,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
