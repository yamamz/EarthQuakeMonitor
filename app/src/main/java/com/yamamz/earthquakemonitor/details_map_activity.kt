package com.yamamz.earthquakemonitor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
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
import android.location.LocationManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*

import com.yamamz.earthquakemonitor.service.LocationUpdatesService
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay


class details_map_activity : AppCompatActivity(), OnMapReadyCallback {
    var mMap: GoogleMap? = null
    var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199
    var success: Boolean? = null
    var extras: Bundle? = null


    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null

    // Tracks the bound state of the service.
    private var mBound = false
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    @SuppressLint("SetTextI18n")


    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationUpdatesService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }


    override fun onMapReady(p0: GoogleMap?) {

        mMap = p0

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val c = Calendar.getInstance()
            val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

            if (timeOfDay in 0..5) {
                success = mMap?.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.style_json))

            } else if (timeOfDay in 6..17) {
                success = mMap?.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.style_retro))

            } else if (timeOfDay in 18..23) {
                success = mMap?.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.style_json))
            }

            if (success==false) {
                Log.e("Yamamz", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("yamamz", "Can't find style. Error: ", e)
        }
        val df = DecimalFormat("##.###")

        val e = extras?.getDouble("e").toString()
        val n = extras?.getDouble("n").toString()

        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        val loc = LatLng(e.toDouble(), n.toDouble())
        val mag = extras?.getDouble("mag")

        val px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
        val mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mDotMarkerBitmap)
        val shape = ContextCompat.getDrawable(this@details_map_activity, R.drawable.map_red_dot)
        shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
        shape.draw(canvas)

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.00, 0.00), 0f))

        async(UI) {
          animateMapFlyGotoLoc(loc,mDotMarkerBitmap)
        }


        val lat: String?
        val lon: String?

        if (e.toDouble() < 0) lat = "${df.format(intent.extras["e"])} °S" else lat = "${df.format(intent.extras["e"])} °N"

        if (n.toDouble() < 0) lon = "${df.format(intent.extras["n"])} °W" else lon = "${df.format(intent.extras["n"])} °E"

        tv_depth.text = "${extras?.getDouble("depth")} km"
        tv_time.text = convertTime(extras?.getLong("time").toString().toLong())
        tv_location.text = "$lat , $lon"
        tv_place.text = extras?.getString("location")
        tv_mag.text = "${extras?.getDouble("mag")}"


        val magnitude=mag.toString().toDouble()
        when(magnitude){

            in -1.0 .. 1.09 ->  tv_mag.setBackgroundResource(R.drawable.circle)

            in 1.1 .. 2.49 ->  tv_mag.setBackgroundResource(R.drawable.circle_weak)

            in 2.5 .. 4.49 ->  tv_mag.setBackgroundResource(R.drawable.circle_moderate)

            else ->  tv_mag.setBackgroundResource(R.drawable.very_strong_circle)

        }

        when (mag.toString().toDouble()) {
            in -1.0..1.99 -> {

                tv_scale.text = "Did you feel it?  -Not Felt"
                tv_info.text = "Perceptible to people under favorable circumstances. " +
                        "\nDelicately balanced objects are disturbed slightly."

            }


            in 2.0..3.99 -> {

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

                if (mag.toString().toDouble() >= 9) {
                    tv_scale.text = "Did you feel it?  -Devastating"
                    tv_info.text = "People are forcibly thrown to ground. " +
                            "\nMany cry and shake with fear. " +
                            "\nMost buildings are totally damaged. " +
                            "\nbridges and elevated concrete structures " +
                            "\nare toppled or destroyed."
                } else tv_scale.text = "Did you feel it?  -Very Destructive"
                tv_info.text = "People panicky. People find" +
                        "\nit difficult to stand even outdoors." +
                        "\nMany well-built buildings are considerably damaged. " +
                        "\nConcrete dikes and foundation of bridges are" +
                        "\ndestroyed by ground settling or toppling." +
                        "\nRailway tracks are bent or broken"
            }

        }


    }


    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("YAMAMZ", "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    findViewById(R.id.detail_map),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(this@details_map_activity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    }
                    .show()
        } else {
            Log.i("Yamamz", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@details_map_activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("Yamamz", "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("Yamamz", "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService?.requestLocationUpdates()
            } else {
                // Permission denied.
               // setButtonsState(false)
                Snackbar.make(
                        findViewById(R.id.detail_map),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
            }
        }


    }

    private val myReciever =object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            if (location != null) {
                Toast.makeText(this@details_map_activity, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show()


                val geDistance: Double? = distance(extras?.getDouble("e").toString().toDouble(),
                        extras?.getDouble("n").toString().toDouble(), location?.latitude?:0.0, location?.longitude?:0.0)
                val df = DecimalFormat("##.##")
                tv_distance1.text = "${df.format(geDistance)} km"

                val pref = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = pref.edit()
                editor.putString("location", tv_distance1.text.toString())
                editor.apply()
                Log.e("yamamz", "Save Successfully")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun animateMapFlyGotoLoc(loc: LatLng, mDotMarkerBitmap: Bitmap) {

        delay(1500)

        val position = CameraPosition.Builder()
                .target(loc) // Sets the new camera position
                .zoom(10f) // Sets the zoom
                .bearing(0f) // Rotate the camera
                .tilt(30f)// Set the camera tilt
                .build()// Creates a CameraPosition from the builder
        mMap?.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000, null)

        mMap?.addMarker(MarkerOptions().anchor(.5f, .5f).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).position(loc).title(intent.extras["location"].toString()))
        delay(3000)
        val manager = this@details_map_activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!hasGPSDevice(this@details_map_activity)) {
            Toast.makeText(this@details_map_activity, "Gps not Supported", Toast.LENGTH_SHORT).show()
        }
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val locationFromprefs = sharedPrefs.getString("location", "")

        tv_distance1.text = locationFromprefs

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(this@details_map_activity)) {

            if (!checkPermissions()) {
                requestPermissions()
            } else {
                enableLoc()
                if (locationFromprefs == "")
                    tv_distance1.text = "Gps not enable"
            }

        } else {

            if (!checkPermissions()) {
                requestPermissions()
            } else {
                tv_distance1.text = locationFromprefs
                mService?.requestLocationUpdates()
            }

           


        }

    }

    override public fun onNewIntent(intent: Intent) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        }
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

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions()
            }
        }

        //startService(Intent(this@details_map_activity, LocationService::class.java))



    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val location = intent?.getParcelableExtra<Location>("location")

            val geDistance: Double? = distance(extras?.getDouble("e").toString().toDouble(),
                    extras?.getDouble("n").toString().toDouble(), location?.latitude?:0.0, location?.longitude?:0.0)
            val df = DecimalFormat("##.##")
            tv_distance1.text = "${df.format(geDistance)} km"

            val pref = PreferenceManager.getDefaultSharedPreferences(contxt)
            val editor = pref.edit()
            editor.putString("location", tv_distance1.text.toString())
            editor.apply()
            Log.e("yamamz", "Save Successfully")
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

    override fun onStart() {
        super.onStart()

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.

        bindService(Intent(this, LocationUpdatesService::class.java), mServiceConnection,
                Context.BIND_AUTO_CREATE)

    }



    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(myReciever,
                IntentFilter(LocationUpdatesService.ACTION_BROADCAST))

