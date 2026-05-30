package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Vibrant Palette Design colors
val CyberCyan = Color(0xFFD0BCFF)       // Lavender / primary aesthetic highlight
val CyberPurple = Color(0xFF381E72)     // Deep Regal Purple
val CyberAmber = Color(0xFFFFB74D)      // Soft Amber/Gold for streak flames (originally #xFFFF9E00)
val CyberGreen = Color(0xFF49E3B4)      // Vibrant Mint Green for streaks/correct answers (originally #00E676)
val CyberRed = Color(0xFFFF8A80)        // Soft Warm Red

// Dark Palette matching HTML bg-[#1A1C1E], bg-[#232429]
val MidnightDark = Color(0xFF1A1C1E)    // Main background body
val SurfaceDark = Color(0xFF232429)     // Card background
val OnSurfaceDark = Color(0xFFE2E2E6)   // Main text label color
val AccentSemiDark = Color(0x3BD0BCFF)  // Translucent lavender

// Secondary Card / Badge fields background
val SurfaceBadgeDark = Color(0xFF2D2F33) // From .bg-[#2D2F33]
val SurfaceHighlightDark = Color(0xFF313033) // From .bg-[#313033] (your active list item style)
val BorderMuted = Color(0xFF44474F)     // Border stroke color
val BorderActive = Color(0xFF49454F)    // active border color
val MutedLabel = Color(0xFFCAC4D0)      // text-[#CAC4D0]

// Legacy compatibility / Light Palette
val CyberLight = Color(0xFFF3F1F8)      // Soft lilac light background
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1D1B20)
val AccentSemiLight = Color(0x22381E72)

// Legacy compatibility
val Purple80 = CyberCyan
val PurpleGrey80 = CyberPurple
val Pink80 = CyberAmber
val Purple40 = CyberPurple
val PurpleGrey40 = CyberLight
val Pink40 = CyberRed
