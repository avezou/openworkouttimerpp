package xyz.negmawon.workouttimerpp.utils

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

@Composable
fun VibrationPermissionHandler(enabled: Boolean) {
    if (Build.VERSION.SDK_INT < 33 || !enabled) return

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result ignored */ }

    LaunchedEffect(enabled) {
        launcher.launch(Manifest.permission.VIBRATE)
    }
}
