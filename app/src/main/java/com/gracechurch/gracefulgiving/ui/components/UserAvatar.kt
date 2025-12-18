package com.gracechurch.gracefulgiving.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

/**
 * Reusable avatar component that handles different avatar types:
 * - Initials (initials:XX format)
 * - Icon avatars (icon:male, icon:female, icon:girl)
 * - Whimsical avatars (icon:dog, icon:cat, icon:coffee, icon:unicorn)
 * - Image URIs
 */
@Composable
fun UserAvatar(
    avatarUri: String?,
    fullName: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when {
            avatarUri?.startsWith("initials:") == true -> {
                // Display initials avatar
                val initials = avatarUri.removePrefix("initials:")
                InitialsAvatar(initials, size)
            }
            avatarUri?.startsWith("icon:") == true -> {
                // Display icon or emoji avatar
                val iconType = avatarUri.removePrefix("icon:")
                
                // Whimsical Emojis/Icons
                when (iconType) {
                    "dog" -> EmojiAvatar("🐶", size)
                    "cat" -> EmojiAvatar("🐱", size)
                    "coffee" -> EmojiAvatar("☕", size) // Replaced Spy with Coffee
                    "unicorn" -> EmojiAvatar("🦄", size) // Replaced Dunce/Clown with Unicorn
                    "male" -> IconAvatar(Icons.Default.Man, size)
                    "female" -> IconAvatar(Icons.Default.Woman, size)
                    "girl" -> IconAvatar(Icons.Default.PersonOutline, size)
                    else -> IconAvatar(Icons.Default.Person, size)
                }
            }
            avatarUri != null -> {
                // Display image from URI
                Image(
                    painter = rememberAsyncImagePainter(avatarUri),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                )
            }
            else -> {
                // Fallback
                if (fullName.isNotBlank()) {
                    InitialsAvatar(getInitials(fullName), size)
                } else {
                    IconAvatar(Icons.Default.Person, size)
                }
            }
        }
    }
}

@Composable
private fun EmojiAvatar(emoji: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = when {
                size >= 80.dp -> MaterialTheme.typography.displayMedium
                size >= 40.dp -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.titleMedium
            }
        )
    }
}

@Composable
private fun InitialsAvatar(initials: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = when {
                size >= 80.dp -> MaterialTheme.typography.headlineLarge
                size >= 50.dp -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.titleMedium
            },
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun IconAvatar(icon: androidx.compose.ui.graphics.vector.ImageVector, size: Dp) {
    Icon(
        imageVector = icon,
        contentDescription = "Avatar",
        modifier = Modifier.size(size),
        tint = MaterialTheme.colorScheme.primary
    )
}

private fun getInitials(fullName: String): String {
    if (fullName.isBlank()) return "?"

    val names = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        names.isEmpty() -> "?"
        names.size == 1 -> names[0].take(2).uppercase()
        else -> "${names.first().first()}${names.last().first()}".uppercase()
    }
}
