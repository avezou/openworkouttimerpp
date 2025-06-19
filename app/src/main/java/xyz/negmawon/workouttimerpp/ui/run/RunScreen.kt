package xyz.negmawon.workouttimerpp.ui.run

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.negmawon.workouttimerpp.R
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo
import xyz.negmawon.workouttimerpp.data.repo.TimerRepo
import xyz.negmawon.workouttimerpp.engine.Section
import xyz.negmawon.workouttimerpp.engine.TimerEngine
import xyz.negmawon.workouttimerpp.ui.components.CircularTimer

/**
 * Displays the live countdown, controls (pause / stop),
 * and reacts to the engine finishing by calling [onFinished].
 *
 * @param timerId      ID of the WorkoutTimer to run
 * @param onFinished   callback when the workout is complete or stopped
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunScreen(
    timerId: String,
    onFinished: () -> Unit,
    engine: TimerEngine
) {
    val ctx = LocalContext.current
    val repo = remember { TimerRepo.get(ctx) }
    val timers by repo.timers.collectAsState(initial = emptyList())
    val timer = timers.firstOrNull { it.id == timerId }
    val state by engine.state.collectAsState()
    var started by remember { mutableStateOf(false) }
    var isLeaving by remember { mutableStateOf(false) }
    val prefs by SettingsRepo.get(ctx).flow.collectAsState(null)
    val window = (ctx as Activity).window

    if (prefs?.wakeLock == true) {
        DisposableEffect(Unit) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    val labelText = remember(state.section, state.setIndex, state.repIndex) {
        when (state.section) {
            Section.WORKOUT -> {         // show current set name
                state.timer?.exerciseNames
                    ?.getOrNull(state.setIndex - 1)
                    ?.takeIf { it.isNotBlank() }
            }
            Section.REST_SET -> {        // show next set name
                state.timer?.exerciseNames
                    ?.getOrNull(state.setIndex)      // next set = currentIndex+1
                    ?.takeIf { it.isNotBlank() }
            }
            else -> null
        }
    }

    if (timer == null) {
        // still loading or invalid ID: show a spinner
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(timerId) {
        when {
            state.timer == null -> engine.start(timer)                  // nothing running
            state.timer?.id != timerId -> engine.start(timer)           // different timer running
            state.paused && state.section != Section.DONE -> engine.resume() // same timer, paused
            // same timer & already running or DONE → do nothing
        }
    }
    /* Auto‑dismiss when done */
    if (state.section != Section.DONE) started = true
    else if (started && !isLeaving) {
        isLeaving = true
        state.summary?.let {
            Text(it, Modifier.padding(24.dp))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        timer.name.ifBlank { "Workout" },
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            )
        }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(inner)
                    .imePadding()                      // pushes content above keyboard
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val section = state.section
                val sectionLabel = when (section) {
                    Section.WARM_UP -> stringResource(R.string.warm_up)
                    Section.WORKOUT -> stringResource(R.string.workout)
                    Section.REST_REP, Section.REST_SET -> stringResource(R.string.rest)
                    Section.COOL_DOWN -> stringResource(R.string.cool_down)
                    else -> ""
                }
                val sectionColor = when (section) {
                    Section.WARM_UP -> Color(0xFFFFD54F)
                    Section.WORKOUT -> Color(0xFF66BB6A)
                    Section.REST_REP, Section.REST_SET -> Color(0xFF42A5F5)
                    Section.COOL_DOWN -> Color(0xFF26C6DA)
                    else -> Color.Gray
                }
                val totalTimeForSection = when (state.section) {
                    Section.WARM_UP      -> timer.warmUp
                    Section.WORKOUT      -> timer.setDuration
                    Section.REST_REP     -> timer.restBetweenReps
                    Section.REST_SET     -> timer.restBetweenSets
                    Section.COOL_DOWN    -> timer.coolDown
                    else                 -> 1
                }

                val progress = state.remaining.toFloat() / totalTimeForSection.toFloat()

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)       // keep chip below
                        .height(32.dp)                            // fixed height, avoids push
                ) {
                    this@Column.AnimatedVisibility(
                        visible = labelText != null,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(
                            labelText ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                AssistChip(
                /* Phase label */
                    label = { Text(sectionLabel.uppercase()) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = sectionColor.copy(alpha = 0.15f),
                        labelColor = sectionColor
                    ),
                    onClick = {},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    label = stringResource(R.string.animated_progress)
                )


                CircularTimer(
                    progress = animatedProgress,
                    remaining = state.remaining,
                    color = sectionColor
                )
                Spacer(Modifier.height(8.dp))

                /* Set progress */
                if (state.section != Section.DONE) {
                    Text(
                        stringResource(
                            R.string.set_rep,
                            state.setIndex,
                            timer.sets,
                            state.repIndex,
                            timer.reps
                        ))
                }
                /* Summary */
                AnimatedVisibility(visible = section == Section.DONE) {
                    Card(
                        modifier = Modifier
                            .padding(inner)
                            .imePadding()                      // pushes content above keyboard
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.workout_complete), style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(state.summary ?: stringResource(R.string.great_job), style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(16.dp))
                            Text("🎉", fontSize = 36.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                when (state.section) {
                    Section.DONE -> {
                        // Only an “Exit” FAB after workout completes
                        FloatingActionButton(onClick = onFinished) {
                            Icon(Icons.Default.Close, contentDescription = "Exit")
                        }
                    }

                    else -> {
                        /* Controls */
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            PauseResumeButton(
                                paused = state.paused,
                                onPause = engine::pause,
                                onResume = engine::resume
                            )
                            IconButton(
                                onClick = {
                                    engine.stop()
                                    if (!isLeaving) {
                                        isLeaving = true
                                        onFinished()
                                    }
                                },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.stop_button),
                                    contentDescription = stringResource(R.string.stop),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            /* Dim overlay when paused (tap anywhere to resume) */
            if (state.paused) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { engine.resume() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.resume),
                        tint = Color.White,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }
        }
    }
}

/* --------------------------------------------------------------- *
 *  Re‑usable pause / resume button
 * --------------------------------------------------------------- */
@Composable
private fun PauseResumeButton(
    paused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    IconButton(
        onClick = { if (paused) onResume() else onPause() },
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            if (paused) Icons.Default.PlayArrow else ImageVector.vectorResource(R.drawable.pause_button),
            contentDescription = if (paused) stringResource(R.string.resume) else stringResource(R.string.pause)
        )
    }
}
