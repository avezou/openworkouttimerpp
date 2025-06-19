package xyz.negmawon.workouttimerpp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.crashlytics.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import xyz.negmawon.workouttimerpp.data.repo.SettingsRepo.Keys
import xyz.negmawon.workouttimerpp.ui.common.WorkoutTimerTopBar
import xyz.negmawon.workouttimerpp.R
import xyz.negmawon.workouttimerpp.utils.VibrationPermissionHandler

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val vm: SettingsViewModel  = remember { SettingsViewModel.factory(ctx) }
        .let { androidx.lifecycle.viewmodel.compose.viewModel(factory = it) }
    val prefs by vm.prefsFlow.collectAsState(initial = null)
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WorkoutTimerTopBar(
                title = "Preferences",
                onClearAll = {},
                onPreferences = onBack,
                showClearAll = false,
                showPreferences = false
            )
        }
    ) { inner ->
        prefs?.let { p ->
            Column(
                Modifier
                    .padding(inner)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),

            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        /* ──────────── TIMER ANNOUNCEMENTS ──────────── */
                        Header("Workout & Timers")
                        SwitchRow("Announce workout name", p.announceName) {
                            vm.toggle(Keys.ANNOUNCE_NAME)
                        }
                        SwitchRow("Announce rest", p.announceRest) {
                            vm.toggle(Keys.ANNOUNCE_REST)
                        }
                        SwitchRow("End‑of‑set beep", p.beepEndSet) {
                            vm.toggle(Keys.BEEP_END_SET)
                        }
                        SwitchRow("End‑of‑workout cheer", p.cheerEndWorkout) {
                            vm.toggle(Keys.CHEER_END_WO)
                        }
                        SwitchRow("Vibrate at transitions", p.vibrateTransitions) {
                            vm.toggle(Keys.VIBRATE)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        /* ──────────────── THEME ───────────────────── */
                        Header("Appearance")
                        SwitchRow("Dark theme", p.darkTheme) {
                            vm.toggle(Keys.DARK_THEME)
                        }
                        SwitchRow("Keep Screen On", p.wakeLock) {
                            vm.toggle(Keys.WAKE_LOCK)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {                Header("Diagnostics")
                        SwitchRow("Enable crash reporting", p.crashReporting) {
                            vm.toggle(Keys.CRASH_REPORTING)
                            // Immediately update Crashlytics collection
                            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = it
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        /* ──────────────── ABOUT ───────────────────── */
                        Header("About")
                        TextButton(onClick = { showAbout = true }) {
                            Text("About WorkoutTimerPP")
                        }
                    }
                }
            }
        }
    }
    VibrationPermissionHandler(enabled = prefs?.vibrateTransitions == true)

    if (showAbout) {
        val title         = stringResource(R.string.about_title)
        val version       = stringResource(R.string.about_version)
        val description   = stringResource(R.string.about_description)
        val features      = stringArrayResource(R.array.about_features)
        val attribLabel   = stringResource(R.string.about_attribution_label)
        val attribDetail  = stringResource(R.string.about_attribution_detail)
        val contactLabel  = stringResource(R.string.about_contact_label)
        val contactDetail = stringResource(R.string.about_contact_dev)

        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(end = 8.dp) // room for scrollbar
                ) {
                    Text(
                        version,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(description)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Features",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    features.forEach { feature ->
                        Row(Modifier.padding(start = 8.dp, top = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold)
                            Text(feature, Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        attribLabel,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(attribDetail)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        contactLabel,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(contactDetail)
                }
            }
        )
    }
}

/* ---------- re‑usable composables ---------- */
@Composable private fun Header(text: String) = Text(
    text = text.uppercase(),
    style = MaterialTheme.typography.titleMedium.copy(
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp
    ),
    modifier = Modifier.padding(vertical = 6.dp)
)

@Composable private fun SwitchRow(text: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
