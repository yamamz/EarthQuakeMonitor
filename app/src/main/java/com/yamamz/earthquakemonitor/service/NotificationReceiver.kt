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
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


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
            async (CommonPool) {
                getQuakesOnRefresh(context)
            }


            intentToRepeat = Intent(context, details_map_activity::class.java)
        }
        catch (ignore:Exception){
        }
    }

    private fun getQuakesOnRefresh(context:Context){

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
                    setnotify(earthQuakes!!,context)

                }
            }


        })
    }


    fun setnotify(earthQuakes: ArrayList<Feature>,context:Context){
try {

    Realm.init(context)
    val realm = Realm.getDefaultInstance()
    realm!!.executeTransactionAsync(object : Realm.Transaction {
        override fun execute(realm: Realm?) {
            val realmResult = realm!!.where(Notification::class.java).findAll()
            val i=0
            earthQuakes.filter {it.properties!!.mag!=null && it.properties!!.mag!! >= 6}.forEach {e ->


                    //loop the result and find if the eathquake id is not in notification
                    if (realmResult.none { it.notificationID == e.id }) {
                            val requestCode = ("someString" + System.currentTimeMillis()).hashCode()
                            val notId: Int = (System.currentTimeMillis()).hashCode() + i+1

                            //value to pass in activity
                            intentToRepeat!!.putExtra("n", e.geometry!!.coordinates!![0])
                            intentToRepeat!!.putExtra("e", e.geometry!!.coordinates!![1])
                            intentToRepeat!!.putExtra("time", e.properties!!.time)
                            intentToRepeat!!.putExtra("mag", e.properties!!.mag)
                            intentToRepeat!!.putExtra("depth", e.geometry!!.coordinates!![2])
                            intentToRepeat!!.putExtra("location", e.properties!!.place)

                            //set flag to restart/relaunch the app
                            intentToRepeat!!.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            //Pending intent to handle launch of Activity in intent above
                            pendingIntent = PendingIntent.getActivity(context, requestCode, intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT)
                            //Build notification
                            val repeatedNotification = buildLocalNotification(context, pendingIntent!!, e.properties!!.mag!!, e.properties!!.place!!).build()
                            //Send local notification
                            NotificationHelper.getNotificationManager(context).notify(notId, repeatedNotification)
                            //create notification object
                            val notification = Notification(e.id.toString())

                            //save the notification object to realm database
                            realm.copyToRealmOrUpdate(notification)

                    }

            }

        }

    }, Realm.Transaction.OnSuccess {

        realm.close()
    })

}catch (e:Exception){}
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
