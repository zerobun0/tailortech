package com.tailortech.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tailortech.app.R

private val HeaderFamily = FontFamily(
    Font(R.font.syne_variable, weight = FontWeight.Normal),
    Font(R.font.syne_variable, weight = FontWeight.Bold),
    Font(R.font.syne_variable, weight = FontWeight.ExtraBold)
)

private val DataFamily = FontFamily(
    Font(R.font.dmmono_regular, weight = FontWeight.Normal),
    Font(R.font.dmmono_medium, weight = FontWeight.Medium)
)

val TailorTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = HeaderFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 42.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeaderFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HeaderFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = DataFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DataFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DataFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)
