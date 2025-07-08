package com.example.twofactorauthapp

import android.app.Application
import androidx.room.Room
import com.example.twofactorauthapp.data.AppDatabase

class TwoFactorAuthApp : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "two_factor_auth_db"
        ).build()
    }
}
