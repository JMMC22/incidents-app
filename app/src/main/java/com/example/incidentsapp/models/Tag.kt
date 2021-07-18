package com.example.incidentsapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tag (var id : String? = null,
                var name : String? = null,
                var icon : String? = null,
                var colorBubble: String? = null ){
}
