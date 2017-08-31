package com.yamamz.earthquakemonitor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.yamamz.earthquakemonitor.model.EarthQuake
import com.yamamz.earthquakemonitor.model.Feature
import com.yamamz.earthquakemonitor.model.MyItem

import kotlinx.android.synthetic.main.activity_main2.*
import org.json.JSONException
import com.google.android.gms.maps.model.Marker
import android.view.View
import android.widget.TextView
import com.yamamz.earthquakemonitor.model.EarthquakeRealmModel
import io.realm.Realm
import kotlinx.android.synthetic.main.info_window_layout.*


class AllEarthquakeActivity : AppCompatActivity(), OnMapReadyCallback{

    var mClusterManager: ClusterManager<MyItem>? = null
    var mMap:GoogleMap?=null
    var realm: Realm?=null
    val ArrayItems: ArrayList<MyItem>? = ArrayList()

    var clickedClusterItem: MyItem? = null
    override fun onMapReady(p0: GoogleMap?) {

        mMap=p0


            readItems()

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)
        realm = Realm.getDefaultInstance()
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        fab.setOnClickListener { view ->
val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }



    private fun readItems() {


            realm!!.executeTransactionAsync(object : Realm.Transaction {
                override fun execute(realm: Realm?) {

                    val realmResult = realm!!.where(EarthquakeRealmModel::class.java).findAll()

                    (0 until realmResult.size)
                            .asSequence()
                            .map { MyItem(realmResult[it].lat!!, realmResult[it].lon!!, realmResult[it].location, realmResult[it].mag.toString()) }
                            .forEach { ArrayItems!!.add(it) }


                }

            }, Realm.Transaction.OnSuccess {
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.503186, -0.126446), 2f))
                mClusterManager = ClusterManager(this, mMap)
                mMap!!.setOnCameraIdleListener(mClusterManager)
                mMap!!.setOnMarkerClickListener(mClusterManager)
                mMap!!.setInfoWindowAdapter(mClusterManager!!.markerManager)
                mMap!!.setOnInfoWindowClickListener(mClusterManager) //added

                mClusterManager!!.setOnClusterItemClickListener({ item ->
                    clickedClusterItem = item
                    false
                })

                mClusterManager!!.markerCollection.setOnInfoWindowAdapter(
                        MyCustomAdapterForItems())

                mMap!!.setOnCameraIdleListener(mClusterManager)

                mClusterManager!!.addItems(ArrayItems)

            })


    }
    private fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    inner class MyCustomAdapterForItems internal constructor() : GoogleMap.InfoWindowAdapter {

     private val myContentsView: View = layoutInflater.inflate(
                R.layout.info_window_layout, null)

        override fun getInfoWindow(marker: Marker): View? {

            return null
        }

        @SuppressLint("SetTextI18n")
        override fun getInfoContents(marker: Marker): View {


            val tvTitle = myContentsView.findViewById<TextView>(R.id.txtTitle)
            val tvSnippet = myContentsView.findViewById<TextView>(R.id.txtSnippet)

            tvTitle.text = clickedClusterItem!!.getmTitle()
            tvSnippet.text = "Mag ${clickedClusterItem!!.getmSnippet()}"

            return myContentsView
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm!!.close()
    }
}
