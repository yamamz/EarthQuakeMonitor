package com.yamamz.earthquakemonitor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.yamamz.earthquakemonitor.adapter.QuakeAdapter
import com.yamamz.earthquakemonitor.api.ApiServices
import com.yamamz.earthquakemonitor.model.*
import com.yamamz.earthquakemonitor.ui.DeviderItemDecoration
import com.yamamz.earthquakemonitor.view.Settings
import io.realm.Realm
import io.realm.RealmResults

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

 var earthQuakes: ArrayList<Feature>?=null
    val earthQuakelist=ArrayList<EarthQuake>()
    var earthquaketoPass=ArrayList<MyItem>()

    var mAdapter: QuakeAdapter?=null
    var realm:Realm?=null
    var realmResult:RealmResults<EarthquakeRealmModel>?=null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Realm.init(this)
        realm = Realm.getDefaultInstance()

        fab.setOnClickListener {

            val intent = Intent(this, AllEarthquakeActivity::class.java)
            startActivity(intent)

        }




       setupRecyclerView()
        loadLocalDb()

    swipeContainer.setOnRefreshListener {
        if(isNetworkAvailable()) {
    getQuakesOnRefresh()

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
    }

        else{
            swipeContainer.isRefreshing = false
            toast("No network is available")
        }
}



    }

    private fun loadLocalDb(){

            realm!!.executeTransactionAsync(object : Realm.Transaction {
                override fun execute(realm: Realm?) {

                    realmResult=realm!!.where(EarthquakeRealmModel::class.java).findAll()
                    for(i in 0 until realmResult!!.size){
                        val now = Date()
                        val past = convertTime(realmResult!![i].dateOccur!!)
                        val timeAgo = timeAgo(past, now)
                        val earthquake=EarthQuake(realmResult!![i].mag!!, realmResult!![i].location!!,
                                timeAgo, realmResult!![i].dept!!)
                        earthQuakelist.add(earthquake)
                    }

                }

            }, Realm.Transaction.OnSuccess {
                mAdapter!!.notifyDataSetChanged()

                if(earthQuakelist.size<=0) {
                    getQuakes()

                }
            })







    }

    private fun getQuakesOnRefresh(){
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val earthquake_category = sharedPrefs.getString("earthquake_category", "all")
        val time_category = sharedPrefs.getString("time_category", "day")

        val BASE_URL="https://earthquake.usgs.gov/"
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiServices = retrofit.create(ApiServices::class.java)


        var call:Call<EarthquakeGeoJSon>?=null
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
                    mAdapter!!.clear()


                    swipeContainer.isRefreshing = false
                    val realm = Realm.getDefaultInstance()

    realm!!.executeTransactionAsync(object : Realm.Transaction {
        override fun execute(realm: Realm?) {
            realm!!.delete(EarthquakeRealmModel::class.java)

        }

    }, Realm.Transaction.OnSuccess {
        toast("not close")
        addEarthquakes()
        realm.close()
    })



                val handler=Handler()
                    handler.post(Runnable {
                        for(i in 0 until earthQuakes!!.size) {
                            val now = Date()
                            val past = convertTime(earthQuakes!![i].properties!!.time!!)
                            val timeAgo = timeAgo(past, now)
                            val earthQuake = EarthQuake(earthQuakes!![i].properties?.mag!!, earthQuakes!![i].properties!!.place!!,
                                    timeAgo, earthQuakes!![i].geometry!!.coordinates!![2])
                            earthQuakelist.add(earthQuake)
                            mAdapter?.notifyDataSetChanged()

                        }

                    })




                }



            }


        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
            val intent= Intent(this@MainActivity, Settings::class.java)
                startActivity(intent)
            true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_earthquake)
        mAdapter = QuakeAdapter(this@MainActivity, earthQuakelist)
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(DeviderItemDecoration(this@MainActivity, LinearLayoutManager.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

    }

    @SuppressLint("SimpleDateFormat")
    fun convertTime(time: Long): Date {
        val date = Date(time)

        return date
    }
    private fun getQuakes(){

       val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val earthquake_category = sharedPrefs.getString("earthquake_category", "all")
        val time_category = sharedPrefs.getString("time_category", "day")

        val BASE_URL="https://earthquake.usgs.gov/"
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiServices = retrofit.create(ApiServices::class.java)


var call:Call<EarthquakeGeoJSon>?=null
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

                if(earthQuakes!!.isNotEmpty()) {
                    mAdapter!!.clear()
                    val realm = Realm.getDefaultInstance()
                    realm!!.executeTransactionAsync(object : Realm.Transaction {
                        override fun execute(realm: Realm?) {
                            realm!!.delete(EarthquakeRealmModel::class.java)

                        }

                    }, Realm.Transaction.OnSuccess {
                        addEarthquakes()
                        realm.close()
                    })

                    val handler=Handler()

                    handler.post(Runnable {for (i in 0 until earthQuakes!!.size) {

                        //val now =convertTime(earthQuakes!![i].properties!!.updated!!)
                        val now = Date()
                        val past = convertTime(earthQuakes!![i].properties!!.time!!)
                        val timeAgo = timeAgo(past, now)
                        val earthQuake = EarthQuake(earthQuakes!![i].properties?.mag!!, earthQuakes!![i].properties!!.place!!,
                                timeAgo, earthQuakes!![i].geometry!!.coordinates!![2])
                        earthQuakelist.add(earthQuake)
                        mAdapter?.notifyDataSetChanged()

                    } })


                }

            }


        })
}

