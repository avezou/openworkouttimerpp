package xyz.negmawon.workouttimerpp.ui

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import xyz.negmawon.workouttimerpp.data.repo.Settings
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo
import xyz.negmawon.workouttimerpp.data.repo.TimerRepo
import xyz.negmawon.workouttimerpp.engine.Section
import xyz.negmawon.workouttimerpp.engine.TimerEngine
import xyz.negmawon.workouttimerpp.ui.theme.WorkoutTimerTheme
import xyz.negmawon.workouttimerpp.ui.edit.TimerEditScreen
import xyz.negmawon.workouttimerpp.ui.list.TimerListScreen
import xyz.negmawon.workouttimerpp.ui.run.RunScreen
import xyz.negmawon.workouttimerpp.ui.settings.SettingsScreen
import java.util.UUID

@SuppressLint("ContextCastToActivity", "StateFlowValueCalledInComposition")
@Composable
fun WorkoutTimerApp(
    onSplashExit: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val repo = remember { SettingsRepo.get(ctx) }
    val prefs = remember { repo.flow }.collectAsState(initial = Settings())
    val engine: TimerEngine = viewModel()
    val lastTimer = repo.lastTimerFlow.collectAsState(initial = null)
    val (lastId, _) = lastTimer.value ?: (null to null)

    val timers = TimerRepo.get(ctx).timers.collectAsState(initial = null).value

    LaunchedEffect(timers) {
        if (timers != null) {
            delay(300) // subtle fade delay
            onSplashExit()
        }
    }

    val section = engine.state.value.section
    LaunchedEffect(lastId, section) {
        if (lastId != null &&
            section !in listOf(Section.WARM_UP, Section.DONE)
        ) {
            val result = snackBarHostState.showSnackbar(
                message = "Resume your last workout?",
                actionLabel = "Resume",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                nav.navigate("run/$lastId")
            }
        }
    }
    Crossfade(targetState = prefs.value.darkTheme, label = "theme") { dark ->
        WorkoutTimerTheme(useDark = dark) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackBarHostState) }
            ) { innerPadding ->
                NavHost(nav, startDestination = "list", modifier = Modifier.padding(innerPadding)) {
                    composable("prefs") { SettingsScreen(onBack = { nav.popBackStack() }) }
                    composable("list") {
                        TimerListScreen(
                            onNavigate = nav::navigate,
                            engine = engine
                        )
                    }
                    composable("edit") {
                        TimerEditScreen(timerId = "new", onDone = { nav.popBackStack() })
                    }
                    composable(
                        "edit/{id}",
                        arguments = listOf(navArgument("id") { defaultValue = "new" })
                    ) { backstack ->
                        TimerEditScreen(
                            timerId = backstack.arguments?.getString("id") ?: "new",
                            onDone = { nav.popBackStack() })
                    }
                    composable(
                        "run/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) {
                        RunScreen(
                            timerId = it.arguments?.getString("id") ?: UUID.randomUUID().toString(),
                            onFinished = { nav.popBackStack() },
                            engine = engine
                        )
                    }
                }
            }
        }
    }
}