//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                mMessageReceiver, IntentFilter("getlocation"))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReciever)
        super.onPause()
    }

    override fun onStop() {

        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
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
                            googleApiClient?.connect()
                        }
                    })
                    .addOnConnectionFailedListener { connectionResult -> Log.d("Location error", "Location error " + connectionResult.errorCode) }.build()
            googleApiClient?.connect()

            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            locationRequest.interval = (30 * 1000).toLong()
            locationRequest.fastestInterval = (5 * 1000).toLong()
            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)


            val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
            result.addOnCompleteListener {
                try {
                    val response = result.getResult(ApiException::class.java)
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (exception:ApiException) {
                    when(exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                val resolvable =  exception as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        this@details_map_activity,
                                        REQUEST_LOCATION)
                            } catch (e: IntentSender.SendIntentException) {
                                // Ignore the error.
                            } catch (e:ClassCastException ) {
                                // Ignore, should be an impossible error.
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->{

                        }
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    }
                }
            }

            mService?.requestLocationUpdates()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val states = LocationSettingsStates.fromIntent(intent)
        when(requestCode) {
            REQUEST_LOCATION ->{
            when (resultCode) {
                Activity.RESULT_OK -> {

            }

                Activity.RESULT_CANCELED ->{


                }
            }
                // The user was asked to change settings, but chose not to

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
