package com.yamamz.earthquakemonitor.allEarthquakeActMVP

import com.google.maps.android.geojson.GeoJsonLayer

/**
 * Created by AMRI on 11/20/2017.
 */
class AllEarthquakeMVP{

    interface View{
        fun setMapStyle(style_json: Int)

    }
    interface Presenter{
        fun setMapStyle()
        fun setMapResource(style_json: Int)


    }
    interface Interactor{
        fun getTimeOftheDay()


    }
}