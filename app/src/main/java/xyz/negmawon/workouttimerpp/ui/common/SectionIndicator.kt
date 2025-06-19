package xyz.negmawon.workouttimerpp.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.negmawon.workouttimerpp.R
import xyz.negmawon.workouttimerpp.engine.Section

/**
 * Small rounded “pill” that shows which phase of the workout
 * the timer is currently in.
 *
 * Usage inside RunScreen:
 * ```
 * SectionIndicator(section = state.section)
 * ```
 */
@Composable
fun SectionIndicator(section: Section) {
    val (label, icon, colour) = meta(section)

    Surface(
        color = colour,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.padding(end = 4.dp))
            Text(label, fontSize = 14.sp)
        }
    }
}

/* ---------- helper that maps a Section to (label, icon, colour) ---------- */
@Composable
private fun meta(s: Section): Triple<String, androidx.compose.ui.graphics.vector.ImageVector, Color> =
    when (s) {
        Section.WARM_UP   -> Triple("Warm‑up", ImageVector.vectorResource(R.drawable.warmup_hourglass), Color(0xFFFFA726))
        Section.WORKOUT   -> Triple("Workout", ImageVector.vectorResource(R.drawable.workout_exercise),  Color(0xFFE53935))
        Section.REST_REP  -> Triple("Rest (rep)", ImageVector.vectorResource(R.drawable.rest_water),   Color(0xFF42A5F5))
        Section.REST_SET  -> Triple("Rest (set)", ImageVector.vectorResource(R.drawable.rest_set),   Color(0xFF1E88E5))
        Section.COOL_DOWN -> Triple("Cool‑down", ImageVector.vectorResource(R.drawable.cooldown_stretch), Color(0xFF66BB6A))
        Section.DONE      -> Triple("Done", ImageVector.vectorResource(R.drawable.workout_done),       Color(0xFF26A69A))
    }
