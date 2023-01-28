package com.cc.displaydata

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat


class NotificationListener : NotificationListenerService() {

    var context: Context? = null
    var time:Long = 0L

    private val TAG = "NotificationListener"
    private val WA_PACKAGE = "com.whatsapp"

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
       // super.onNotificationPosted(sbn)
    //    try {
            if (!sbn.packageName.equals(WA_PACKAGE)) return
           // Log.v(TAG, "data :${sbn.}")

            val notification: Notification = sbn.notification
            val bundle: Bundle = notification.extras

           Log.v(TAG, "data :${bundle.toString()}")
            for(i in bundle.keySet()){
                Log.v(TAG, "key :${i}    ===${bundle[i]}   ====>")
            }
            val msg = bundle.get("android.text").toString()
        var coun = 1
            var map = SavedPreference.loadMap(applicationContext)
            if(msg.contains("new messages")){
                 coun  = msg.substringBefore(" ").toInt()
                map.put(bundle.get("android.title").toString(), coun)
            }else{
                map.put(bundle.get("android.title").toString() , coun)
            }
           //   Log.v(TAG, "data msg:$msg:key:${bundle.get("android.title").toString()}:value:$coun")
            SavedPreference.saveMap(applicationContext,map as Map<String?, Int?>)


//
//            val from = bundle.getString(NotificationCompat.EXTRA_TITLE)
//            val message = bundle.getString(NotificationCompat.EXTRA_TEXT)
//
//            if (time != sbn.postTime && !message!!.contains("new messages")) {
//                time = sbn.postTime
//                var count = SavedPreference.getWhatsAppNotifCount(context!!)!!
//                count += 1
//                Log.i(TAG, "count: $count")
//
//
//                SavedPreference.setWhatsAppNotifCount(context!!, count)
//            }
//        }catch (e:Exception){
//
//            Log.v(TAG, "Error :${e.localizedMessage}")
//        }
////        time = sbn.postTime
//
//        val notification: Notification = sbn.notification
//        val bundle: Bundle = notification.extras
//
//        val from = bundle.getString(NotificationCompat.EXTRA_TITLE)
//        val message = bundle.getString(NotificationCompat.EXTRA_TEXT)
//
//
//        Log.i(TAG, "${sbn.packageName}")
//
//        Log.i(TAG, "From: $from")
//        Log.i(TAG, "Message: $message")
//        Log.i(TAG, "time: $time")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}