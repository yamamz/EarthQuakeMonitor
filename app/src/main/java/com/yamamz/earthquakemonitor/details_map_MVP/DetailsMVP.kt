package com.yamamz.earthquakemonitor.details_map_MVP

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng

/**
 * Created by AMRI on 11/9/2017.
 */
class DetailsMVP{

    interface View{
        fun setTextViews(scale:String,info:String,lat:String,lon:String)
        fun setMapStyle(mapStyle: Int?)
        fun setMagBackgroundResource(drawable:Int)
        fun drawShapeOnCanvas()
        fun animateFlyIntheMap(loc: LatLng)
        fun addMarker(mDotMarkerBitmap: Bitmap,loc:LatLng)
        fun hasGPS():Boolean
        fun showMessage(s: String)
        fun checkPermissions(): Boolean
        fun requestPermissions()
        fun shouldProvideRationale():Boolean
        fun showSnackBar()
        fun startLocationUpdate()
        fun showSnackBarIfdenied()
        fun getPrefs():String
        fun setTextDistance(distance: Double)
        fun enableLoc()
    }
    interface Presenter{
        fun setMapStyle()
        fun setMaStyleResource(mapStyle:Int)
        fun setTextViews(magnitude: Double,e:String,n:String)
        fun setBackgroundResources(circle: Int)
        fun setTextScaleAndInfo(scale: String, info: String,lat:String,lon:String)
        fun drawShapeOnCanvas()
        fun animateMapFlyGotoLoc(loc: LatLng): Any
        fun addMarker(mDotMarkerBitmap: Bitmap, loc: LatLng)
        fun hasGPS()
        fun showMessage(s: String)
        fun requestPermissions()
        fun showSnackBar()
        fun requestPermission()
        fun RequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
        fun reLocationUpdate()
        fun showSnackBarIfDenied()
        fun displayDistance(lat: Double, lon: Double, prefs: String)
        fun hasGPSDevice(hasGPS: Boolean)
        fun checkIsProviderEnable(providerEnabled: Boolean, hasGPS: Boolean, checkPermissions: Boolean, shouldProvideRationale: Boolean)
        fun enableLoc()

    }

    interface Interactor{
        fun getTimeofTheDay()
        fun setTextViews(magnitude: Double,e:String,n:String)
        fun requestPermission(shouldProvideRationale: Boolean)
        fun RequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
        fun hasGPS(hasGPS: Boolean)
        fun checkProvideEnable(providerEnabled: Boolean, hasGPS: Boolean, checkPermissions: Boolean, shouldProvideRationale: Boolean)

    }
}