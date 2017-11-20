package com.yamamz.earthquakemonitor.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yamamz.earthquakemonitor.NotificationHelper
import com.yamamz.earthquakemonitor.Details_map_activity
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async


/**
 * Created by AMRI on 9/8/2017.
 */

class NotificationReceiver : BroadcastReceiver() {
    var intentToRepeat:Intent?=null

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        try {
            async (CommonPool) {
                intentToRepeat = Intent(context, Details_map_activity::class.java)
                NotificationHelper.getQuakesOnRefresh(context,intentToRepeat)
            }


        }
        catch (ignore:Exception){
        }
    }



}
