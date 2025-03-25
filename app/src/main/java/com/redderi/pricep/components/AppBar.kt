package com.redderi.pricep.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppBarIcon(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

data class AppBarDropdownItem(
    val text: String,
    val onClick: () -> Unit
)

data class AppBarTitle(
    val text: String,
    val style: TextStyle
)

@Composable
fun AppBar(
    expanded: MutableState<Boolean>,
    leftIcons: List<AppBarIcon>,
    rightIcons: List<AppBarIcon>,
    title: AppBarTitle,
    dropdownItems: List<AppBarDropdownItem>? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leftIcons.forEach { icon ->
                IconButton(
                    onClick = icon.onClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = icon.icon,
                        contentDescription = icon.contentDescription,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Text(
            text = title.text,
            style = title.style,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dropdownItems?.let { items ->
                IconButton(
                    onClick = { expanded.value = true },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Информация",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(8.dp)
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item.text,
                                    textAlign = TextAlign.Justify,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.SansSerif
                                )
                            },
                            onClick = item.onClick
                        )
                    }
                }
            }
            rightIcons.forEach { icon ->
                IconButton(
                    onClick = icon.onClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = icon.icon,
                        contentDescription = icon.contentDescription,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}