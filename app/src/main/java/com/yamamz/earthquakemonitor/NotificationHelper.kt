package com.yamamz.earthquakemonitor

/**
 * Created by AMRI on 9/5/2017.
 */

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock

import com.yamamz.earthquakemonitor.service.AlarmReceiver
import com.yamamz.earthquakemonitor.service.DeviceBootReceiver

import java.util.Calendar

import android.content.Context.ALARM_SERVICE
import android.widget.RemoteViews

/**
 * Created by ptyagi on 4/17/17.
 */

object NotificationHelper {
    var ALARM_TYPE_RTC = 100
    private var alarmManagerRTC: AlarmManager? = null
    private var alarmIntentRTC: PendingIntent? = null

    var ALARM_TYPE_ELAPSED = 101
    private var alarmManagerElapsed: AlarmManager? = null
    private var alarmIntentElapsed: PendingIntent? = null



    fun cancelAlarmRTC() {
        if (alarmManagerRTC != null) {
            alarmManagerRTC?.cancel(alarmIntentRTC)
        }
    }

    fun cancelAlarmElapsed() {
        if (alarmManagerElapsed != null) {
            alarmManagerElapsed?.cancel(alarmIntentElapsed)
        }
    }

    fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Enable boot receiver to persist alarms set for notifications across device reboots
     */
    fun enableBootReceiver(context: Context) {
        val receiver = ComponentName(context, DeviceBootReceiver::class.java)
        val pm = context.packageManager

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    /**
     * Disable boot receiver when user cancels/opt-out from notifications
     */
    fun disableBootReceiver(context: Context) {
        val receiver = ComponentName(context, DeviceBootReceiver::class.java)
        val pm = context.packageManager

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }


}