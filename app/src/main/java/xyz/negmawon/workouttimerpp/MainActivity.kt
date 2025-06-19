package xyz.negmawon.workouttimerpp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import xyz.negmawon.workouttimerpp.ui.WorkoutTimerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // Keep splash screen visible until condition is false
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)

        setContent {
            WorkoutTimerApp(
                onSplashExit = { keepSplash = false }
            )
        }
    }
}
