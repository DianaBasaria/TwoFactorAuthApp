package com.example.twofactorauthapp

import android.app.Application
import androidx.room.Room
import com.example.twofactorauthapp.data.AppDatabase
import com.example.twofactorauthapp.util.EncryptionHelper

class TwoFactorAuthApp : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        EncryptionHelper.init(applicationContext)
        database = AppDatabase.getInstance(applicationContext)
    }
}
