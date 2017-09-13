package com.yamamz.earthquakemonitor.service

import android.R
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
import com.yamamz.earthquakemonitor.details_map_activity


/**
 * Created by AMRI on 9/8/2017.
 */

class NotificationReceiver : BroadcastReceiver() {
    var earthQuakes: ArrayList<Feature>?=null
    var intentToRepeat:Intent?=null
    var pendingIntent: PendingIntent?=null

    var metadata:Metadata?=null
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        try {
            getQuakesOnRefresh(context, intent)
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


                //query to server successfull with data
                if(earthQuakes!!.isNotEmpty()){
                    Log.e("YamamzNotification","not empty")
                    setnotify(earthQuakes,context)

                }
            }


        })
    }


    fun setnotify(earthQuakes: ArrayList<Feature>?,context:Context){


        Realm.init(context)
        val realm = Realm.getDefaultInstance()
        realm!!.executeTransactionAsync(object :Realm.Transaction{
            override fun execute(realm: Realm?) {
                val realmResult = realm?.where(Notification::class.java)?.findAll()
                for (i in 0 until earthQuakes!!.size) {
                    //loop the result and find if the eathquake id is not in notification
                    if(realmResult!!.none{it.notificationID == earthQuakes[i].id}){
                        //if earthquake is 6 plus magnitude notify the user
                    if (earthQuakes[i].properties?.mag!! >= 6) {
                        val requestCode = ("someString" + System.currentTimeMillis()).hashCode()
                        val notId: Int = (System.currentTimeMillis()).hashCode() + i

                        //value to pass in activity
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
                        //create notification object
                        val notification = Notification(earthQuakes[i].id.toString())

                        //save the notification object to realm database
                        realm.copyToRealmOrUpdate(notification)
                    }
                    }
                }

            }

        },Realm.Transaction.OnSuccess {

            realm.close()
        })


    }

    /**
     * Buld notification for earthquakes
     */
    private fun buildLocalNotification(context: Context, pendingIntent: PendingIntent, mag:Double, place:String): NotificationCompat.Builder {

        return NotificationCompat.Builder(context,"channel_id")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_menu_mylocation)
                .setStyle(NotificationCompat.BigTextStyle())
                .setColorized(true)
                .setContentTitle("Mag-$mag")
                .setContentInfo(place)
                .setAutoCancel(true) as NotificationCompat.Builder
    }



}
