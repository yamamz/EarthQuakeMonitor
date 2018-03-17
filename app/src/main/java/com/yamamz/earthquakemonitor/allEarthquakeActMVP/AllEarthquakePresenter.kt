package com.yamamz.earthquakemonitor.allEarthquakeActMVP

import com.google.maps.android.geojson.GeoJsonLayer
import com.yamamz.earthquakemonitor.R
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

/**
 * Created by AMRI on 11/20/2017.
 */
class AllEarthquakePresenter(val view:AllEarthquakeMVP.View):AllEarthquakeMVP.Presenter{

    override fun setMapResource(style_json: Int) {
        view.setMapStyle(style_json)
    }

    override fun setMapStyle() {
        interactor.getTimeOftheDay()
    }

    val interactor=AllEarthquakeInteractor(this)
}