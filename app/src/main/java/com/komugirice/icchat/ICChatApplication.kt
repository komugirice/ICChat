package com.komugirice.icchat

import android.app.Application
import android.content.Context
import timber.log.Timber


class ICChatApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        ICChatApplication.applicationContext = applicationContext
        initialize()
    }

    private fun initialize() {
        initTimber()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        lateinit var applicationContext: Context
        var isDevelop = BuildConfig.FLAVOR == "develop"
    }
}