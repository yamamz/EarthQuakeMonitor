package com.yamamz.earthquakemonitor

/**
 * Created by Raymundo T. Melecio on 9/5/2017.
 */

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.yamamz.earthquakemonitor.service.DeviceBootNotificationReceiver
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.yamamz.earthquakemonitor.api.ApiServices
import com.yamamz.earthquakemonitor.model.EarthquakeGeoJSon
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

    var earthQuakes: ArrayList<Feature>? = null

    private var pendingIntent: PendingIntent? = null

    var metadata: Metadata? = null

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
        val receiver = ComponentName(context, DeviceBootNotificationReceiver::class.java)
        val pm = context.packageManager

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    /**
     * Disable boot receiver when user cancels/opt-out from notifications
     */
    fun disableBootReceiver(context: Context) {
        val receiver = ComponentName(context, DeviceBootNotificationReceiver::class.java)
        val pm = context.packageManager

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }


    fun getQuakesOnRefresh(context: Context, intentToRepeat: Intent?) {
        val BASE_URL = "https://earthquake.usgs.gov/"
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiServices = retrofit.create(ApiServices::class.java)


        val call = apiServices.earthQuakesLastdayAll
        call.enqueue(object : Callback<EarthquakeGeoJSon> {
            override fun onFailure(call: Call<EarthquakeGeoJSon>?, t: Throwable?) {

            }

            override fun onResponse(call: Call<EarthquakeGeoJSon>?, response: Response<EarthquakeGeoJSon>?) {
                earthQuakes = response?.body()?.features
                metadata = response?.body()?.metadata

                //query to server successfull with data
                if (earthQuakes?.isNotEmpty() == true) {
                    Log.e("YamamzNotification", "not empty")
                    setnotify(earthQuakes, context, intentToRepeat)


                }
            }


        })
    }


    fun setnotify(earthQuakes: ArrayList<Feature>?, context: Context, intentToRepeat: Intent?) {
        Realm.init(context)
        val realm = Realm.getDefaultInstance()
        try {
            realm?.executeTransactionAsync(Realm.Transaction { realmAsync ->
                val realmResult: RealmResults<Notification>? = realmAsync?.where(Notification::class.java)?.findAll()
                val i = 0
                earthQuakes?.filter { it.properties?.mag != null && it.properties?.mag ?: 0.0 >= 6 }?.forEach { e ->
                    //loop the result and find if the eathquake id is not in notification database
                    if (realmResult?.none { it.notificationID == e.id } == true) {
                        val requestCode = ("someString" + System.currentTimeMillis()).hashCode()
                        val notId: Int = (System.currentTimeMillis()).hashCode() + i + 1
                        //value to pass in activity
                        intentToRepeat?.putExtra("n", e.geometry?.coordinates?.get(0))
                        intentToRepeat?.putExtra("e", e.geometry?.coordinates?.get(1))
                        intentToRepeat?.putExtra("time", e.properties?.time)
                        intentToRepeat?.putExtra("mag", e.properties?.mag)
                        intentToRepeat?.putExtra("depth", e.geometry?.coordinates?.get(2))
                        intentToRepeat?.putExtra("location", e.properties?.place)

                        //set flag to restart/relaunch the app
                        intentToRepeat?.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        //Pending intent to handle launch of Activity in intent above
                        pendingIntent = PendingIntent.getActivity(context, requestCode, intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT)
                        //Build notification
                        val repeatedNotification = buildLocalNotification(context,
                                pendingIntent as PendingIntent, e.properties?.mag ?: 0.0, e.properties?.place ?: "").build()
                        //Send local notification
                        NotificationHelper.getNotificationManager(context).notify(notId, repeatedNotification)
                        //create notification object
                        val notification = Notification(e.id.toString())
                        //save the notification object to realm database
                        realmAsync.copyToRealmOrUpdate(notification)

                    }

                }
            }, Realm.Transaction.OnSuccess {
                Log.e("YamamzNotification", "notification successfully added")

            })

        } catch (e: Exception) {
        } finally {
            realm.close()
        }
    }

    /**
     * Buld notification for earthquakes
     */
    private fun buildLocalNotification(context: Context, pendingIntent: PendingIntent, mag: Double, place: String): NotificationCompat.Builder {

        return NotificationCompat.Builder(context, "channel_id")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.iconearth)
                .setStyle(NotificationCompat.BigTextStyle())
                .setColorized(true)
                .setContentTitle("Mag-$mag")
                .setContentInfo(place)
                .setAutoCancel(true) as NotificationCompat.Builder
    }

}