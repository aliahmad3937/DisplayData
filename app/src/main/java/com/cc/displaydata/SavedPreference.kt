package com.cc.displaydata

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.json.JSONException
import org.json.JSONObject


object SavedPreference {

    const val TARGETTIME = "targettime"
    const val USAGETTIME = "usagetime"
    const val DEVICELOCK = "devicelock"
    const val CHARGINGSTATUS = "chargingstatus"
    const val DEVICELOCKTIME = "devicelocktime"
    const val UNPLUGEDTIME = "unplugedtime"
    const val DEVICEUNLOCKTIME = "deviceunlocktime"
    const val WHATSAPPMSGCOUNT = "whatsappmsgcount"
    const val PKGNAME = "pkgname"


    private fun getSharedPreference(ctx: Context?): SharedPreferences? {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
    }


    private fun editor(context: Context, key: String, value: Long) {
        getSharedPreference(
            context
        )?.edit()?.putLong(key, value)?.apply()
    }


    private fun editor(context: Context, key: String, value: Int) {
        getSharedPreference(
            context
        )?.edit()?.putInt(key, value)?.apply()
    }

    private fun editor(context: Context, key: String, value: Boolean) {
        getSharedPreference(
            context
        )?.edit()?.putBoolean(key, value)?.apply()
    }


    private fun editor(context: Context, key: String, value: String) {
        getSharedPreference(
            context
        )?.edit()?.putString(key, value)?.apply()
    }


    @JvmStatic
    fun setTargetTime(context: Context, time: Long) {
        editor(
            context = context,
            key = TARGETTIME,
            value = time
        )
    }

    @JvmStatic
    public fun getTargetTime(context: Context) = getSharedPreference(
        context
    )?.getLong(TARGETTIME, 0)



    @JvmStatic
    fun setUsageTime(context: Context, time: Long) {
        editor(
            context = context,
            key = USAGETTIME,
            value = time
        )
    }

    @JvmStatic
    public fun getUsageTime(context: Context) = getSharedPreference(
        context
    )?.getLong(USAGETTIME, -1)

    @JvmStatic
    fun setDeviceLockStatus(context: Context, lock: Boolean) {
        editor(
            context = context,
            key = DEVICELOCK,
            value = lock
        )
    }

    @JvmStatic
    fun setDeviceChargingStatus(context: Context, lock: Boolean) {
        editor(
            context = context,
            key = CHARGINGSTATUS,
            value = lock
        )
    }


    @JvmStatic
    fun setDeviceLockTime(context: Context, time: Long) {
        editor(
            context = context,
            key = DEVICELOCKTIME,
            value = time
        )
    }

    @JvmStatic
    fun setUnplugedTime(context: Context, time: Long) {
        editor(
            context = context,
            key = UNPLUGEDTIME,
            value = time
        )
    }

    @JvmStatic
    public fun getUnplugedTime(context: Context) = getSharedPreference(
        context
    )?.getLong(UNPLUGEDTIME,0)

    @JvmStatic
    public fun getDeviceLockTime(context: Context) = getSharedPreference(
        context
    )?.getLong(DEVICELOCKTIME,0)


    @JvmStatic
    fun setDeviceUnLockTime(context: Context, time: Long) {
        editor(
            context = context,
            key = DEVICEUNLOCKTIME,
            value = time
        )
    }

    @JvmStatic
    public fun getDeviceUnLockTime(context: Context) = getSharedPreference(
        context
    )?.getLong(DEVICEUNLOCKTIME,0)

    @JvmStatic
    fun setWhatsAppNotifCount(context: Context, count: Int) {
        editor(
            context = context,
            key = WHATSAPPMSGCOUNT,
            value = count
        )
    }



     fun saveMap(context: Context,inputMap: Map<String?, Int?>) {
        val pSharedPref: SharedPreferences = getSharedPreference(context)!!
        if (pSharedPref != null) {
            val jsonObject = JSONObject(inputMap)
            val jsonString = jsonObject.toString()
            pSharedPref.edit()
                .remove("My_map")
                .putString("My_map", jsonString)
                .apply()
        }
    }

     fun loadMap(context: Context): MutableMap<String, Int> {
        val outputMap: MutableMap<String, Int> = HashMap()
        val pSharedPref: SharedPreferences = getSharedPreference(context)!!
        try {
            if (pSharedPref != null) {
                val jsonString = pSharedPref.getString("My_map", JSONObject().toString())
                if (jsonString != null) {
                    val jsonObject = JSONObject(jsonString)
                    val keysItr = jsonObject.keys()
                    while (keysItr.hasNext()) {
                        val key = keysItr.next()
                        val value = jsonObject.getInt(key)
                        outputMap[key] = value
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return outputMap
    }

    @JvmStatic
    public fun getWhatsAppNotifCount(context: Context) = getSharedPreference(
        context
    )?.getInt(WHATSAPPMSGCOUNT, 0)



    @JvmStatic
    public fun getDeviceLockStatus(context: Context) = getSharedPreference(
        context
    )?.getBoolean(DEVICELOCK, false)



    @JvmStatic
    public fun getDeviceChargingStatus(context: Context) = getSharedPreference(
        context
    )?.getBoolean(CHARGINGSTATUS, false)

    @JvmStatic
    fun setLauncherPackageName(context: Context, lock: String) {
        editor(
            context = context,
            key = PKGNAME,
            value = lock
        )
    }

    @JvmStatic
    public fun getLauncherPackageName(context: Context) = getSharedPreference(
        context
    )?.getString(PKGNAME, "")



    @JvmStatic
    fun clearTargetTime(context: Context) {
        getSharedPreference(
            context
        )?.edit()?.remove(TARGETTIME)?.apply()
    }

    @JvmStatic
    fun clearDeviceLockStatus(context: Context) {
        getSharedPreference(
            context
        )?.edit()?.remove(DEVICELOCK)?.apply()
    }

    @JvmStatic
    fun clearPreferences(context: Context) {
        getSharedPreference(
            context
        )?.edit()?.clear()?.apply()
    }
}