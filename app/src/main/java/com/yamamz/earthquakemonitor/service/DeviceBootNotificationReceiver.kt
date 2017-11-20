package com.yamamz.earthquakemonitor.service

/**
 * Created by AMRI on 9/4/2017.
 */

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yamamz.earthquakemonitor.NotificationHelper
import com.yamamz.earthquakemonitor.Details_map_activity
import com.yamamz.earthquakemonitor.model.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.util.ArrayList


class DeviceBootNotificationReceiver : BroadcastReceiver() {

    var intentToRepeat:Intent?=null
    var pendingNotificationIntent:PendingIntent?=null

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {

        if(intent.action == Intent.ACTION_BOOT_COMPLETED) {
          val notificationIntent= Intent(context, DeviceBootNotificationReceiver::class.java)
           pendingNotificationIntent = PendingIntent.getBroadcast(context, 3, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT)
           startNotification(context)
        }

        else{
            try {
                Log.e("YamamzBootNotification","Running")
                async(CommonPool) {
                    intentToRepeat = Intent(context, Details_map_activity::class.java)
                    NotificationHelper.getQuakesOnRefresh(context,intentToRepeat)
                }

            } catch (ignore: Exception) {

            }
        }


    }

    private fun startNotification(context:Context) {

        val manager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval:Long = 60000
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingNotificationIntent)

    }


}
