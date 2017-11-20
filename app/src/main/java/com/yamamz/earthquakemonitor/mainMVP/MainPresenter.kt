package com.yamamz.earthquakemonitor.mainMVP

import com.yamamz.earthquakemonitor.MyApplication
import com.yamamz.earthquakemonitor.adapter.QuakeAdapter
import com.yamamz.earthquakemonitor.model.EarthQuake

/**
 * Created by AMRI on 11/6/2017.
 */
class MainPresenter(var view: MainMVP.View):MainMVP.Presenter{
    override fun initializeNotification() {
        view.initializeIntent()
    }

    override fun showDetails(north: Double?, east: Double?, time: Long?, mag: Double?, depth: Double?, location: String?) {
        view.showDetails(north,east,time,mag,depth,location)
    }

    override fun registerReciever() {
        view.registerReciever()
    }

    override fun stopLoading() {
        view.stopLoading()
    }

    override fun showLoading() {
        view.showLoading()
    }

    override fun startAllEarthquakeAct() {
        view.startAllEarthquakeAct()
    }

    override fun initializeRecyclerViewAndAdapter() {
        view.initializeRecyclerView()
    }

    override fun swipeOnRefresh() {
        view.swipeOnRefresh()
    }

    override fun showDialog() {
        view.showDialog()
    }

    override fun startNotification() {
      view.startNotification()
    }

    override fun start() {
      view.start()
    }

    override fun getPrefs() {
        view.getPrefs()
    }

    override fun goToDeatails(position: Int) {
        interactor.goToDeatails(position)
    }

    override fun checkinternetConnectivity(earthquake_category:String,time_category:String) {
        interactor.checkinternetConnectivity(earthquake_category,time_category)
    }

    override fun checkRecyclerViewIsemplty(mAdapter: QuakeAdapter?) {
       view.checkRecyclerViewIsemplty(mAdapter)
    }


    override fun checkAdapter(mAdapter: QuakeAdapter?) {
        interactor.checkAdapterSize(mAdapter)
    }

    override fun loadataFromRealm() {
     interactor.searchDataFromRealm()
    }

    override fun setRecyclerView(earthQuakes: ArrayList<EarthQuake>) {
        view.setEarthquakeOnList(earthQuakes)
       }

    val interactor=MainInteractor(this)

    override fun SetRecyclerViewDataOnClick(earthquake_category: String, time_category: String) {
    interactor.getEarthquakes(earthquake_category,time_category)
    }





}