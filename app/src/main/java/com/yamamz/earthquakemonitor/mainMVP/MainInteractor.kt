package com.yamamz.earthquakemonitor.mainMVP

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.yamamz.earthquakemonitor.adapter.QuakeAdapter
import com.yamamz.earthquakemonitor.api.ApiServices
import com.yamamz.earthquakemonitor.MyApplication
import com.yamamz.earthquakemonitor.model.EarthQuake
import com.yamamz.earthquakemonitor.model.EarthquakeGeoJSon
import com.yamamz.earthquakemonitor.model.EarthquakeRealmModel
import com.yamamz.earthquakemonitor.model.Feature
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by AMRI on 11/6/2017.
 */
class MainInteractor(val presenter: MainMVP.Presenter) : MainMVP.Interactor {

    override fun goToDeatails(position: Int) {
        Realm.init(MyApplication())
        val realm = Realm.getDefaultInstance()
        try {
            val realmResult = realm?.where(EarthquakeRealmModel::class.java)?.findAllAsync()
            val north = realmResult?.get(position)?.lat
            val east = realmResult?.get(position)?.lon
            val time = realmResult?.get(position)?.dateOccur
            val mag = realmResult?.get(position)?.mag
            val depth = realmResult?.get(position)?.dept
            val location = realmResult?.get(position)?.location
            presenter.showDetails(north,east,time,mag,depth,location)
        } catch (e: Exception) {

        } finally {
            realm.close()
        }

    }

    override fun checkinternetConnectivity(earthquake_category: String, time_category: String) {
        val internetAvail: Deferred<Boolean> = bg {

            try {
                Socket().use({ socket ->
                    socket.connect(InetSocketAddress("www.google.com", 80), 2000)
                    true
                })
            } catch (e: IOException) {
                // Either we have a timeout or unreachable host or failed DNS lookup
                System.out.println(e)
                false
            }


        }
        async(UI) {
            if (internetAvail.await()) {
                presenter.showLoading()
                getEarthquakes(earthquake_category,time_category)
            }
        }
    }


