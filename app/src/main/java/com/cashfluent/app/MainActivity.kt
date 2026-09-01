package com.cashfluent.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cashfluent.app.ui.navigation.CashfluentNavHost
import com.cashfluent.app.ui.theme.CashfluentTheme

/**
 * One activity, no splash, no sign-in. The first thing anyone taps in Cashfluent is a
 * module, which is also why the demo can never stall on a loading state.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashfluentTheme {
                CashfluentNavHost()
            }
        }
    }
}
