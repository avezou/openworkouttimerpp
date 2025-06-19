package xyz.negmawon.workouttimerpp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import xyz.negmawon.workouttimerpp.R

@Composable
fun Stepper(
    label: String,
    value: Int,
    range: IntRange,
    largeRow: Boolean = false,
    onChange: (Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange((value - 1).coerceAtLeast(range.first)) }) {
            Icon(ImageVector.vectorResource(R.drawable.minus_circle), contentDescription = stringResource(
                R.string.decrease, label
            ))
        }
        Text(if (largeRow) "$label: $value" else "$value $label", (if (largeRow) Modifier else Modifier.width(52.dp)).clickable { showPicker = true })
        IconButton(onClick = { onChange((value + 1).coerceAtMost(range.last)) }) {
            Icon(ImageVector.vectorResource(R.drawable.add_circle), contentDescription = stringResource(R.string.increase, label))
        }

        if (showPicker) {
            WheelPickerDialog(
                title = label,
                initial = value,
                range = range,
                onSelected = { onChange(it) },
                onDismiss = { showPicker = false }
            )
        }
    }
}


@Composable
fun TimeStepperRow(
    label: String,
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Column(Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Minutes Stepper
            Stepper(
                label = stringResource(R.string.min),
                value = minutes,
                range = 0..60,
                largeRow = false,
                onChange = onMinutesChange
            )

            Spacer(modifier = Modifier.width(24.dp))

            // Seconds Stepper
            Stepper(
                label = stringResource(R.string.sec),
                value = seconds,
                range = 5..59,
                largeRow = false,
                onChange = onSecondsChange
            )
        }
    }
}

