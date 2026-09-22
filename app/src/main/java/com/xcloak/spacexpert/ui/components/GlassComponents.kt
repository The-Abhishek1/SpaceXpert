package com.xcloak.spacexpert.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xcloak.spacexpert.ui.theme.CyanAccent
import com.xcloak.spacexpert.ui.theme.LocalSpaceXpertColors
import com.xcloak.spacexpert.ui.theme.NebulaPurple
import com.xcloak.spacexpert.ui.theme.SpaceNavy900

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    borderBrush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalSpaceXpertColors.current
    val borderModifier = if (borderBrush != null) {
        Modifier.border(1.dp, borderBrush, RoundedCornerShape(24.dp))
    } else {
        Modifier.border(1.dp, colors.glassBorder, RoundedCornerShape(24.dp))
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .then(borderModifier),
        color = colors.glassBackground
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

@Composable
fun AdvancedGlassCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val glowBrush = Brush.linearGradient(
        colors = listOf(CyanAccent, NebulaPurple, CyanAccent.copy(alpha = 0.3f))
    )
    GlassCard(
        modifier = modifier,
        padding = padding,
        borderBrush = glowBrush,
        content = content
    )
}

@Composable
fun SpaceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CyanAccent,
    contentColor: Color = SpaceNavy900
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
