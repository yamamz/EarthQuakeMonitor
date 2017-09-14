package com.yamamz.earthquakemonitor.service

/**
 * Created by AMRI on 9/4/2017.
 */

import android.R
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.yamamz.earthquakemonitor.NotificationHelper
import com.yamamz.earthquakemonitor.api.ApiServices
import com.yamamz.earthquakemonitor.details_map_activity
import com.yamamz.earthquakemonitor.model.EarthquakeGeoJSon
import com.yamamz.earthquakemonitor.model.Feature
import com.yamamz.earthquakemonitor.model.Metadata
import com.yamamz.earthquakemonitor.model.Notification
import io.realm.Realm
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList


class DeviceBootReceiver : BroadcastReceiver() {

    var earthQuakes: ArrayList<Feature>?=null
    var intentToRepeat:Intent?=null
    var pendingIntent: PendingIntent?=null

    var metadata: Metadata?=null
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        try {
            async (CommonPool) {
                getQuakesOnRefresh(context, intent)
            }
            intentToRepeat = Intent(context, details_map_activity::class.java)

        }
        catch (ignore:Exception){

        }

    }

    private fun getQuakesOnRefresh(context:Context,intent:Intent){


        val BASE_URL="https://earthquake.usgs.gov/"
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
                earthQuakes =response?.body()?.features
                metadata=response?.body()?.metadata



                if(earthQuakes!!.isNotEmpty()){
                    Log.e("YamamzBootNotification","not empty")
                    setnotify(earthQuakes,context)

                }
            }


        })
    }


    fun setnotify(earthQuakes: ArrayList<Feature>?, context:Context){


        Realm.init(context)
        val realm = Realm.getDefaultInstance()
        realm!!.executeTransactionAsync(object : Realm.Transaction{
            override fun execute(realm: Realm?) {

                val realmResult = realm?.where(Notification::class.java)?.findAll()
                for (i in 0 until earthQuakes!!.size) {
                    if(realmResult!!.none{it.notificationID == earthQuakes[i].id}){
                        if (earthQuakes[i].properties!!.mag!! >= 6) {
                            val requestCode = ("someString" + System.currentTimeMillis()).hashCode()
                            val notId: Int = (System.currentTimeMillis()).hashCode() + i
                            //Also add it to the intent, to make sure system sees it as different/modified
                            intentToRepeat!!.putExtra("id", earthQuakes[i].id)
                            intentToRepeat!!.putExtra("n", earthQuakes[i].geometry!!.coordinates!![0])
                            intentToRepeat!!.putExtra("e", earthQuakes[i].geometry!!.coordinates!![1])
                            intentToRepeat!!.putExtra("time", earthQuakes[i].properties!!.time)
                            intentToRepeat!!.putExtra("mag", earthQuakes[i].properties!!.mag)
                            intentToRepeat!!.putExtra("depth",  earthQuakes[i].geometry!!.coordinates!![2])
                            intentToRepeat!!.putExtra("location", earthQuakes[i].properties!!.place)
                            //set flag to restart/relaunch the app
                            intentToRepeat!!.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            //Pending intent to handle launch of Activity in intent above
                            pendingIntent = PendingIntent.getActivity(context, requestCode, intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT)
                            //Build notification
                            val repeatedNotification = buildLocalNotification(context, pendingIntent!!, earthQuakes[i].properties!!.mag!!, earthQuakes[i].properties!!.place!!).build()
                            //Send local notification
                            NotificationHelper.getNotificationManager(context).notify(notId, repeatedNotification)
                            val notification = Notification(earthQuakes[i].id.toString())
                            realm.copyToRealmOrUpdate(notification)
                        }
                    }
                }

            }

        }, Realm.Transaction.OnSuccess {

            realm.close()
        })


    }



    private fun buildLocalNotification(context: Context, pendingIntent: PendingIntent, mag:Double, place:String): NotificationCompat.Builder {


        return NotificationCompat.Builder(context,"channel_id")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_menu_mylocation)
                .setContentTitle("Mag-$mag")
                .setContentInfo(place)
                .setAutoCancel(true) as NotificationCompat.Builder
    }



}
