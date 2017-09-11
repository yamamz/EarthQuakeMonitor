package com.yamamz.earthquakemonitor.service

/**
 * Created by AMRI on 9/4/2017.
 */

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.yamamz.earthquakemonitor.api.ApiServices
import com.yamamz.earthquakemonitor.model.EarthquakeGeoJSon
import com.yamamz.earthquakemonitor.model.EarthquakeRealmModel
import com.yamamz.earthquakemonitor.model.Feature
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

import com.yamamz.earthquakemonitor.MainActivity


class AlarmReceiver : BroadcastReceiver() {
    var earthQuakes: ArrayList<Feature>?=null
    var intentToRepeat:Intent?=null


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
try {
    getQuakesOnRefresh(context, intent)
    intentToRepeat = Intent(context, MainActivity::class.java)



}
catch (ignore:Exception){

}

    }

    private fun getQuakesOnRefresh(context:Context,intent:Intent){
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val earthquake_category = sharedPrefs.getString("earthquake_category", "all")
        val time_category = sharedPrefs.getString("time_category", "day")

        val BASE_URL="https://earthquake.usgs.gov/"
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiServices = retrofit.create(ApiServices::class.java)


        var call: Call<EarthquakeGeoJSon>?=null
        when(earthquake_category){

            "one" ->{
                when(time_category){

                    "hour" -> call = apiServices.earthQuakesLasthour1_0
                    "day" -> call = apiServices.earthQuakesLastday1_0
                    "week"->call = apiServices.earthQuakesLastweek1_0
                    "month"->call = apiServices.earthQuakesLastmonth1_0
                }

            }

            "two" ->{
                when(time_category){

                    "hour" -> call = apiServices.earthQuakesLasthour2_5
                    "day" -> call = apiServices.earthQuakesLastday2_5
                    "week"->call = apiServices.earthQuakesLastweek2_5
                    "month"->call = apiServices.earthQuakesLastmonth2_5
                }

            }

            "four" ->{

                when(time_category){

                    "hour" -> call = apiServices.earthQuakesLasthour4_5
                    "day" -> call = apiServices.earthQuakesLastday4_5
                    "week"->call = apiServices.earthQuakesLastweek4_5
                    "month"->call = apiServices.earthQuakesLastmonth4_5
                }

            }

            "all" ->{
                when(time_category){

                    "hour" -> call = apiServices.earthQuakesLasthourAll
                    "day" -> call = apiServices.earthQuakesLastdayAll
                    "week"->call = apiServices.earthQuakesLastweekAll
                    "month"->call = apiServices.earthQuakesLastmonthAll
                }

            }

            "significant" ->{

                when(time_category){

                    "hour" -> call = apiServices.earthQuakesLasthourSig
                    "day" -> call = apiServices.earthQuakesLastdaySig
                    "week"->call = apiServices.earthQuakesLastweekSig
                    "month"->call = apiServices.earthQuakesLastmonthSig
                }

            }

        }

        call!!.enqueue(object : Callback<EarthquakeGeoJSon> {
            override fun onFailure(call: Call<EarthquakeGeoJSon>?, t: Throwable?) {


            }

            override fun onResponse(call: Call<EarthquakeGeoJSon>?, response: Response<EarthquakeGeoJSon>?) {
                earthQuakes =response?.body()?.features



                if(earthQuakes!!.isNotEmpty()){

                    Log.e("Yamamz","the query is successful")
                    Realm.init(context)
                    val realm = Realm.getDefaultInstance()

                    realm!!.executeTransactionAsync(object : Realm.Transaction {
                        override fun execute(realm: Realm?) {
                            realm!!.delete(EarthquakeRealmModel::class.java)
                            Log.e("Yamamz","realm db is clear")
                        }

                    }, Realm.Transaction.OnSuccess {
                        addEarthquakes(context)
                        realm.close()
                        Log.e("Yamamz","realm instance in close")
                    })


                }



            }


        })
    }

    fun addEarthquakes(context:Context){

        val realm = Realm.getDefaultInstance()
        realm!!.executeTransactionAsync(object :Realm.Transaction{
            override fun execute(realm: Realm?) {
                for(i in 0 until earthQuakes!!.size) {

                    val earthquakesRealm = EarthquakeRealmModel(earthQuakes!![i].properties!!.place!!, earthQuakes!![i].properties!!.mag, earthQuakes!![i].geometry!!.coordinates!![0], earthQuakes!![i].geometry!!.coordinates!![1],
                            earthQuakes!![i].properties!!.time, earthQuakes!![i].geometry!!.coordinates!![2],
                            earthQuakes!![i].id)
                    realm!!.copyToRealmOrUpdate(earthquakesRealm)





                }
            }

        },Realm.Transaction.OnSuccess {
            Log.e("Yamamz","items successfully add")
            val intent = Intent("updaterecyclerView")
            sendBroadcast(intent,context)
            realm.close()

        })
    }


    private fun sendBroadcast(intent: Intent,context:Context) {

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

    }



}
