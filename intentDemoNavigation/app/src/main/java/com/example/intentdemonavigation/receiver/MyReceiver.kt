package com.example.intentdemonavigation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.intentdemonavigation.service.DemoService

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("AIRPLANE", "Broadcast Received: ${intent.action}")

        if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val state = intent.getBooleanExtra("state", false)
            Log.d("AIRPLANE", "Airplane State = $state")

            if (state) {
                Toast.makeText(context, "Airplane Mode ON", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Airplane Mode OFF", Toast.LENGTH_LONG).show()
            }
        }
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, DemoService::class.java)
            context.startService(serviceIntent)
            Log.d("AIRPLANE", "Service started on boot")
        }
    }
}