fun addEarthquakes(){

    val realm = Realm.getDefaultInstance()
    realm!!.executeTransactionAsync(object :Realm.Transaction{
        override fun execute(realm: Realm?) {
            for(i in 0 until earthQuakes!!.size) {

                val earthquakesRealm = EarthquakeRealmModel(earthQuakes!![i].properties!!.place!!, earthQuakes!![i].properties!!.mag, earthQuakes!![i].geometry!!.coordinates!![0], earthQuakes!![i].geometry!!.coordinates!![1],
                        earthQuakes!![i].properties!!.time, earthQuakes!![i].geometry!!.coordinates!![2])
realm!!.copyToRealm(earthquakesRealm)
            }
        }

    },Realm.Transaction.OnSuccess {
realm.close()
    })
}

    private fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    fun timeAgo(past:Date,now:Date):String {

            val seconds= TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
            val minutes=TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
            val hours=TimeUnit.MILLISECONDS.toHours(now.time - past.time)
            val days=TimeUnit.MILLISECONDS.toDays(now.time - past.time)


        return when {
            seconds<60 -> if(seconds>1) "$seconds seconds ago" else "$seconds second ago"
            minutes<60 -> if(minutes>1) "$minutes minutes ago" else "$minutes minute ago"
            hours<24 -> if(hours>1) "$hours hours ago" else "$hours hour ago"
            else -> if(days>1) "$days days ago" else "$days day ago"
        }
        }

    fun goTodetails(position: Int) {

        try {
            val realmResult = realm!!.where(EarthquakeRealmModel::class.java).findAll()
            val north = realmResult!![position].lat
            val east = realmResult[position].lon
            val time = realmResult[position].dateOccur
            val mag = realmResult[position].mag
            val depth = realmResult[position].dept

            val location = realmResult[position].location
            val intent = Intent(this, details_map_activity::class.java)
            intent.putExtra("n", north)
            intent.putExtra("e", east)
            intent.putExtra("time", time)
            intent.putExtra("mag", mag)
            intent.putExtra("depth", depth)
            intent.putExtra("location", location)
            startActivity(intent)

        }catch (e:Exception){

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        realm!!.close()
            }


    private fun isNetworkAvailable():Boolean  {
  val connectivityManager =getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}





