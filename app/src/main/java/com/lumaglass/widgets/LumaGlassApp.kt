package com.lumaglass.widgets

import android.app.Application
import com.lumaglass.widgets.data.LumaPreferencesRepository
import com.lumaglass.widgets.monetization.AdMobManager
import com.lumaglass.widgets.monetization.BillingManager

class LumaGlassApp : Application() {

    lateinit var repository: LumaPreferencesRepository
        private set

    lateinit var adMobManager: AdMobManager
        private set

    lateinit var billingManager: BillingManager
        private set

    override fun onCreate() {
        super.onCreate()
        repository = LumaPreferencesRepository(applicationContext)
        adMobManager = AdMobManager(applicationContext)
        billingManager = BillingManager(applicationContext, repository)

        adMobManager.initialize()
        billingManager.startConnection()
    }
}
