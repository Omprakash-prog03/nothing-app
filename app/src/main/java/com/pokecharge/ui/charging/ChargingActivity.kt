package com.pokecharge.ui.charging

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PokeChargeTheme

/**
 * The dedicated charging Activity. It is only ever reached two ways:
 *  1. The user taps the charging-session notification (always works).
 *  2. The notification's full-screen intent fires automatically, which the
 *     platform only honours for apps it currently trusts with that
 *     capability (see `ChargingForegroundService.canUseFullScreenIntent`
 *     and the README's "Lock screen & full-screen intent limitations"
 *     section) - this Activity never assumes it got here that way.
 *
 * `showWhenLocked` / `turnScreenOn` are declared on the <activity> in the
 * manifest (the supported, non-bypassing Android 8.1+ APIs for "allowed to
 * draw over the keyguard") and mirrored here via the equivalent runtime
 * calls for older/edge-case OEM behaviour. This does NOT dismiss the
 * keyguard or bypass any device security - if the device is secured, the
 * system still shows the lock screen underneath/in front as appropriate.
 */
class ChargingActivity : ComponentActivity() {

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }

        setContent {
            PokeChargeTheme {
                Surface(modifier = Modifier.fillMaxSize().background(PcBlack)) {
                    ChargingScreen(onReadyToClose = { finish() })
                }
            }
        }
    }
}
