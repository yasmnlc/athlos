package com.example.athlos

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.athlos.workers.ClearOldDiaryWorker
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleDailyDiaryClear()
    }

    private fun scheduleDailyDiaryClear() {
        val repeatingRequest = PeriodicWorkRequestBuilder<ClearOldDiaryWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "clear_diary_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}