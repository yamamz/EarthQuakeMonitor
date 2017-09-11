package com.yamamz.earthquakemonitor.api;

import com.yamamz.earthquakemonitor.model.EarthquakeGeoJSon;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by AMRI on 8/24/2017.
 */

public interface ApiServices {
    //last day earthquake
    @GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastdayAll();
    @GET("earthquakes/feed/v1.0/summary/1.0_day.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastday1_0();
    @GET("earthquakes/feed/v1.0/summary/2.5_day.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastday2_5();
    @GET("earthquakes/feed/v1.0/summary/4.5_day.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastday4_5();
    @GET("earthquakes/feed/v1.0/summary/significant_day.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastdaySig();


    //last hour earthquake
    @GET("earthquakes/feed/v1.0/summary/all_hour.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLasthourAll();
    @GET("earthquakes/feed/v1.0/summary/1.0_hour.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLasthour1_0();
    @GET("earthquakes/feed/v1.0/summary/2.5_hour.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLasthour2_5();
    @GET("earthquakes/feed/v1.0/summary/4.5_hour.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLasthour4_5();
    @GET("earthquakes/feed/v1.0/summary/significant_hour.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLasthourSig();


    //last hour earthquake
    @GET("earthquakes/feed/v1.0/summary/all_week.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastweekAll();
    @GET("earthquakes/feed/v1.0/summary/1.0_week.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastweek1_0();
    @GET("earthquakes/feed/v1.0/summary/2.5_week.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastweek2_5();
    @GET("earthquakes/feed/v1.0/summary/4.5_week.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastweek4_5();
    @GET("earthquakes/feed/v1.0/summary/significant_week.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastweekSig();


    //last hour earthquake
    @GET("earthquakes/feed/v1.0/summary/all_month.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastmonthAll();
    @GET("earthquakes/feed/v1.0/summary/1.0_month.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastmonth1_0();
    @GET("earthquakes/feed/v1.0/summary/2.5_month.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastmonth2_5();
    @GET("earthquakes/feed/v1.0/summary/4.5_month.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastmonth4_5();
    @GET("earthquakes/feed/v1.0/summary/significant_month.geojson")
    Call<EarthquakeGeoJSon> getEarthQuakesLastmonthSig();






}
