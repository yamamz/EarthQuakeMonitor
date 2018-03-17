package com.yamamz.earthquakemonitor.details_map_MVP

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.model.LatLng

/**
 * Created by AMRI on 11/9/2017.
 */
class DetailsPresenter(var view: DetailsMVP.View):DetailsMVP.Presenter{
    override fun enableLoc() {
        view.enableLoc()
    }

    override fun checkIsProviderEnable(providerEnabled: Boolean, hasGPS: Boolean) {
        interactor.checkProvideEnable(providerEnabled,hasGPS)
    }

    override fun hasGPSDevice(hasGPS: Boolean) {
        interactor.hasGPS(hasGPS)
    }

    override fun displayDistance(lat: Double, lon: Double, prefs: String, prefsLon: String) {


        val location= prefs.split(",").toList()
        Log.e("Yamamz", location.size.toString())
        val distance=distance(prefs.toDouble(), prefsLon.toDouble(),lat,lon)

        view.setTextDistance(distance)
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



    override fun reLocationUpdate() {
        view.startLocationUpdate()
    }




    override fun showSnackBar() {
        view.showSnackBar()
    }



    override fun showMessage(s: String) {
        view.showMessage(s)
    }

    override fun hasGPS() {
        view.hasGPS()
    }

    override fun addMarker(mDotMarkerBitmap: Bitmap, loc: LatLng) {
        view.addMarker(mDotMarkerBitmap,loc)
    }

    override fun animateMapFlyGotoLoc(loc: LatLng) {
        view.animateFlyIntheMap(loc)
    }

    override fun drawShapeOnCanvas() {
        view.drawShapeOnCanvas()
    }

    override fun setTextScaleAndInfo(scale: String, info: String,lat:String,lon:String) {
        view.setTextViews(scale,info,lat,lon)
    }

    override fun setBackgroundResources(circle: Int) {
        view.setMagBackgroundResource(circle)
    }

    override fun setTextViews(magnitude: Double,e:String,n:String) {
        interactor.setTextViews(magnitude,e,n)
    }

    override fun setMaStyleResource(mapStyle: Int) {
        view.setMapStyle(mapStyle)
    }

    val interactor=DetailsInteractor(this)
    override fun setMapStyle() {
        interactor.getTimeofTheDay()
    }


}