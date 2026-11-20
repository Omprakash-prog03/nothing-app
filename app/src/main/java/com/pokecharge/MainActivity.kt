package com.pokecharge

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pokecharge.ui.navigation.PokeChargeNavHost
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PokeChargeTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way: the
            charging screen still works via its content-tap PendingIntent even
            if the user declines, it just won't be able to post the
            charging-session notification (and therefore the auto-launch
            path) - this is surfaced to the user in Settings copy, not hidden. */
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            PokeChargeTheme {
                Surface(modifier = Modifier.fillMaxSize().background(PcBlack)) {
                    PokeChargeNavHost()
                }
            }
        }
    }
}
