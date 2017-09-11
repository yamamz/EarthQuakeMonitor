package com.yamamz.earthquakemonitor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.android.synthetic.main.activity_details_map_activity2.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationRequest
import android.location.LocationManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.MenuItem
import com.google.android.gms.maps.model.*
import com.yamamz.earthquakemonitor.service.MyService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class details_map_activity : AppCompatActivity(), OnMapReadyCallback {
    var mMap: GoogleMap? = null
    var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199
    var success:Boolean?=null
    var extras:Bundle?=null
    @SuppressLint("SetTextI18n")
    override fun onMapReady(p0: GoogleMap?) {

        mMap = p0

          try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
              val c = Calendar.getInstance()
              val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

              if (timeOfDay in 0..5) {
                  success = mMap!!.setMapStyle(
                          MapStyleOptions.loadRawResourceStyle(
                                  this, R.raw.style_json))

              } else if (timeOfDay in 6..17) {
                  success= mMap!!.setMapStyle(
                          MapStyleOptions.loadRawResourceStyle(
                                  this, R.raw.style_retro))

              } else if (timeOfDay in 18..23) {
                  success= mMap!!.setMapStyle(
                          MapStyleOptions.loadRawResourceStyle(
                                  this, R.raw.style_json))
              }


            if (!success!!) {
                Log.e("Yamamz", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("yamamz", "Can't find style. Error: ", e);
        }
        val df = DecimalFormat("##.###")

        val e = extras!!.getDouble("e").toString()
        val n = extras!!.getDouble("n").toString()

        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        val loc = LatLng(e.toDouble(), n.toDouble())
        val mag = extras!!.getDouble("mag")

        val px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
val mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
val canvas = Canvas(mDotMarkerBitmap)
val shape = ContextCompat.getDrawable(this@details_map_activity,R.drawable.map_red_dot)
shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
        shape.draw(canvas)

        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.503186, -0.126446), 0f))

        val scheduler = Executors.newScheduledThreadPool(1)

