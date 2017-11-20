package com.yamamz.earthquakemonitor.mainMVP

import com.yamamz.earthquakemonitor.MyApplication
import com.yamamz.earthquakemonitor.adapter.QuakeAdapter
import com.yamamz.earthquakemonitor.model.EarthQuake
import kotlin.collections.ArrayList

/**
 * Created by AMRI on 11/6/2017.
 */
class MainMVP {

    interface View{

       fun setEarthquakeOnList(earthquakeList: ArrayList<EarthQuake>)
        fun checkRecyclerViewIsemplty(mAdapter: QuakeAdapter?)
        fun goToDetailsMap(position: Int)
        fun getPrefs()
        fun start()
        fun startNotification()
        fun showDialog()
        fun isNetAvailable():Boolean
        fun swipeOnRefresh()
        fun initializeRecyclerView()
        fun initializeIntent()
        fun startAllEarthquakeAct()
        fun showLoading()
        fun stopLoading()
        fun registerReciever()
        fun showDetails(north: Double?, east: Double?, time: Long?, mag: Double?, depth: Double?, location: String?)
    }

    interface Presenter{

        fun SetRecyclerViewDataOnClick(earthquake_category: String, time_category: String)
        fun checkAdapter(mAdapter: QuakeAdapter?)
        fun loadataFromRealm()
        fun setRecyclerView(earthQuakes: ArrayList<EarthQuake>)
        fun checkRecyclerViewIsemplty(mAdapter: QuakeAdapter?)
        fun checkinternetConnectivity(earthquake_category:String,time_category:String)
        fun goToDeatails(position: Int)
        fun getPrefs()
        fun start()
        fun startNotification()
        fun showDialog()
        fun swipeOnRefresh()
        fun initializeRecyclerViewAndAdapter()
        fun startAllEarthquakeAct()
        fun showLoading()
        fun stopLoading()
        fun registerReciever()
        fun showDetails(north: Double?, east: Double?, time: Long?, mag: Double?, depth: Double?, location: String?)
        fun initializeNotification()


    }

    interface Interactor{
        fun getEarthquakes(earthquake_category: String, time_category: String)
        fun searchDataFromRealm()
        fun checkAdapterSize(mAdapter: QuakeAdapter?)
        fun checkinternetConnectivity(earthquake_category: String, time_category: String)
        fun goToDeatails(position: Int)
    }
}