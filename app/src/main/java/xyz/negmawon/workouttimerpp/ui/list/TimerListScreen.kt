package xyz.negmawon.workouttimerpp.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.negmawon.workouttimerpp.R
import xyz.negmawon.workouttimerpp.data.entity.WorkoutTimer
import xyz.negmawon.workouttimerpp.engine.Section
import xyz.negmawon.workouttimerpp.engine.TimerEngine
import xyz.negmawon.workouttimerpp.ui.common.WorkoutTimerTopBar

@Composable
fun TimerListScreen(
    onNavigate: (String) -> Unit,
    vm: TimerListViewModel = viewModel(factory = TimerListViewModel.factory(LocalContext.current)),
    engine: TimerEngine
) {
    val timers by vm.timers.collectAsState(initial = null)
    val state  by engine.state.collectAsState()
    var showClearAll by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WorkoutTimerTopBar(
                title = stringResource(R.string.workout_timers),
                onClearAll = { showClearAll = true },
                onPreferences = { onNavigate("prefs") },
                clearEnabled = timers?.isNotEmpty() == true
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigate("edit/new") }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        }
    )  { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            if (timers == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (timers?.isEmpty() == true) {
                showClearAll = false
                // Centered “no timers yet” card
                Card(
                    shape   = MaterialTheme.shapes.medium,
                    colors  = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.no_timers_yet_tap_to_add_one),
                        style    = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                timers?.let {
                    LazyColumn(
                        contentPadding = inner,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        /** 1️⃣  Real loop – one item per timer */
                        items(
                            items = it,
                            key = { it.id }            // stable key
                        ) { timer ->
                            // are we currently running *this* timer?
                            val running = state.timer?.id == timer.id &&
                                    state.section != Section.DONE
                            val isPausedSame = state.timer?.id == timer.id &&
                                    state.paused

                            // build a little status line if so
                            val statusText = if (running) {
                                val sec = state.remaining
                                val mm = sec / 60
                                val ss = sec % 60
                                "${state.section.name.replace('_', ' ')}  •  %02d:%02d".format(
                                    mm,
                                    ss
                                )
                            } else null
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(),
                            ) {
                                TimerRow(
                                    timer = timer,
                                    statusText = statusText,
                                    onPlay = {
                                        if (!isPausedSame) {
                                            engine.clear()
                                        }
                                        onNavigate("run/${timer.id}")
                                    },
                                    onEdit = { onNavigate("edit/${timer.id}") },
                                    onDelete = { vm.delete(timer.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showClearAll) {
        AlertDialog(
            title = { Text(stringResource(R.string.delete_all_timers)) },
            text  = { Text(stringResource(R.string.this_action_cannot_be_undone)) },
            onDismissRequest = { showClearAll = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteAll()        // your ViewModel method
                    showClearAll = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearAll = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}


@Composable
private fun TimerRow(
    timer: WorkoutTimer,
    statusText: String?,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            /* Play / pause icon */
            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    if (statusText != null ) Icons.Default.Menu else Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.run),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            /* Text block */
            Column(Modifier.weight(1f)) {
                Text(
                    timer.name,
                    style = MaterialTheme.typography.titleMedium
                )
                statusText?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(stringResource(
                                R.string.warm_up_work,
                                timer.sets,
                                timer.reps,
                                fmt(timer.warmUp),
                                fmt(timer.setDuration)
                            )
                )
            }

            Spacer(Modifier.width(12.dp))

            /* Edit + delete */
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

private fun fmt(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

