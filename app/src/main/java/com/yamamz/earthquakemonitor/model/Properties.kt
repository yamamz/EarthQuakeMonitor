package com.yamamz.earthquakemonitor.model
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by AMRI on 8/24/2017.
 */

class Properties {

    @SerializedName("mag")
    var mag: Double? = null
    @SerializedName("place")
    var place: String? = null
    @SerializedName("time")
    var time: Long? = null
    @SerializedName("updated")
    var updated: Long? = null
    @SerializedName("tz")
    var tz: Int? = null
    @SerializedName("url")
    var url: String? = null
    @SerializedName("detail")
    var detail: String? = null
    @SerializedName("felt")
    var felt: Int? = null
    @SerializedName("cdi")
    var cdi: Double? = null
    @SerializedName("mmi")
    var mmi: Double? = null
    @SerializedName("alert")
    var alert: String? = null
    @SerializedName("status")
    var status: String? = null
    @SerializedName("tsunami")
    var tsunami: Int? = null
    @SerializedName("sig")
    var sig: Int? = null
    @SerializedName("net")
    var net: String? = null
    @SerializedName("code")
    var code: String? = null
    @SerializedName("ids")
    var ids: String? = null
    @SerializedName("sources")
    var sources: String? = null
    @SerializedName("types")
    var types: String? = null
    @SerializedName("nst")
    var nst: Any? = null
    @SerializedName("dmin")
    var dmin: Double? = null
    @SerializedName("rms")
    var rms: Double? = null
    @SerializedName("gap")
    var gap: Double? = null
    @SerializedName("magType")
    var magType: String? = null
    @SerializedName("type")
    var type: String? = null
    @SerializedName("title")
    var title: String? = null

}
