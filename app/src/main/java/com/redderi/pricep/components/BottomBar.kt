package com.redderi.pricep.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomBar(
    leftIcon: BottomBarIcon,
    centerIcon: BottomBarIcon,
    rightIcon: BottomBarIcon
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem(icon = leftIcon)
        BottomBarItem(icon = centerIcon)
        BottomBarItem(icon = rightIcon)
    }
}

@Composable
fun BottomBarItem(icon: BottomBarIcon) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = icon.onClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(icon.backgroundColor)
                .size(icon.size)
        ) {
            Icon(
                imageVector = icon.icon,
                contentDescription = icon.contentDescription,
                tint = icon.tint
            )
        }
        if (icon.label != null) {
            Text(
                text = icon.label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

data class BottomBarIcon(
    val icon: ImageVector,
    val contentDescription: String,
    val backgroundColor: Color,
    val tint: Color = Color.White,
    val size: Dp = 70.dp,
    val label: String? = null,
    val onClick: () -> Unit
)