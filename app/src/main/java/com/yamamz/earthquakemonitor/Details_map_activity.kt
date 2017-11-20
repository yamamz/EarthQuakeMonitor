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
import android.view.Window
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.yamamz.earthquakemonitor.details_map_MVP.DetailsMVP
import com.yamamz.earthquakemonitor.details_map_MVP.DetailsPresenter

import com.yamamz.earthquakemonitor.service.LocationUpdatesService
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay


class Details_map_activity : AppCompatActivity(), OnMapReadyCallback, DetailsMVP.View {

    @SuppressLint("SetTextI18n")
    override fun setTextDistance(distance: Double) {



        val df = DecimalFormat("##.##")
        tv_distance1.text = "${df.format(distance)} km"
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPrefs(): String {

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val locationFromprefs = sharedPrefs.getString("location", "0,0")
        return  locationFromprefs
    }

    override fun showSnackBarIfdenied() {
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

    override fun startLocationUpdate() {
        mService?.requestLocationUpdates()
    }

    override fun showSnackBar() {
        Snackbar.make(
                findViewById(R.id.detail_map),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(this@Details_map_activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_PERMISSIONS_REQUEST_CODE)
                }
                .show()
    }

    override fun showMessage(s: String) {
        Toast.makeText(this@Details_map_activity, s, Toast.LENGTH_SHORT).show()
    }

    override fun hasGPS(): Boolean {
        val mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mgr.allProviders ?: return false
        return providers.contains(LocationManager.GPS_PROVIDER)
    }

    override fun addMarker(mDotMarkerBitmap: Bitmap, loc: LatLng) {
        mMap?.addMarker(MarkerOptions().anchor(.5f, .5f).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).position(loc).title(intent.extras["location"].toString()))
    }

    lateinit var mDotMarkerBitmap: Bitmap
    override fun animateFlyIntheMap(loc: LatLng) {

        val position = CameraPosition.Builder()
                .target(loc) // Sets the new camera position
                .zoom(10f) // Sets the zoom
                .bearing(0f) // Rotate the camera
                .tilt(30f)// Set the camera tilt
                .build()// Creates a CameraPosition from the builder
        mMap?.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000, null)

    }

    override fun drawShapeOnCanvas() {
        val px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
        mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mDotMarkerBitmap)
        val shape = ContextCompat.getDrawable(this@Details_map_activity, R.drawable.map_red_dot)
        shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
        shape.draw(canvas)
    }

    @SuppressLint("SetTextI18n")
    override fun setTextViews(scale: String, info: String, lat: String, lon: String) {
        tv_depth.text = "${extras?.getDouble("depth")} km"
        tv_time.text = convertTime(extras?.getLong("time").toString().toLong())
        tv_location.text = "$lat , $lon"
        tv_place.text = extras?.getString("location")
        tv_mag.text = "${extras?.getDouble("mag")}"
        tv_info.text = info
        tv_scale.text = scale

    }

    override fun setMagBackgroundResource(drawable: Int) {
        tv_mag.setBackgroundResource(drawable)
    }

    override fun setMapStyle(mapStyle: Int?) {
        success = mMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, mapStyle ?: 0))
    }


    var mMap: GoogleMap? = null
    var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199
    var success: Boolean? = null
    var extras: Bundle? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null

    // Tracks the bound state of the service.
    private var mBound = false

    private val presenter = DetailsPresenter(this)

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


    @SuppressLint("SetTextI18n")
    override fun onMapReady(p0: GoogleMap?) {

        mMap = p0

        presenter.setMapStyle()
        val e = extras?.getDouble("e").toString()
        val n = extras?.getDouble("n").toString()
        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        val loc = LatLng(e.toDouble(), n.toDouble())
        val mag = extras?.getDouble("mag")

        presenter.drawShapeOnCanvas()
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.00, 0.00), 0f))


        // Check that the user hasn't revoked permissions by going to Settings.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (!checkPermissions()) {
                Log.e("Yamamz","permission check")
                presenter.requestPermissions()
            }
            else {

                async(UI) {

                    delay(1500)
                    presenter.animateMapFlyGotoLoc(loc)
                    presenter.addMarker(mDotMarkerBitmap, loc)
                    delay(3000)
                    presenter.hasGPSDevice(hasGPS())
                    presenter.displayDistance(extras?.getDouble("e").toString().toDouble(),
                            extras?.getDouble("n").toString().toDouble(),getPrefs())
                    val manager = this@Details_map_activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    presenter.checkIsProviderEnable(manager.isProviderEnabled(LocationManager.GPS_PROVIDER), hasGPS(), checkPermissions(), shouldProvideRationale())
                }
            }

        }

        else{
            Log.e("Yamamz","Test1")
            async(UI) {

                delay(1500)
                presenter.animateMapFlyGotoLoc(loc)
                presenter.addMarker(mDotMarkerBitmap, loc)
                delay(3000)

                presenter.hasGPSDevice(hasGPS())

                presenter.displayDistance(extras?.getDouble("e").toString().toDouble(),
                        extras?.getDouble("n").toString().toDouble(),getPrefs())
                val manager = this@Details_map_activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                presenter.checkIsProviderEnable(manager.isProviderEnabled(LocationManager.GPS_PROVIDER), hasGPS(), checkPermissions(), shouldProvideRationale())
            }
        }


        val magnitude = mag.toString().toDouble()
        presenter.setTextViews(magnitude, e, n)


    }

    override fun shouldProvideRationale(): Boolean {
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        return shouldProvideRationale
}
    override fun requestPermissions() {

        ActivityCompat.requestPermissions(this@Details_map_activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
    }

    override fun checkPermissions(): Boolean {

        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        presenter.RequestPermissionResult(requestCode,permissions,grantResults)
    }

    private val myReciever = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            if (location != null) {

                val geDistance: Double? = distance(extras?.getDouble("e").toString().toDouble(),
                        extras?.getDouble("n").toString().toDouble(), location.latitude, location.longitude)
                val df = DecimalFormat("##.##")
                tv_distance1.text = "${df.format(geDistance)} km"

                val pref = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = pref.edit()
                editor.putString("location", "${location.latitude},${location.longitude}")
                editor.apply()
                Log.e("Yamamz", getPrefs())
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



        onNewIntent(intent)
        this.setFinishOnTouchOutside(true)

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

    override fun enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this@Details_map_activity)
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

                } catch (exception: ApiException) {
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                val resolvable = exception as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        this@Details_map_activity,
                                        REQUEST_LOCATION)
                            } catch (e: IntentSender.SendIntentException) {
                                // Ignore the error.
                            } catch (e: ClassCastException) {
                                // Ignore, should be an impossible error.
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

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
        when (requestCode) {
            REQUEST_LOCATION -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {

                    }

                    Activity.RESULT_CANCELED -> {


                    }
                }

                // The user was asked to change settings, but chose not to

            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        android.R.id.home -> {
            onBackPressed()
            true

        }

        else -> super.onOptionsItemSelected(item)

    }


}
