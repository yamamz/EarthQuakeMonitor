package com.yamamz.earthquakemonitor.allEarthquakeActMVP

import android.content.res.Resources
import android.util.Log
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.geojson.GeoJsonLayer
import com.yamamz.earthquakemonitor.R
import java.util.*

/**
 * Created by AMRI on 11/20/2017.
 */
class AllEarthquakeInteractor(val presenter:AllEarthquakeMVP.Presenter):AllEarthquakeMVP.Interactor {


    override fun getTimeOftheDay() {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val c = Calendar.getInstance()
            val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

            when (timeOfDay) {
                in 0..5 -> presenter.setMapResource(R.raw.style_json)

                in 6..17 -> presenter.setMapResource(R.raw.style_retro)

                in 18..23 -> presenter.setMapResource(R.raw.style_json)
            }

        } catch (e: Resources.NotFoundException) {
            Log.e("yamamz", "Can't find style. Error: ", e)
        }
    }

}