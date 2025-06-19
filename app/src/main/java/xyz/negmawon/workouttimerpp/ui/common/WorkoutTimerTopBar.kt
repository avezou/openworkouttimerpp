package xyz.negmawon.workouttimerpp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WorkoutTimerTopBar(
    title: String,
    onClearAll: () -> Unit,
    onPreferences: () -> Unit,
    clearEnabled: Boolean = true,
    showClearAll: Boolean = true,
    showPreferences: Boolean = true
) {
    val cs = MaterialTheme.colorScheme

    /* soft vertical gradient like the alarm‑clock app */
    val gradient = Brush.verticalGradient(
        0f to cs.primary,
        1f to lerp(cs.primary, cs.surfaceVariant, .85f)
    )

    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(gradient)
        ) {
            /* --------------- centre title --------------- */
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onPrimary
                )
            )

            /* --------------- right‑side actions -------- */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            ) {
                if (showClearAll) {
                    IconButton(onClick = onClearAll, enabled = clearEnabled) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear all timers",
                            tint = if (clearEnabled) cs.onPrimary else cs.onPrimary.copy(alpha = 0.3f)
                        )
                    }
                }
                if (showPreferences) {
                    IconButton(onClick = onPreferences) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Preferences",
                            tint = cs.onPrimary
                        )
                    }
                }
            }

            /* thin divider at bottom edge */
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
                    .background(cs.outline.copy(alpha = .4f))
            )
        }
    }
}
