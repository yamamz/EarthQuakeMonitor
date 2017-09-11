package com.yamamz.earthquakemonitor.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by AMRI on 9/7/2017.
 */

open class Notification : RealmObject {
    @PrimaryKey
    var notificationID: String? = null


    constructor() {}

    constructor(notificationID: String) {
        this.notificationID = notificationID

    }
}
