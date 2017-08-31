package com.yamamz.earthquakemonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by AMRI on 8/24/2017.
 */

class Feature{

    @SerializedName("type")
    var type: String? = null
    @SerializedName("properties")
    var properties: Properties? = null
    @SerializedName("geometry")
    var geometry: Geometry? = null
    @SerializedName("id")
    var id: String? = null



}