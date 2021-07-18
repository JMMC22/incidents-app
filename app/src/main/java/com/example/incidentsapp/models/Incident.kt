package com.example.incidentsapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.IgnoreExtraProperties
import com.google.maps.android.clustering.ClusterItem

@IgnoreExtraProperties
data class Incident(var id: String? = null,
                    var description: String? = null,
                    var tag: String? = null,
                    var image: String? = null,
                    var latitud: Double? = null,
                    var longitud: Double? = null,
                    var status: String? = null,
                    var userId: String? = null) {

}