package com.yamamz.earthquakemonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by AMRI on 8/24/2017.
 */

class EarthquakeGeoJSon {

    @SerializedName("type")
    var type: String? = null
    @SerializedName("metadata")
    var metadata: Metadata? = null
    @SerializedName("features")
    var features: ArrayList<Feature>? = null
    @SerializedName("bbox")
    var bbox: List<Double>? = null

}
