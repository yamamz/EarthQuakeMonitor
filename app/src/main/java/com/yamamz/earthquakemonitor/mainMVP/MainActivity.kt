package com.yamamz.earthquakemonitor.mainMVP

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat

import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import android.widget.Toast
import com.yamamz.earthquakemonitor.AllEarthquakeActivity
import com.yamamz.earthquakemonitor.BuildConfig

import com.yamamz.earthquakemonitor.adapter.QuakeAdapter
import com.yamamz.earthquakemonitor.model.*
import com.yamamz.earthquakemonitor.R
import com.yamamz.earthquakemonitor.details_map_MVP.Details_map_activity

import com.yamamz.earthquakemonitor.service.DataFetchReciever
import com.yamamz.earthquakemonitor.service.NotificationReceiver

import com.yamamz.earthquakemonitor.ui.DeviderItemDecoration
import com.yamamz.earthquakemonitor.view.Settings

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), MainMVP.View {

    var time_category: String? = null
    var earthquake_category: String? = null
    var earthQuakelist = ArrayList<EarthQuake>()
    var pendingIntent: PendingIntent? = null
    var pendingNotificationIntent: PendingIntent? = null
    var mAdapter: QuakeAdapter? = null
    val presenter: MainPresenter = MainPresenter(this)
    var alarmIntent: Intent? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34


    override fun setEarthquakeOnList(earthquakeList: ArrayList<EarthQuake>) {
        earthQuakelist.clear()
        Log.e("YamamzMVP", earthquakeList.size.toString())
        earthQuakelist.addAll(earthquakeList)
        mAdapter?.notifyDataSetChanged()
        pbLoading.visibility = View.GONE
        swipeContainer.isRefreshing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (!checkPermissions()) {
            requestPermissions()
        }

        presenter.getPrefs()
        presenter.loadataFromRealm()
        presenter.checkinternetConnectivity(earthquake_category ?: "", time_category ?: "")
        presenter.initializeNotification()
        presenter.start()
        presenter.startNotification()
        presenter.swipeOnRefresh()
        presenter.initializeRecyclerViewAndAdapter()
        presenter.registerReciever()

        fab.setOnClickListener {
            presenter.startAllEarthquakeAct()
        }


    }




    private val mMessageReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(contxt: Context?, intent: Intent?) {
            try {
                if (intent?.action != null && intent.action == "updaterecyclerView") {
                    presenter.loadataFromRealm()
                    Log.e("Yamamz", "successfully loaded")
                }
            } catch (e: Exception) {

            }
        }
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
                val intent = Intent(this@MainActivity, Settings::class.java)
                startActivity(intent)
                true

            }
            R.id.action_about -> {
                presenter.showDialog()
                return true
            }
            R.id.action_search -> {
                if (isNetAvailable()) {
                    presenter.getPrefs()
                    presenter.showLoading()
                    presenter.SetRecyclerViewDataOnClick(earthquake_category ?: "", time_category ?: "")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun initializeRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_earthquake)
        mAdapter = QuakeAdapter(this, earthQuakelist)
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(DeviderItemDecoration(applicationContext, LinearLayoutManager.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter
        presenter.checkAdapter(mAdapter)
    }

    override fun swipeOnRefresh() {

        swipeContainer.setOnRefreshListener {
            if (isNetAvailable()) {
                presenter.getPrefs()
                presenter.SetRecyclerViewDataOnClick(earthquake_category ?: "", time_category ?: "")
                // Configure the refreshing colors
                swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light)
            } else {
                presenter.stopLoading()
                toast("No network is available")
            }
        }

    }

    override fun isNetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected

    }

    override fun registerReciever() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, IntentFilter("updaterecyclerView"))
        Log.e("Yamamz", "registerReciever")

    }


    override fun showLoading() {
        pbLoading.visibility = View.VISIBLE
    }

    override fun stopLoading() {
        swipeContainer.isRefreshing = false
    }


    override fun showDialog() {
        val builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val positiveText = getString(android.R.string.ok)
        builder.setTitle(getString(R.string.dialog_title))
        builder.setMessage(getString(R.string.dialog_message1))
        builder.setPositiveButton(positiveText) { _, _ ->
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun startNotification() {
        val manager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval: Long = 60000
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingNotificationIntent)
        Log.e("Yamamz", "startNotification")
    }

    override fun startAllEarthquakeAct() {
        val intent = Intent(this, AllEarthquakeActivity::class.java)
        startActivity(intent)
    }

    override fun start() {
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval: Long = 60000
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent)
        Log.e("Yamamz", "start")

    }

    override fun getPrefs() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        earthquake_category = sharedPrefs.getString("earthquake_category", "all")
        time_category = sharedPrefs.getString("time_category", "day")
        Log.e("Yamamz", "getPrefs")
    }

    override fun showDetails(north: Double?, east: Double?, time: Long?, mag: Double?, depth: Double?, location: String?) {
        val intent = Intent(this, Details_map_activity::class.java)
        intent.putExtra("n", north)
        intent.putExtra("e", east)
        intent.putExtra("time", time)
        intent.putExtra("mag", mag)
        intent.putExtra("depth", depth)
        intent.putExtra("location", location)
        this.startActivity(intent)
    }


    override fun checkRecyclerViewIsemplty(mAdapter: QuakeAdapter?) {
        if (mAdapter?.itemCount == 0) {
            empty.visibility = View.VISIBLE
        } else {
            empty.visibility = View.GONE
        }
    }


    override fun initializeIntent() {
        alarmIntent = Intent(applicationContext, DataFetchReciever::class.java)
        pendingIntent = PendingIntent.getBroadcast(MainActivity@ this, 1, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationIntent = Intent(MainActivity@ this, NotificationReceiver::class.java)
        pendingNotificationIntent = PendingIntent.getBroadcast(MainActivity@ this, 2, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        Log.e("Yamamz", "initializeIntent")
    }


    override fun goToDetailsMap(position: Int) {
        presenter.goToDeatails(position)
    }


    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("YAMAMZ", "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    findViewById(R.id.coordinator),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    }
                    .show()
        } else {
            Log.i("Yamamz", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity,
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
                //mService?.requestLocationUpdates()
            } else {
                // Permission denied.
                // setButtonsState(false)
                Snackbar.make(
                        findViewById(R.id.coordinator),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
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
}





