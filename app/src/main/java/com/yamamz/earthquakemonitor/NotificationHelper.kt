package com.yamamz.earthquakemonitor

/**
 * Created by Raymundo T. Melecio on 9/5/2017.
 */

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
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
import kotlin.collections.ArrayList

/**
 * Created by ptyagi on 4/17/17.
 */

object NotificationHelper {
    private const val CHANNEL_ID_EARTH = "Earthquake_app_id"
    private const val CHANNEL_NAME_EARTH = "Earthquake_app"
    private const val EARTH_ID = 0
    var earthQuakes: ArrayList<Feature>? = null
    private var pendingIntent: PendingIntent? = null
    var metadata: Metadata? = null

    fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                    setNotify(earthQuakes, context, intentToRepeat)


                        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                        val lat= sharedPrefs.getString("lat", "0.0")
                    val lon= sharedPrefs.getString("lon", "0.0")
                        val distancePrefs=sharedPrefs.getString("distance", "200")
                    if(lat!="0.0" && lon !="0.0") {

                        // Log.e("Yamamz","test")
                        setNotifyNearMe(earthQuakes, context, intentToRepeat, lat.toDouble(), lon.toDouble(),distancePrefs)

                    }
                }
            }


        })
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist *= 60.0 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }


    fun setNotifyNearMe(earthQuakes: ArrayList<Feature>?, context: Context, intentToRepeat: Intent?, mylat: Double?, myLon: Double?, distancePrefs: String) {
        Realm.init(context)
        val realm = Realm.getDefaultInstance()
        try {
            realm?.executeTransactionAsync(Realm.Transaction { realmAsync ->

                val realmResult: RealmResults<Notification>? = realmAsync?.where(Notification::class.java)?.findAll()
                val i = 0
                earthQuakes?.filter { it.properties?.mag != null && it.properties?.mag ?: 0.0 >= 2 }?.forEach { e ->

                    val distance:Double= distance(e.geometry?.coordinates?.get(1)?:0.0,e.geometry?.coordinates?.get(0)?:0.0,mylat?:0.0,myLon?:0.0)


                        if (realmResult?.none { it.notificationID == e.id } == true && distance <= distancePrefs.toDouble()) {

                                Log.e("Yamamz", distance.toString())
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
                                val repeatedNotification = getBasicNotificationBuilder(context,
                                        pendingIntent!!, e.properties?.mag ?: 0.0, e.properties?.place ?: "", CHANNEL_ID_EARTH,true).build()
                                //Send local notification


                            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            nManager.createNotificationChannel(CHANNEL_ID_EARTH, CHANNEL_NAME_EARTH, true)

                            nManager.notify(notId, repeatedNotification)

                                //NotificationHelper.getNotificationManager(context).notify(notId, repeatedNotification)
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

    fun setNotify(earthQuakes: ArrayList<Feature>?, context: Context, intentToRepeat: Intent?) {
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
                        val repeatedNotification = getBasicNotificationBuilder(context,
                                pendingIntent!!, e.properties?.mag ?: 0.0, e.properties?.place ?: "", CHANNEL_ID_EARTH,true).build()
                        //Send local notification
                        val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nManager.createNotificationChannel(CHANNEL_ID_EARTH, CHANNEL_NAME_EARTH, true)
                        nManager.notify(notId, repeatedNotification)




                        //NotificationHelper.getNotificationManager(context).notify(notId, repeatedNotification)
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
//    private fun buildLocalNotification(context: Context, pendingIntent: PendingIntent, mag: Double, place: String): NotificationCompat.Builder {
//        return NotificationCompat.Builder(context, "channel_id")
//                .setContentIntent(pendingIntent)
//                .setSmallIcon(R.mipmap.iconearth)
//                .setStyle(NotificationCompat.BigTextStyle())
//                .setColorized(true)
//                .setContentTitle("Mag-$mag")
//                .setContentInfo(place)
//                .setAutoCancel(true) as NotificationCompat.Builder
//    }
//


    private fun getBasicNotificationBuilder(context: Context,pendingIntent: PendingIntent, mag: Double, place: String, channelId: String, playSound: Boolean)
            : NotificationCompat.Builder{
        val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val nBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.iconearth)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle("Mag-$mag")
                .setContentInfo(place)
                .setDefaults(0)
        if (playSound) nBuilder.setSound(notificationSound)
        return nBuilder
    }


    @TargetApi(26)
    private fun NotificationManager.createNotificationChannel(channelID: String,
                                                              channelName: String,
                                                              playSound: Boolean){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
            else NotificationManager.IMPORTANCE_LOW
            val nChannel = NotificationChannel(channelID, channelName, channelImportance)
            nChannel.enableLights(true)
            nChannel.lightColor = Color.BLUE
            this.createNotificationChannel(nChannel)
        }
    }

}