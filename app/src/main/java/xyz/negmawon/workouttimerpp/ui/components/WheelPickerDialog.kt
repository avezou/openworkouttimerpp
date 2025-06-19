package xyz.negmawon.workouttimerpp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/* ---------- wheel picker (pure Compose) ---------- */
@Composable
fun WheelPicker(
    range: IntRange,
    initial: Int,
    onSelect: (Int) -> Unit,
    visibleCount: Int = 5                // must be odd
) {
    require(visibleCount % 2 == 1) { "visibleCount must be odd" }
    val listState = rememberLazyListState(range.indexOf(initial))
    var current by remember { mutableIntStateOf(initial) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val value = range.first + listState.firstVisibleItemIndex
            current = value
            onSelect(value)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .height((visibleCount * 40).dp)
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(vertical = ((visibleCount / 2) * 40).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(range.count()) { idx ->
            val v = range.first + idx
            Text(
                v.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                textAlign = TextAlign.Center,
                style = if (v == current)
                    MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                else
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            )
        }
    }
}

/* ---------- wheel picker dialog wrapper ---------- */
@Composable
fun WheelPickerDialog(
    title: String,
    initial: Int,
    range: IntRange,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var temp by remember { mutableIntStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSelected(temp); onDismiss() }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        title = { Text(title) },
        text = {
            WheelPicker(
                range = range,
                initial = initial,
                onSelect = { temp = it }
            )
        }
    )
}
