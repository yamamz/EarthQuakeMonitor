package com.yamamz.earthquakemonitor

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.View

import android.widget.Toast

import com.yamamz.earthquakemonitor.adapter.QuakeAdapter
import com.yamamz.earthquakemonitor.api.ApiServices
import com.yamamz.earthquakemonitor.model.*
import com.yamamz.earthquakemonitor.service.AlarmReceiver
import com.yamamz.earthquakemonitor.service.NotificationReceiver

import com.yamamz.earthquakemonitor.ui.DeviderItemDecoration
import com.yamamz.earthquakemonitor.view.Settings
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_details_map_activity2.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI


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

var pendingIntent:PendingIntent?=null
    var pendingNotificationIntent:PendingIntent?=null
    var mAdapter: QuakeAdapter?=null
    var realm:Realm?=null
    var realmResult:RealmResults<EarthquakeRealmModel>?=null
    var manager:AlarmManager? = null
    var alarmIntent:Intent?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        realm = Realm.getDefaultInstance()



    /* Retrieve a PendingIntent that will perform a broadcast */
   alarmIntent = Intent(MainActivity@this,AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(MainActivity@this, 1, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationIntent= Intent(MainActivity@this,NotificationReceiver::class.java)
        pendingNotificationIntent = PendingIntent.getBroadcast(MainActivity@this, 2, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT)

        start()
        startNotification()
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

        onNewIntent(intent)

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, IntentFilter("updaterecyclerView"))
    }


    override public fun onNewIntent(intent:Intent ) {
        val extras = intent.extras
        if (extras != null) {

            val msg = extras.getString("id")

            toast(msg)
        }

        }
    private fun start() {
        manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
       val interval:Long = 60000

        manager?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent)

    }

    private fun startNotification() {
        val manager:AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval:Long = 60000

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingNotificationIntent)

    }



    private fun loadLocalDb(){

        val realm = Realm.getDefaultInstance()
        try {
            realm!!.executeTransactionAsync(object : Realm.Transaction {

                override fun execute(realm: Realm?) {

                        realmResult = realm?.where(EarthquakeRealmModel::class.java)?.findAll()




                    Log.e("Yamamz", "Load data successfully ${realmResult?.size} ")

                   realmResult?.filter{it.mag!=null && it.location!=null && it.dept!=null}?.forEach {
                        val now = Date()
                        val past = convertTime(it.dateOccur?:0)
                        val timeAgo = timeAgo(past, now)
                            val earthquake = EarthQuake(it?.mag?:0.0, it.location?:"", timeAgo, it.dept?:0.0)
                            earthQuakelist.add(earthquake)

                    }

                }

            }, Realm.Transaction.OnSuccess {

                mAdapter?.notifyDataSetChanged()

                if (earthQuakelist.size <= 0) {

                    if (isNetworkAvailable()) {
                        pbLoading.visibility = View.VISIBLE
                        getQuakes()
                    }
                }
            })

        }
       finally{
           realm.close()
        }
    }

 fun loaddataFromdb(){


}

    private fun loaddata(){

            mAdapter?.clear()
            Log.e("yamamz","adapter clear")


        try {
            realm!!.executeTransactionAsync(object : Realm.Transaction {

                override fun execute(realm: Realm?) {

                    realmResult = realm?.where(EarthquakeRealmModel::class.java)?.findAll()



                        Log.e("Yamamz", "Load data successfully ${realmResult?.size} ")

                        realmResult?.filter {it.mag!=null && it.location!=null && it.dept!=null}?.forEach {
                            val now = Date()
                            val past = convertTime(it.dateOccur?:0)
                            val timeAgo = timeAgo(past, now)

                            val earthquake = EarthQuake(it.mag?:0.0, it.location?:"", timeAgo, it.dept?:0.0)
                            earthQuakelist.add(earthquake)
                        }


                }

            }, Realm.Transaction.OnSuccess {

                mAdapter?.notifyDataSetChanged()

            })

        }
        catch (e:Exception){
        }
    }


    private fun checkRecyclerViewIsemplty() {
        if (mAdapter?.itemCount == 0) {
            empty.visibility = View.VISIBLE
        } else {

            empty.visibility = View.GONE
        }


    }

    private fun checkAdapter() {
        mAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkRecyclerViewIsemplty()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                checkRecyclerViewIsemplty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkRecyclerViewIsemplty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkRecyclerViewIsemplty()
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

        call?.enqueue(object : Callback<EarthquakeGeoJSon> {
            override fun onFailure(call: Call<EarthquakeGeoJSon>?, t: Throwable?) {


            }

            override fun onResponse(call: Call<EarthquakeGeoJSon>?, response: Response<EarthquakeGeoJSon>?) {
                earthQuakes =response?.body()?.features



                if(earthQuakes?.isNotEmpty() == true){
                    mAdapter?.clear()

                    pbLoading.visibility=View.GONE
                    swipeContainer.isRefreshing = false
                    val realm = Realm.getDefaultInstance()

    realm!!.executeTransactionAsync(Realm.Transaction { realmAsync ->
        realmAsync!!.delete(EarthquakeRealmModel::class.java) }
            , Realm.Transaction.OnSuccess {
        addEarthquakes()
        realm.close()
    })



                val handler=Handler()
                    handler.post({
                        earthQuakes?.filter{ it.properties?.mag!=null}?.forEach {
                            val now = Date()
                            val past = convertTime(it.properties?.time?:0)
                            val timeAgo = timeAgo(past, now)

                                val earthQuake = EarthQuake(it.properties?.mag?:0.0, it.properties?.place?:"",
                                        timeAgo, it.geometry?.coordinates?.get(2)?:0.0)
                                earthQuakelist.add(earthQuake)
                                mAdapter?.notifyDataSetChanged()

                        }

                    })

                }

            }
        })
    }




    private val mMessageReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(contxt: Context?, intent: Intent?) {
            try {

                if(intent?.action != null && intent.action == "updaterecyclerView"){
                    loaddata()
                    Log.e("yamamz","succesfully loaded the data")
                }

            } catch (e:Exception){

            }
        }
    }

    override fun onResume() {
        super.onResume()


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
R.id.action_about -> {
    val builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
    val positiveText = getString(android.R.string.ok)
    builder.setTitle(getString(R.string.dialog_title))
    builder.setMessage(getString(R.string.dialog_message1))
    builder.setPositiveButton(positiveText) { _, _ ->

    }

    val dialog = builder.create()
    dialog.show()
    return true
}
            R.id.action_search -> {

                if(isNetworkAvailable()) {
                    pbLoading.visibility = View.VISIBLE
                    getQuakesOnRefresh()
                }
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
        checkAdapter()

    }

    @SuppressLint("SimpleDateFormat")
    fun convertTime(time: Long): Date {
        val date = Date(time)

        return date
    }
    private fun getQuakes(){

       val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val earthquake_category = sharedPrefs.getString("earthquake_category", "two")
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
                    pbLoading.visibility = View.GONE
                    mAdapter?.clear()
                    val realm = Realm.getDefaultInstance()
                    realm?.executeTransactionAsync(Realm.Transaction { realmAsync ->
                        realmAsync?.delete(EarthquakeRealmModel::class.java) },
                            Realm.Transaction.OnSuccess {
                        addEarthquakes()
                        realm.close()
                    })

                    val handler=Handler()

                    handler.post({

                        earthQuakes?.filter{it.properties?.mag!=null}?.forEach {


                        val now = Date()
                        val past = convertTime(it.properties?.time?:0)
                        val timeAgo = timeAgo(past, now)
                        val earthQuake = EarthQuake(it.properties?.mag?:0.0, it.properties?.place?:"",
                                timeAgo, it.geometry?.coordinates?.get(2)?:0.0)
                        earthQuakelist.add(earthQuake)
                        mAdapter?.notifyDataSetChanged()

                    } })


                }

            }


        })
}

fun addEarthquakes(){

    val realm = Realm.getDefaultInstance()
    realm!!.executeTransactionAsync(Realm.Transaction { realmAsync ->
        earthQuakes?.forEach {

            val earthquakesRealm = EarthquakeRealmModel(it.properties?.place, it.properties?.mag, it.geometry?.coordinates?.get(0), it.geometry?.coordinates?.get(1),
                    it.properties?.time, it.geometry?.coordinates?.get(2),it.id)
            realmAsync?.copyToRealmOrUpdate(earthquakesRealm)
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
            val realmResult = realm?.where(EarthquakeRealmModel::class.java)?.findAll()
            val north = realmResult?.get(position)?.lat
            val east = realmResult?.get(position)?.lon
            val time = realmResult?.get(position)?.dateOccur
            val mag = realmResult?.get(position)?.mag
            val depth = realmResult?.get(position)?.dept

            val location = realmResult?.get(position)?.location
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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)


            }


    private fun isNetworkAvailable():Boolean  {
  val connectivityManager =getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}





