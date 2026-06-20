package com.example.pokequery

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey
@Serializable data class Preview(val goalId: String) : NavKey
