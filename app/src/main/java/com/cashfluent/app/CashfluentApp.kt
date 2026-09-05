package com.cashfluent.app

import android.app.Application
import com.cashfluent.app.di.ServiceLocator

class CashfluentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
