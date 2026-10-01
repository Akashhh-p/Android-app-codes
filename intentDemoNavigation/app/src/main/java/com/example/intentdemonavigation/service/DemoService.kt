package com.example.intentdemonavigation.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.intentdemonavigation.receiver.MyReceiver
import java.util.Timer
import java.util.TimerTask

class DemoService : Service() {

    private var isStarted = false
    private var counter = 0
    private var timer: Timer? = null
    private val myReceiver = MyReceiver()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ServiceLog", "Service Created - Registering Receiver")
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(myReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isStarted) {
            Log.d("ServiceLog", "Service Started already")
        } else {
            isStarted = true
            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    counter++
                    Log.d("ServiceLog", "Counter : $counter")
                }
            }, 0, 1000)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
        isStarted = false
        
        try {
            unregisterReceiver(myReceiver)
        } catch (e: Exception) {
            Log.e("ServiceLog", "Error unregistering receiver", e)
        }
        Log.d("ServiceLog", "Service Destroyed")
    }
}