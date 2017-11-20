package com.yamamz.earthquakemonitor.details_map_MVP

import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.Log
import com.yamamz.earthquakemonitor.R
import java.text.DecimalFormat
import java.util.*

/**
 * Created by AMRI on 11/9/2017.
 */
class DetailsInteractor(val presenter: DetailsMVP.Presenter):DetailsMVP.Interactor{
    override fun checkProvideEnable(providerEnabled: Boolean, hasGPS: Boolean, checkPermissions: Boolean, shouldProvideRationale: Boolean) {



        if (!providerEnabled && hasGPS) {
                presenter.enableLoc()
            //presenter.displayDistance()

        } else {

               // presenter.displayDistance(getPrefs(), extras?.getDouble("n").toString().toDouble(), getPrefs())
                presenter.reLocationUpdate()
            }


    }

    override fun hasGPS(hasGPS: Boolean) {
        if(!hasGPS){
            presenter.showMessage("Gps not Supported")
        }
    }

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    override fun RequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i("Yamamz", "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("Yamamz", "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.

                presenter.reLocationUpdate()

            } else {
                // Permission denied.
                // setButtonsState(false)
                presenter.showSnackBarIfDenied()


            }
        }

    }

    override fun requestPermission(shouldProvideRationale: Boolean) {
        if (shouldProvideRationale) {
            Log.i("YAMAMZ", "Displaying permission rationale to provide additional context.")
            presenter.showSnackBar()
        } else {
            Log.i("Yamamz", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
          presenter.requestPermission()
        }
    }

    override fun setTextViews(magnitude: Double,e:String,n:String) {
        val lat: String?
        val lon: String?
        val df = DecimalFormat("##.###")

        if (e.toDouble() < 0) lat = "${df.format(e.toDouble())} °S" else lat = "${df.format(e.toDouble())} °N"

        if (n.toDouble() < 0) lon = "${df.format(n.toDouble())} °W" else lon = "${df.format(n.toDouble())} °E"



        when(magnitude){

            in -1.0 .. 1.09 -> presenter.setBackgroundResources(R.drawable.circle)

            in 1.1 .. 2.49 ->  presenter.setBackgroundResources(R.drawable.circle_weak)

            in 2.5 .. 4.49 -> presenter.setBackgroundResources(R.drawable.circle_moderate)

            else ->  presenter.setBackgroundResources(R.drawable.very_strong_circle)

        }

        when (magnitude) {
            in -1.0..1.99 -> {

               val scale = "Did you feel it?  -Not Felt"
               val info= "Perceptible to people under favorable circumstances. " +
                        "\nDelicately balanced objects are disturbed slightly."
                presenter.setTextScaleAndInfo(scale,info,lat,lon)
            }


            in 2.0..3.99 -> {

                if (magnitude >= 3) {

                    val scale= "Did you feel it?  -Weak"
                    val info= "Felt by many people indoors especially in upper " +
                            "\nfloors of buildings. light truck." +
                            "\nDizziness and nausea are experienced by some people."
                    presenter.setTextScaleAndInfo(scale,info,lat,lon)

                } else {

                   val scale = "Did you feel it?  -Slightly Felt"
                   val info = "Felt by few individuals at rest indoors. " +
                            "\nHanging objects swing slightly." +
                            "\nStill Water in containers oscillates noticeably."

                    presenter.setTextScaleAndInfo(scale,info,lat,lon)

                }

            }

            in 4.0..5.99 -> {

                if (magnitude >= 5) {
                   val scale = "Did you feel it?  -Strong"
                    val info= "Felt generally by people indoors and by some people outdoors. " +
                            "\nLight sleepers are awakened. Vibration is felt like a passing" +
                            "\nof heavy truck."
                    presenter.setTextScaleAndInfo(scale,info,lat,lon)
                } else {
                    val scale = "Did you feel it?  -Moderate Strong"
                   val info = "Generally felt by most people indoors and outdoors. " +
                            "\nMany sleeping people are awakened. " +
                            "\nSome are frightened, some run outdoors. " +
                            "\nStrong shaking and rocking felt throughout building."
                    presenter.setTextScaleAndInfo(scale,info,lat,lon)

                }
            }
            in 6.0..7.99 -> {

                if (magnitude >= 7) {
                    val scale = "Did you feel it?  -Destructive"
                    val info = "Most people are frightened and run outdoors. " +
                            "\nPeople find it difficult to stand in upper floors." +
                            "\nBig church bells may ring. Old or poorly-built s" +
                            "\ntructures suffer considerably damage.  " +
                            "\nSome well-built structures are slightly damaged."
                    presenter.setTextScaleAndInfo(scale, info, lat, lon)

                } else {
                    val scale = "Did you feel it?  -Very Strong"
                    val info= "Many people are frightened; many run outdoors. " +
                            "\nSome people lose their balance. motorists feel like " +
                            "\ndriving in flat tires. Heavy objects or " +
                            "\nfurniture move or may be shifted."

                    presenter.setTextScaleAndInfo(scale,info,lat,lon)
                }
            }
            in 8.0..20.0 -> {

                if (magnitude >= 9) {
                    val scale = "Did you feel it?  -Devastating"
                  val info = "People are forcibly thrown to ground. " +
                            "\nMany cry and shake with fear. " +
                            "\nMost buildings are totally damaged. " +
                            "\nbridges and elevated concrete structures " +
                            "\nare toppled or destroyed."
                    presenter.setTextScaleAndInfo(scale,info,lat,lon)
                } else {
                 val scale = "Did you feel it?  -Very Destructive"
                  val info = "People panicky. People find" +
                            "\nit difficult to stand even outdoors." +
                            "\nMany well-built buildings are considerably damaged. " +
                            "\nConcrete dikes and foundation of bridges are" +
                            "\ndestroyed by ground settling or toppling." +
                            "\nRailway tracks are bent or broken"
                    presenter.setTextScaleAndInfo(scale,info,lat,lon)
                }
            }

        }

    }

    override fun getTimeofTheDay() {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val c = Calendar.getInstance()
            val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

            if (timeOfDay in 0..5) {

                presenter.setMaStyleResource(R.raw.style_json)

            } else if (timeOfDay in 6..17) {
                presenter.setMaStyleResource(R.raw.style_retro)

            } else if (timeOfDay in 18..23) {
                presenter.setMaStyleResource(R.raw.style_json)
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("yamamz", "Can't find style. Error: ", e)
        }

    }


}