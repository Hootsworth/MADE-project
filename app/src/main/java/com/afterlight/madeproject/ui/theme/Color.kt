package com.afterlight.madeproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

// ============================================================================
// LIGHT THEME (Clean, Pure White, Premium)
// ============================================================================
internal val LightSnow = Color(0xFFFFFFFF)      // Pure White App Background
internal val LightPaper = Color(0xFFFFFFFF)     // Elevated Surfaces & Cards
internal val LightPearl = Color(0xFFF5F5F5)     // Subtle fills, tags, dividers (Light grey)
internal val LightCoal = Color(0xFF11100F)      // Main Text & Primary Actions
internal val LightSlate = Color(0xFF333333)     // Secondary elements
internal val LightTextMuted = Color(0xFF6F6658) // Subtle text

// Accents
internal val LightCopper = Color(0xFFB06A3C)
internal val LightSand = Color(0xFFD9BB93)
internal val LightMoss = Color(0xFF5D7050)
internal val LightWine = Color(0xFF6D3E35)
internal val LightGlass = Color(0x66FFFFFF)

// ============================================================================
// DARK THEME (Deep OLED, Glowing, Immersive)
// ============================================================================
internal val DarkSnow = Color(0xFF000000)       // App Background (OLED Black)
internal val DarkPaper = Color(0xFF141414)      // Elevated Surfaces (Subtle Grey)
internal val DarkPearl = Color(0xFF1C1A18)      // Subtle fills, tags, dividers
internal val DarkCoal = Color(0xFFF7F1E8)       // Main Text & Primary Actions
internal val DarkSlate = Color(0xFFF1E7D7)      // Secondary elements
internal val DarkTextMuted = Color(0xFFC8BFAA)  // Subtle text

// Accents
internal val DarkCopper = Color(0xFFE08A57)
internal val DarkSand = Color(0xFFE4C79B)
internal val DarkMoss = Color(0xFF8EA07B)
internal val DarkWine = Color(0xFFA56960)
internal val DarkGlass = Color(0x33FFFFFF)

// Core standard text & hairlines
internal val LightHairline = Color(0x2E4A3F30)
internal val DarkHairline = Color(0x4CEDE2D0)
internal val LightTextPrimary = Color(0xFF211F1A)
internal val DarkTextPrimary = Color(0xFFF4ECDC)

// ============================================================================
// DYNAMIC EXPORTS (Use these in your UI)
// ============================================================================
val Snow: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkSnow else LightSnow

val Paper: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkPaper else LightPaper

val Pearl: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkPearl else LightPearl

val Slate: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkSlate else LightSlate

val Coal: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkCoal else LightCoal

val Copper: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkCopper else LightCopper

val Sand: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkSand else LightSand

val Moss: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkMoss else LightMoss

val Wine: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkWine else LightWine

val LightGlassColor: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkGlass else LightGlass

val LightHairlineColor: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkHairline else LightHairline

val LightTextPrimaryColor: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkTextPrimary else LightTextPrimary

val LightTextMutedColor: Color
	@Composable get() = if (LocalGatherDarkTheme.current) DarkTextMuted else LightTextMuted