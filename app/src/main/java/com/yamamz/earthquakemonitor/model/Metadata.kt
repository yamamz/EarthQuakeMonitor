package com.yamamz.earthquakemonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by AMRI on 8/24/2017.
 */

class Metadata {

    @SerializedName("generated")
    var generated: Long? = null
    @SerializedName("url")
    var url: String? = null
    @SerializedName("title")
    var title: String? = null
    @SerializedName("status")
    var status: Int? = null
    @SerializedName("api")
    var api: String? = null
    @SerializedName("count")
    var count: Int? = null

}