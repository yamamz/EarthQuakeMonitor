package com.yamamz.earthquakemonitor.model

import io.realm.RealmObject

/**
 * Created by AMRI on 8/28/2017.
 */

open class EarthquakeRealmModel : RealmObject {

    var location: String?=null
    var  mag: Double?=null
    var  lat: Double?=null
    var  lon: Double?=null
    var  dateOccur: Long?=null
    var  dept: Double?=null
    var  felt: Int? = null
    var  cdi: Int? = null
    var  mmi: Double? = null
    var  alert: String? = null
    var  tsunami: Int? = null

    constructor() {}

    constructor(location: String, mag: Double?, lat: Double?, lon: Double?, dateOccur: Long?, dept: Double?) {
        this.location = location
        this.mag = mag
        this.lat = lat
        this.lon = lon
        this.dateOccur = dateOccur
        this.dept = dept
    }
}
