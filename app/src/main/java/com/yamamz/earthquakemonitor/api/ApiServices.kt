package com.yamamz.earthquakemonitor.api

import com.yamamz.earthquakemonitor.model.EarthquakeGeoJSon

import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by AMRI on 8/24/2017.
 */

interface ApiServices {
    //last day earthquake
    @get:GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    val earthQuakesLastdayAll: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/1.0_day.geojson")
    val earthQuakesLastday1_0: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/2.5_day.geojson")
    val earthQuakesLastday2_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/4.5_day.geojson")
    val earthQuakesLastday4_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/significant_day.geojson")
    val earthQuakesLastdaySig: Call<EarthquakeGeoJSon>


    //last hour earthquake
    @get:GET("earthquakes/feed/v1.0/summary/all_hour.geojson")
    val earthQuakesLasthourAll: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/1.0_hour.geojson")
    val earthQuakesLasthour1_0: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/2.5_hour.geojson")
    val earthQuakesLasthour2_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/4.5_hour.geojson")
    val earthQuakesLasthour4_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/significant_hour.geojson")
    val earthQuakesLasthourSig: Call<EarthquakeGeoJSon>


    //last hour earthquake
    @get:GET("earthquakes/feed/v1.0/summary/all_week.geojson")
    val earthQuakesLastweekAll: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/1.0_week.geojson")
    val earthQuakesLastweek1_0: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/2.5_week.geojson")
    val earthQuakesLastweek2_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/4.5_week.geojson")
    val earthQuakesLastweek4_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/significant_week.geojson")
    val earthQuakesLastweekSig: Call<EarthquakeGeoJSon>


    //last hour earthquake
    @get:GET("earthquakes/feed/v1.0/summary/all_month.geojson")
    val earthQuakesLastmonthAll: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/1.0_month.geojson")
    val earthQuakesLastmonth1_0: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/2.5_month.geojson")
    val earthQuakesLastmonth2_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/4.5_month.geojson")
    val earthQuakesLastmonth4_5: Call<EarthquakeGeoJSon>
    @get:GET("earthquakes/feed/v1.0/summary/significant_month.geojson")
    val earthQuakesLastmonthSig: Call<EarthquakeGeoJSon>


}