scheduler.schedule({
val  handler = Handler(Looper.getMainLooper())
handler.post({
    val position = CameraPosition.Builder()
            .target(loc) // Sets the new camera position
            .zoom(4f) // Sets the zoom
            .bearing(0f) // Rotate the camera
            .tilt(30f)// Set the camera tilt
            .build()// Creates a CameraPosition from the builder
    mMap!!.animateCamera(CameraUpdateFactory
            .newCameraPosition(position),2000,null)

    mMap?.addMarker(MarkerOptions().anchor(.5f, .5f).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).position(loc).title(intent.extras["location"].toString()))
    })}, 1, TimeUnit.SECONDS)
        scheduler.shutdown()



        val lat: String?
        val lon: String?

        if (e.toDouble() < 0) lat = "${df.format(intent.extras["e"])} °S" else lat = "${df.format(intent.extras["e"])} °N"

        if (n.toDouble() < 0) lon = "${df.format(intent.extras["n"])} °W" else lon = "${df.format(intent.extras["n"])} °E"

        tv_depth.text = "${extras!!.getDouble("depth")} km"
        tv_time.text = convertTime(extras!!.getLong("time").toString().toLong())
        tv_location.text = "$lat , $lon"
        tv_place.text= extras!!.getString("location")
        tv_mag.text = "${extras!!.getDouble("mag")}"

        when (mag.toString().toDouble()) {
            in -1.0..1.99 -> {
                tv_mag.setBackgroundResource(R.drawable.circle)
                tv_scale.text = "Did you feel it?  -Not Felt"
                tv_info.text = "Perceptible to people under favorable circumstances. " +
                        "\nDelicately balanced objects are disturbed slightly."

            }


            in 2.0..3.99 -> {
                tv_mag.setBackgroundResource(R.drawable.circle_weak)
                if (mag.toString().toDouble() >= 3) {
                    tv_scale.text = "Did you feel it?  -Weak"
                    tv_info.text = "Felt by many people indoors especially in upper " +
                            "\nfloors of buildings. light truck." +
                            "\nDizziness and nausea are experienced by some people."
                } else {
                    tv_scale.text = "Did you feel it?  -Slightly Felt"
                    tv_info.text = "Felt by few individuals at rest indoors. " +
                            "\nHanging objects swing slightly." +
                            "\nStill Water in containers oscillates noticeably."

                }

            }

            in 4.0..5.99 -> {
                tv_mag.setBackgroundResource(R.drawable.circle_moderate)

                if (mag.toString().toDouble() >= 5) {
                    tv_scale.text = "Did you feel it?  -Strong"
                    tv_info.text = "Felt generally by people indoors and by some people outdoors. " +
                            "\nLight sleepers are awakened. Vibration is felt like a passing" +
                            "\nof heavy truck."
                } else tv_scale.text = "Did you feel it?  -Moderate Strong"
                tv_info.text = "Generally felt by most people indoors and outdoors. " +
                        "\nMany sleeping people are awakened. " +
                        "\nSome are frightened, some run outdoors. " +
                        "\nStrong shaking and rocking felt throughout building."
            }
            in 6.0..7.99 -> {
                tv_mag.setBackgroundResource(R.drawable.very_strong_circle)
                if (mag.toString().toDouble() >= 7) {
                    tv_scale.text = "Did you feel it?  -Destructive"
                    tv_info.text = "Most people are frightened and run outdoors. " +
                            "\nPeople find it difficult to stand in upper floors." +
                            "\nBig church bells may ring. Old or poorly-built s" +
                            "\ntructures suffer considerably damage.  " +
                            "\nSome well-built structures are slightly damaged."

                } else tv_scale.text = "Did you feel it?  -Very Strong"
                tv_info.text = "Many people are frightened; many run outdoors. " +
                        "\nSome people lose their balance. motorists feel like " +
                        "\ndriving in flat tires. Heavy objects or " +
                        "\nfurniture move or may be shifted."
            }

            in 8.0..20.0 -> {
                tv_mag.setBackgroundResource(R.drawable.violent_circle)
                if (mag.toString().toDouble() >= 9){ tv_scale.text = "Did you feel it?  -Devastating"
                     tv_info.text="People are forcibly thrown to ground. " +
                             "\nMany cry and shake with fear. " +
                             "\nMost buildings are totally damaged. " +
                             "\nbridges and elevated concrete structures " +
                             "\nare toppled or destroyed."
                }

                else tv_scale.text = "Did you feel it?  -Very Destructive"
                tv_info.text="People panicky. People find" +
                        "\nit difficult to stand even outdoors." +
                        "\nMany well-built buildings are considerably damaged. " +
                        "\nConcrete dikes and foundation of bridges are" +
                        "\ndestroyed by ground settling or toppling." +
                        "\nRailway tracks are bent or broken"
            }

        }


    }
    override public fun onNewIntent(intent:Intent ) {
      extras = intent.extras
        if (extras != null) {

        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun convertTime(time: Long): String {
        val date = Date(time)
        val dateFormat = SimpleDateFormat("EEE, MMM d, ''yy h:mm a")
        return dateFormat.format(date)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_map_activity2)


        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //startingintent = intent

        onNewIntent(intent)
        this.setFinishOnTouchOutside(true)


        val manager = this@details_map_activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!hasGPSDevice(this@details_map_activity)) {
            Toast.makeText(this@details_map_activity, "Gps not Supported", Toast.LENGTH_SHORT).show()
        }
        startService(Intent(this@details_map_activity, MyService::class.java))

        tv_distance1.setOnClickListener {
            enableLoc()
        }
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val locationFromprefs = sharedPrefs.getString("location", "")

           tv_distance1.text=locationFromprefs

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(this@details_map_activity)) {
            if(locationFromprefs=="")
            tv_distance1.text = "Gps not enable click here"
        }
        else{
            tv_distance1.text=locationFromprefs
        }


    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val location = intent!!.getParcelableExtra<Location>("location")

            val geDistance: Double? = distance(extras!!.getDouble("e").toString().toDouble(), extras!!.getDouble("n").toString().toDouble(), location.latitude, location.longitude)
            val df = DecimalFormat("##.##")
            tv_distance1.text = "${df.format(geDistance)} km"

            val pref=PreferenceManager.getDefaultSharedPreferences(contxt)
            val editor = pref.edit()
            editor.putString("location",tv_distance1.text.toString())
            editor.apply()
            Log.e("yamamz","Save Successfully")
        }
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

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, IntentFilter("getlocation"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    private fun hasGPSDevice(context: Context): Boolean {
        val mgr = context
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mgr.allProviders ?: return false
        return providers.contains(LocationManager.GPS_PROVIDER)
    }

    private fun enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this@details_map_activity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(bundle: Bundle?) {

                        }

                        override fun onConnectionSuspended(i: Int) {
                            googleApiClient!!.connect()
                        }
                    })
                    .addOnConnectionFailedListener { connectionResult -> Log.d("Location error", "Location error " + connectionResult.errorCode) }.build()
            googleApiClient!!.connect()

            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = (30 * 1000).toLong()
            locationRequest.fastestInterval = (5 * 1000).toLong()
            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)

            val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback {
                val status = it.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        tv_distance1.text = getString(R.string.getting)
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(this@details_map_activity, REQUEST_LOCATION)
                    } catch (e: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }

                }
            }
        }
    }

    fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
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
