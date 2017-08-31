/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yamamz.earthquakemonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem{
    private final LatLng mPosition;
    private String mLocation;
    private String mMag;

    public MyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        mLocation = null;
        mMag = null;
    }

    public MyItem(double lat, double lng, String  mLocation, String mMag) {
        mPosition = new LatLng(lat, lng);
        this.mLocation = mLocation;
        this.mMag = mMag;
    }



    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public String getmTitle() {
        return mLocation;
    }

    public void setmTitle(String mTitle) {
        this.mLocation = mTitle;
    }

    public String getmSnippet() {
        return mMag;
    }

    public void setmSnippet(String mSnippet) {
        this.mMag = mSnippet;
    }

    /**
     * Set the title of the marker
     *
     * @param title string to be set as title
     */
    public void setTitle(String title) {
        mLocation = title;
    }

    /**
     * Set the description of the marker
     *
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mMag = snippet;
    }





}
