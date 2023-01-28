package com.cc.displaydata

import android.Manifest
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cc.displaydata.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isMissedCallAllowed = false
    private var isReadSMSAllowed = false
    private val timer = Timer()
    private val  ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (!isMyServiceRunning(FloatService::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startForegroundService(Intent(application, FloatService::class.java))
            } else {
                startService(Intent(application, FloatService::class.java))
            }
        }

        if(!isNotificationServiceEnabled()) {
            showNotificationPermissionDialog()
        }


        requstPermissions()





        val updateView = object : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                runOnUiThread {
                     if(isReadSMSAllowed && isMissedCallAllowed) {
                         if (!isNotificationServiceEnabled()) {
                             showNotificationPermissionDialog()
                         }
                     }
                    //Retrieve the values
                    updateViews()
                }
            }

        }


        timer.scheduleAtFixedRate(updateView, 0, 5000)
        updateViews()

    }
    private fun requstPermissions(){

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if(report.areAllPermissionsGranted()){
                        isMissedCallAllowed = true
                        isReadSMSAllowed = true
                    }else{
                        val response = report.grantedPermissionResponses
                        for(i in response){
                            if(i.permissionName == "android.permission.READ_CALL_LOG"){
                                isMissedCallAllowed = true
                            }else{
                                isReadSMSAllowed = true
                            }
                        }

                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    fun showNotificationPermissionDialog() {
        applicationContext.startActivity(
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    private fun isNotificationServiceEnabled(): Boolean {

        val cn = ComponentName(this, NotificationListener::class.java)
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        val enabled = flat != null && flat.contains(cn.flattenToString())

        return  enabled




//        val pkgName = packageName
//        val flat = Settings.Secure.getString(
//            contentResolver,
//            ENABLED_NOTIFICATION_LISTENERS
//        )
//        if (!TextUtils.isEmpty(flat)) {
//            val names = flat.split(":".toRegex()).toTypedArray()
//            for (name in names) {
//                val cn = ComponentName.unflattenFromString(name)
//                if (cn != null) {
//                    if (TextUtils.equals(pkgName, cn.packageName)) {
//                        return true
//                    }
//                }
//            }
//        }
//        return false
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    private fun updateViews() {
        var lockTime = SavedPreference.getDeviceLockTime(this@MainActivity)!!
        var unLockTime = SavedPreference.getDeviceUnLockTime(this@MainActivity)!!
        var unPlugTime = SavedPreference.getUnplugedTime(this@MainActivity)!!
        var whatsappCount = SavedPreference.loadMap(this@MainActivity)!!
        var count = 0

        for (i in whatsappCount.values){
            count += i
        }
        if(count == 0){
            binding.tvWhatsappCount.text = "WhatsApp notification count is :0"
        }else{
            binding.tvWhatsappCount.text = "WhatsApp notification count is :$count"
        }

        if(isMissedCallAllowed) {
            val projection = arrayOf(
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.TYPE
            )
            val where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE
            val c: Cursor? =
                contentResolver.query(CallLog.Calls.CONTENT_URI, projection, where, null, null)
            if (c != null && c.moveToFirst()) {
                binding.tvMissedCalls.text = "Missed Calls:${c.count}"
                c.deactivate()
            } else {
                binding.tvMissedCalls.text = "No Missed Call!"
            }

        }else{
            binding.tvMissedCalls.text = "Permission required to Read Missed Calls Count!"
            requstPermissions()
        }

        if(isReadSMSAllowed) {
            val SMS_INBOX: Uri = Uri.parse("content://sms/inbox")
            val co = contentResolver.query(SMS_INBOX, null, "read = 0", null, null)

            if (co != null && co.moveToFirst()) {
                binding.tvUnreadSms.text = "Unread Messages:${co.count}"
                co.deactivate()
            } else {
                binding.tvUnreadSms.text = "Unread Messages:0"
            }
        }else{
            binding.tvUnreadSms.text = "Permission required to Count UnRead Messages!"
            requstPermissions()
        }


        if(lockTime == 0L){
            binding.tvDeviceLockTime.text =
                "Device Lock Time : N/A"
        }else{
            binding.tvDeviceLockTime.text =
                "Device Lock Time :" + convertLongToTime(lockTime)
        }

        if(unLockTime == 0L){
            binding.tvDeviceUnlockTime.text =
                "Device UnLock Time : N/A"
        }else{
            binding.tvDeviceUnlockTime.text =
                "Device UnLock Time :" + convertLongToTime(unLockTime)
        }

        if(unPlugTime == 0L){
            binding.tvPhoneUnplugedTime.text =
                "Device UnPluged from Charging at : N/A"
        }else{
            binding.tvPhoneUnplugedTime.text =
                "Device UnPluged from Charging at :" + convertLongToTime(unPlugTime)
        }




        binding.tvAirplaneMode.text = if(isAirplaneModeOn(this@MainActivity)) "Airplane mode is ON" else "Airplane mode is OFF"

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter -> registerReceiver(null, ifilter)
        }
        val powerConnected: Intent? = IntentFilter(Intent.ACTION_POWER_CONNECTED).let { ifilter -> registerReceiver(null, ifilter)
        }

        val powerDisconnected: Intent? = IntentFilter(Intent.ACTION_POWER_DISCONNECTED).let { ifilter -> registerReceiver(null, ifilter)
        }


        val batteryPct: Float? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }

        binding.tvBatteryPercentage.text = "Battery Level :$batteryPct%"

        // Are we charging / charged?
        // Are we charging / charged?
        val status = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        binding.tvPhoneCharging.text = if(isCharging) "Phone is Charging..." else "Phone is not Charging!"



        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxRingtoneVolume= am.getStreamMaxVolume(AudioManager.STREAM_RING).toDouble()
        val currentRingtoneVolume = am.getStreamVolume(AudioManager.STREAM_RING).toDouble()



        when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> {
                binding.tvSilentMode.text = "Your Mobile in Silent Mode..."
                binding.tvRingToneVolume.text = "Ring tone Volume is Silent"
            }
            AudioManager.RINGER_MODE_VIBRATE -> {binding.tvSilentMode.text = "Your Mobile in Vibrate Mode..."
                binding.tvRingToneVolume.text = "Ring tone Volume is Vibrate"
            }
            AudioManager.RINGER_MODE_NORMAL -> {
                binding.tvSilentMode.text ="Your Mobile in Normal Mode..."
                binding.tvRingToneVolume.text =  "Ring tone Volume is :${((currentRingtoneVolume / maxRingtoneVolume) * 100).toInt()}%"
            }
        }


        checkConnection(this)


    }


    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a")
        return format.format(date)
    }

    /**
     * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
     */

    fun checkConnection(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connMgr != null) {
            val activeNetworkInfo: NetworkInfo? = connMgr.activeNetworkInfo
            if (activeNetworkInfo != null) { // connected to the internet
                // connected to the mobile provider's data plan
                if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    binding.tvConnectedToWifi.text = "Device Connected to wifi Network..."
                    binding.tvConnectedToCellPhone.text =
                        "Device not Connected to Cell Phone network..."

                    CheckOnlineStatus().execute()
                    return true
                } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {

                    binding.tvConnectedToWifi.text = "Device not Connected to wifi Network..."
                    binding.tvConnectedToCellPhone.text =
                        "Device Connected to Cell Phone network..."

                    CheckOnlineStatus().execute()
                    return true
                }
            }
        }


        binding.tvConnectedToWifi.text = "Device not Connected to wifi Network..."
        binding.tvConnectedToCellPhone.text = "Device not Connected to Cell Phone network..."
        binding.tvAvailabilityInternet.text = "Internet not Available!"

        return false
    }

    private fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) !== 0
    }


    inner class CheckOnlineStatus :
        AsyncTask<Void?, Int?, Boolean?>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            //This is a background thread, when it finishes executing will return the result from your function.

            return try {
                val url = URL("http://www.google.com/")
                val urlc: HttpURLConnection = url.openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "test")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 1000 // mTimeout is in seconds
                urlc.connect()
                urlc.responseCode === 200
            } catch (e: IOException) {
                Log.i("warning", "Error checking internet connection", e)
                false
            }


        }

        override fun onPostExecute(result: Boolean?) {
            if (result!!) {
                binding.tvAvailabilityInternet.text = "Internet Available"
            } else {
                binding.tvAvailabilityInternet.text = "Internet not Available"
            }

        }
    }

    override fun onDestroy() {
        timer.cancel()
        stopService(Intent(application, FloatService::class.java))
        super.onDestroy()
    }


}

