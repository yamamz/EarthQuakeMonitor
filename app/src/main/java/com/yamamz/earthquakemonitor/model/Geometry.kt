package com.yamamz.earthquakemonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by AMRI on 8/24/2017.
 */

class Geometry {

    @SerializedName("type")
    var type: String? = null
    @SerializedName("coordinates")
    var coordinates: List<Double>? = null

}