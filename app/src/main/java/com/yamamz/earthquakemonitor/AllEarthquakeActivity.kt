package com.yamamz.earthquakemonitor

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.yamamz.earthquakemonitor.model.MyItem

import kotlinx.android.synthetic.main.activity_main2.*
import org.json.JSONException
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.geojson.GeoJsonLayer
import com.google.maps.android.geojson.GeoJsonPointStyle
import io.realm.Realm
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.Ref
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.coroutines.experimental.bg
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.lang.ref.WeakReference
import java.net.URL


class AllEarthquakeActivity : AppCompatActivity(), OnMapReadyCallback{

    var mClusterManager: ClusterManager<MyItem>? = null
    var mMap:GoogleMap?=null
    var realm: Realm?=null
    val ArrayItems: ArrayList<MyItem>? = ArrayList()
    private val mLogTag = "GeoJsonDemo"
    var clickedClusterItem: MyItem? = null
    override fun onMapReady(p0: GoogleMap?) {
        mMap=p0


        //retrieveFileFromUrl()
        loadAndShowData()

    }




    /**
     * Assigns a color based on the given magnitude
     */
    private fun magnitudeToColor(magnitude: Double): Float {
        return if (magnitude < 1.0) {
            BitmapDescriptorFactory.HUE_CYAN
        } else if (magnitude < 2.5) {
            BitmapDescriptorFactory.HUE_GREEN
        } else if (magnitude < 4.5) {
            BitmapDescriptorFactory.HUE_YELLOW
        } else {
            BitmapDescriptorFactory.HUE_RED
        }
    }
    fun loadAndShowData() {
        // Ref<T> uses the WeakReference under the hood
        val ref: Ref<AllEarthquakeActivity> = this.asReference()

        async(UI) {
            val data: Deferred<GeoJsonLayer?> = bg{downloadGeoJson(getString(R.string.geojson_url))}
            // Use ref() instead of this@MyActivity
            ref().showData(data.await()!!)
        }
    }
    fun showData(data: GeoJsonLayer) {
        addGeoJsonLayerToMap(data)
    }
    private fun retrieveFileFromUrl() {
       //DownloadGeoJsonFile(this).execute(getString(R.string.geojson_url))

    }

    fun downloadGeoJson(url:String):GeoJsonLayer?{
        try {
            // Open a stream from the URL
            val stream = URL(url).openStream()


            val result = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream) as Reader?)
            var line : String?
            do {
                line = reader.readLine()
                if (line == null)
                    break
                result.append(line)
            } while (true)
            // Close the stream
            reader.close()
            stream.close()

            return GeoJsonLayer(mMap, JSONObject(result.toString()))


        } catch (e: IOException) {
            Log.e("Yamamz", "GeoJSON file could not be read")
        } catch (e: JSONException) {
            Log.e("yamamz", "GeoJSON file could not be converted to a JSONObject")
        }
        return null
    }

    private fun addColorsToMarkers(layer: GeoJsonLayer) {
        // Iterate over all the features stored in the layer
        for (feature in layer.features) {
            // Check if the magnitude property exists
            if (feature.getProperty("mag") != null && feature.hasProperty("place")) {
                val magnitude = java.lang.Double.parseDouble(feature.getProperty("mag"))

                // Get the icon for the feature
                val pointIcon = BitmapDescriptorFactory
                        .defaultMarker(magnitudeToColor(magnitude))

                // Create a new point style
                val pointStyle = GeoJsonPointStyle()

                // Set options for the point style
                pointStyle.icon = pointIcon
                pointStyle.title = "Magnitude of " + magnitude
                pointStyle.snippet = "Earthquake occured " + feature.getProperty("place")

                // Assign the point style to the feature
                feature.pointStyle = pointStyle
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        realm = Realm.getDefaultInstance()
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }


    private  class DownloadGeoJsonFile : AsyncTask<String, Void, GeoJsonLayer> {

        private var activityReference: WeakReference<AllEarthquakeActivity>? = null


constructor(context:AllEarthquakeActivity){
    activityReference = WeakReference(context)
}

        override fun doInBackground(vararg params: String): GeoJsonLayer? {


            try {
                // Open a stream from the URL
                val stream = URL(params[0]).openStream()


                val result = StringBuilder()
                val reader = BufferedReader(InputStreamReader(stream) as Reader?)
                var line : String?
                do {
                    line = reader.readLine()
                    if (line == null)
                        break
                    result.append(line)
                } while (true)
                // Close the stream
                reader.close()
                stream.close()
                val activity = activityReference!!.get()
                if (activity != null)
                return GeoJsonLayer(activity.mMap, JSONObject(result.toString()))


            } catch (e: IOException) {
                Log.e("Yamamz", "GeoJSON file could not be read")
            } catch (e: JSONException) {
                Log.e("yamamz", "GeoJSON file could not be converted to a JSONObject")
            }

            return null
        }

        override fun onPostExecute(layer: GeoJsonLayer?) {
            if (layer != null) {
                val activity = activityReference!!.get()
                if (activity != null)
                activity.addGeoJsonLayerToMap(layer)
            }
        }

    }

    private fun addGeoJsonLayerToMap(layer: GeoJsonLayer) {

        addColorsToMarkers(layer)
        layer.addLayerToMap()
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(31.4118, -103.5355)))

    }



    private fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }



    override fun onDestroy() {
        super.onDestroy()
        realm!!.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            android.R.id.home -> {
                onBackPressed()
                return true

            }

            else -> return super.onOptionsItemSelected(item)

        }

    }

}
