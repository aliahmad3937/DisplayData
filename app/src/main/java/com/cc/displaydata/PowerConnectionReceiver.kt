package com.cc.displaydata

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity

class PowerConnectionReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("TAG66", "Broadcast received :" + intent?.action)
         if(intent?.action == "android.intent.action.ACTION_POWER_CONNECTED"){
             Log.e("TAG66", "Broadcast received :charging" )
         }else if(intent?.action == "android.intent.action.ACTION_POWER_DISCONNECTED"){
             Log.e("TAG66", "Broadcast received :un charging" )
         }
    }

}