    override fun checkAdapterSize(mAdapter: QuakeAdapter?) {
        mAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                presenter.checkRecyclerViewIsemplty(mAdapter)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                presenter.checkRecyclerViewIsemplty(mAdapter)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                presenter.checkRecyclerViewIsemplty(mAdapter)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                presenter.checkRecyclerViewIsemplty(mAdapter)
            }
        })

    }

    var realmResult: RealmResults<EarthquakeRealmModel>? = null
    override fun searchDataFromRealm() {
        Realm.init(MyApplication())
        val realm = Realm.getDefaultInstance()
        val earthQuakelist = ArrayList<EarthQuake>()
        try {
            realm?.executeTransactionAsync(Realm.Transaction { realmAsync ->
                realmResult = realmAsync?.where(EarthquakeRealmModel::class.java)?.findAll()
                Log.e("YamamzMVP", "Load data successfully ${realmResult?.size} ")

                realmResult?.filter { it.mag != null && it.location != null && it.dept != null }?.forEach {
                    val now = Date()
                    val past = convertTime(it.dateOccur ?: 0)
                    val timeAgo = timeAgo(past, now)
                    val earthquake = EarthQuake(it?.mag ?: 0.0, it.location ?: "", timeAgo, it.dept ?: 0.0)
                    earthQuakelist.add(earthquake)

                }
            }, Realm.Transaction.OnSuccess {
                presenter.setRecyclerView(earthQuakelist)
            })

        } finally {
            realm.close()
        }
    }


    override fun getEarthquakes(earthquake_category: String, time_category: String) {
        var earthquakes: ArrayList<Feature>?
        val BASE_URL = "https://earthquake.usgs.gov/"
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiServices = retrofit.create(ApiServices::class.java)


        var call: Call<EarthquakeGeoJSon>? = null
        when (earthquake_category) {

            "one" -> {
                when (time_category) {

                    "hour" -> call = apiServices.earthQuakesLasthour1_0
                    "day" -> call = apiServices.earthQuakesLastday1_0
                    "week" -> call = apiServices.earthQuakesLastweek1_0
                    "month" -> call = apiServices.earthQuakesLastmonth1_0
                }

            }

            "two" -> {
                when (time_category) {

                    "hour" -> call = apiServices.earthQuakesLasthour2_5
                    "day" -> call = apiServices.earthQuakesLastday2_5
                    "week" -> call = apiServices.earthQuakesLastweek2_5
                    "month" -> call = apiServices.earthQuakesLastmonth2_5
                }

            }


            "four" -> {

                when (time_category) {

                    "hour" -> call = apiServices.earthQuakesLasthour4_5
                    "day" -> call = apiServices.earthQuakesLastday4_5
                    "week" -> call = apiServices.earthQuakesLastweek4_5
                    "month" -> call = apiServices.earthQuakesLastmonth4_5
                }

            }

            "all" -> {
                when (time_category) {

                    "hour" -> call = apiServices.earthQuakesLasthourAll
                    "day" -> call = apiServices.earthQuakesLastdayAll
                    "week" -> call = apiServices.earthQuakesLastweekAll
                    "month" -> call = apiServices.earthQuakesLastmonthAll
                }

            }

            "significant" -> {

                when (time_category) {

                    "hour" -> call = apiServices.earthQuakesLasthourSig
                    "day" -> call = apiServices.earthQuakesLastdaySig
                    "week" -> call = apiServices.earthQuakesLastweekSig
                    "month" -> call = apiServices.earthQuakesLastmonthSig
                }

            }

        }

        call?.enqueue(object : Callback<EarthquakeGeoJSon> {
            override fun onFailure(call: Call<EarthquakeGeoJSon>?, t: Throwable?) {


            }

            override fun onResponse(call: Call<EarthquakeGeoJSon>?, response: Response<EarthquakeGeoJSon>?) {
                earthquakes = response?.body()?.features

                Log.e("YamamzMVP", earthquakes?.size.toString())
                val earthQuakelist = ArrayList<EarthQuake>()
                if (earthquakes != null) {
                    Realm.init(MyApplication())
                    val realm = Realm.getDefaultInstance()
                    realm?.executeTransactionAsync(Realm.Transaction { realmAsync ->
                        realmAsync?.delete(EarthquakeRealmModel::class.java)
                    }
                            , Realm.Transaction.OnSuccess {
                        addEarthquakes(earthquakes!!)
                        realm.close()
                    })

                    earthquakes?.filter { it.properties?.mag != null }?.forEach {

                        val now = Date()
                        val past = convertTime(it.properties?.time ?: 0)
                        val timeAgo = timeAgo(past, now)

                        val earthQuake = EarthQuake(it.properties?.mag ?: 0.0, it.properties?.place ?: "",
                                timeAgo, it.geometry?.coordinates?.get(2) ?: 0.0)
                        earthQuakelist.add(earthQuake)
                    }
                    presenter.setRecyclerView(earthQuakelist)
                }

            }
        })

    }

    fun addEarthquakes(earthquakes: ArrayList<Feature>) {
        Realm.init(MyApplication())
        val realm = Realm.getDefaultInstance()
        realm?.executeTransactionAsync(Realm.Transaction { realmAsync ->
            earthquakes.forEach {
                val earthquakesRealm = EarthquakeRealmModel(it.properties?.place, it.properties?.mag, it.geometry?.coordinates?.get(0), it.geometry?.coordinates?.get(1),
                        it.properties?.time, it.geometry?.coordinates?.get(2), it.id)
                realmAsync?.copyToRealmOrUpdate(earthquakesRealm)
            }
        }, Realm.Transaction.OnSuccess {
            realm.close()
        })
    }

    fun timeAgo(past: Date, now: Date): String {

        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
        val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)


        return when {
            seconds < 60 -> if (seconds > 1) "$seconds seconds ago" else "$seconds second ago"
            minutes < 60 -> if (minutes > 1) "$minutes minutes ago" else "$minutes minute ago"
            hours < 24 -> if (hours > 1) "$hours hours ago" else "$hours hour ago"
            else -> if (days > 1) "$days days ago" else "$days day ago"
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTime(time: Long): Date {
        val date = Date(time)

        return date
    }

}