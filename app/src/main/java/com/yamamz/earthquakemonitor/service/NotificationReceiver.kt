package com.yamamz.earthquakemonitor.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.yamamz.earthquakemonitor.MainActivity
import com.yamamz.earthquakemonitor.NotificationHelper
import com.yamamz.earthquakemonitor.api.ApiServices
import com.yamamz.earthquakemonitor.model.EarthquakeGeoJSon
import com.yamamz.earthquakemonitor.model.EarthquakeRealmModel
import com.yamamz.earthquakemonitor.model.Feature
import com.yamamz.earthquakemonitor.model.Metadata
import com.yamamz.earthquakemonitor.model.Notification
import io.realm.Realm
import io.realm.RealmResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList
import java.util.stream.Collectors
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources
import android.widget.RemoteViews
import com.yamamz.earthquakemonitor.R
import com.yamamz.earthquakemonitor.details_map_activity
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


/**
 * Created by AMRI on 9/8/2017.
 */

class NotificationReceiver : BroadcastReceiver() {
    var intentToRepeat:Intent?=null

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        try {
            async (CommonPool) {
                intentToRepeat = Intent(context, details_map_activity::class.java)
                NotificationHelper.getQuakesOnRefresh(context,intentToRepeat)
            }


        }
        catch (ignore:Exception){
        }
    }



}
