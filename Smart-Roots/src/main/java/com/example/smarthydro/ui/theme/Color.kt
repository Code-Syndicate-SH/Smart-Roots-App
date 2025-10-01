package com.example.smarthydro.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val TextWhite = Color(0xffeeeeee)
val DeepBlue = Color(0xFF121212) //background color
val DarkerButtonBlue = Color(0xffeeeeee)
val AquaBlue = Color(0xffeeeeee)
val OrangeYellow1 = Color(0xffeeeeee)
val OrangeYellow2 = Color(0xffeeeeee)
val OrangeYellow3 = Color(0xffeeeeee)
val Beige1 = Color(0xffeeeeee)
val Beige2 = Color(0xffeeeeee)
val Beige3 = Color(0xffeeeeee)
val LightGreen1 = Color(0xffeeeeee)
val LightGreen2 = Color(0xffeeeeee)
val LightGreen3 = Color(0xffeeeeee)
val BlueViolet1 = Color(0xffeeeeee)
val BlueViolet2 = Color(0xffeeeeee)
val BlueViolet3 = Color(0xffeeeeee)
val Blue1 = Color(0xffeeeeee)
val Blue2 = Color(0xffeeeeee)
val Blue3 = Color(0xffeeeeee) //wave color for clean water
val Red1 = Color(0xffeeeeee)
val Red2 = Color(0xffeeeeee)
val Red3 = Color(0xffeeeeee)
val AutoBlue = Color(0xff00AFEF)
val PrimaryColor = Color(0xFFA8CF45)
val GreenGood = Color(0xff50c878 )
val RedBad = Color(0xFFD2042D)

// -------- Sunrise Orchard (LIGHT) --------
val SO_Primary_L   = Color(0xFF2FA86B)  // orchard green
val SO_Secondary_L = Color(0xFFFF8A3D)  // sunrise orange
val SO_Tertiary_L  = Color(0xFF7A6BF2)  // violet accent
val SO_Bg_L        = Color(0xFFF4F7F4)  // soft sage white
val SO_Surf_L      = Color(0xFFFFFFFF)
val SO_SurfVar_L   = Color(0xFFE9EFEA)
val SO_OnSurf_L    = Color(0xFF18241D)

// -------- Sunrise Orchard (DARK) --------
val SO_Primary_D   = Color(0xFF5ED39A)
val SO_Secondary_D = Color(0xFFFFAA6E)
val SO_Tertiary_D  = Color(0xFFA99BFF)
val SO_Bg_D        = Color(0xFF0B140F)
val SO_Surf_D      = Color(0xFF101A14)
val SO_SurfVar_D   = Color(0xFF15221A)
val SO_OnSurf_D    = Color(0xFFE8F2EA)

// -------- Status (shared) --------
val SO_Success     = Color(0xFF47C972)
val SO_Warn        = Color(0xFFF1B33B)
val SO_Danger      = Color(0xFFE44D4D)

val GreenGradient = Brush.linearGradient(
    colors = listOf(LightGreen1, LightGreen2),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, 0f)
)

val DarkGradient = Brush.verticalGradient(
    colors = listOf(DeepBlue, DarkerButtonBlue)
)