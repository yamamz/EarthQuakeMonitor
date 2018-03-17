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
        fun showSnackBar()
        fun startLocationUpdate()

        fun getPrefsLat():String
        fun getPrefsLon():String
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
        fun showSnackBar()

        fun reLocationUpdate()

        fun displayDistance(lat: Double, lon: Double, prefs: String, prefsLon: String)
        fun hasGPSDevice(hasGPS: Boolean)
        fun checkIsProviderEnable(providerEnabled: Boolean, hasGPS: Boolean)
        fun enableLoc()

    }

    interface Interactor{
        fun getTimeofTheDay()
        fun setTextViews(magnitude: Double,e:String,n:String)
          fun hasGPS(hasGPS: Boolean)
        fun checkProvideEnable(providerEnabled: Boolean, hasGPS: Boolean)

    }
}