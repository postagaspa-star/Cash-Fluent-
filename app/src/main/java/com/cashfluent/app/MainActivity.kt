package com.cashfluent.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.ui.navigation.CashfluentNavHost
import com.cashfluent.app.ui.theme.CashfluentTheme
import kotlinx.coroutines.launch

/**
 * One activity, no splash, no sign-in. The first thing anyone taps in Cashfluent is a
 * module, which is also why the demo can never stall on a loading state.
 *
 * The one other way in: another app sharing text to Cashfluent. That is how a friend's
 * league card arrives — pasted from a chat, or shared straight from it — and it is the
 * only input this app ever takes from outside, so it is treated as text and nothing more.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedText = sharedText(intent)
        if (sharedText != null && savedInstanceState == null) {
            lifecycleScope.launch { ServiceLocator.playerRepository.importCards(sharedText) }
        }

        setContent {
            CashfluentTheme {
                CashfluentNavHost(openLeagueOnStart = sharedText != null)
            }
        }
    }

    private fun sharedText(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type?.startsWith("text/") != true) return null
        // A card is one short line; anything enormous is not one, and is not read.
        return intent.getStringExtra(Intent.EXTRA_TEXT)?.take(MAX_SHARED_CHARS)
    }

    private companion object {
        const val MAX_SHARED_CHARS = 20_000
    }
}
